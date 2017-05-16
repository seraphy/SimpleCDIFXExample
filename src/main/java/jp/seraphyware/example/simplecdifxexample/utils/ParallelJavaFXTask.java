package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.util.Pair;

/**
 * 複数の
 */
public class ParallelJavaFXTask extends Task<List<Object>> {

	private LinkedList<FutureTask<?>> tasks = new LinkedList<>();

	private ChangeListener<String> messageListener = (self, old, value) -> {
		updateMessages();
	};

	private ChangeListener<String> titleListener = (self, old, value) -> {
		updateTitles();
	};

	private ChangeListener<Number> progressListener = (self, old, value) -> {
		updateProgrsses();
	};

	private Executor executor;

	public ParallelJavaFXTask(Executor executor) {
		Objects.requireNonNull(executor);
		this.executor = executor;
	}

	protected void updateTitles() {
		// タイトルは登録順で最初の有効なタイトルを表示する.
		for (FutureTask<?> task : tasks) {
			if (task instanceof Task) {
				String title = ((Task<?>) task).getTitle();
				if (title != null && title.length() > 0) {
					updateTitle(title);
					break;
				}
			}
		}
	}

	protected void updateMessages() {
		StringBuffer buf = new StringBuffer();
		for (FutureTask<?> task : tasks) {
			if (task instanceof Task) {
				String msg = ((Task<?>) task).getMessage();
				if (msg != null && msg.length() > 0) {
					if (buf.length() > 0) {
						buf.append("\r\n");
					}
					buf.append(msg);
				}
			}
		}
		updateMessage(buf.toString());
	}

	protected void updateProgrsses() {
		double total = 0;
		double sum = 0;
		for (FutureTask<?> task : tasks) {
			if (task instanceof Task) {
				total += 1d;
				sum += ((Task<?>) task).getProgress();
			}
		}
		if (total <= 0) {
			updateProgress(-1, 0);

		} else {
			updateProgress(sum, total);
		}
	}

	public void addTask(FutureTask<?> task) {
		Objects.requireNonNull(task);
		if (task instanceof Task) {
			bind((Task<?>) task);
		}
		this.tasks.add(task);
	}

	public void removeTask(FutureTask<?> task) {
		if (this.tasks.remove(task)) {
			if (task instanceof Task) {
				unbind((Task<?>) task);
			}
		}
	}

	protected void updateProgress(double progress) {
		if (progress < 0) {
			updateProgress(-1, 0);
		} else {
			updateProgress(progress, 1d);
		}
	}

	/**
	 * javafx.concurrent.Taskのタイトル、メッセージ、プログレスのプロパティを監視し、 それを、このTaskへの操作として転送する.
	 *
	 * @param task
	 */
	private void bind(Task<?> task) {
		task.messageProperty().addListener(messageListener);
		task.titleProperty().addListener(titleListener);
		task.progressProperty().addListener(progressListener);
	}

	private void unbind(Task<?> task) {
		task.messageProperty().removeListener(messageListener);
		task.titleProperty().removeListener(titleListener);
		task.progressProperty().removeListener(progressListener);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		tasks.forEach(task -> task.cancel(mayInterruptIfRunning));
		return super.cancel(mayInterruptIfRunning);
	}

	protected CompletableFuture<Void> executeParallel(Executor executor) {
		Objects.requireNonNull(executor);


		List<Pair<Runnable, CompletableFuture<Object>>> runnables = new ArrayList<>();

		AtomicReference<Throwable> firstException = new AtomicReference<>();
		for (FutureTask<?> task : tasks) {
			CompletableFuture<Object> cf = new CompletableFuture<>();
			Runnable wrapper = (() -> {
				try {
					task.run();
					cf.complete(task.get());

				} catch (Throwable ex) {
					if (ex instanceof ExecutionException) {
						ex = ((ExecutionException) ex).getCause();
					}

					// 最初に検出された例外のみ記録し、
					// 他のすべてのタスクは最初のタスクの例外をそのまま返送する.
					// 結果としてCompletableFuture.allOf()で取得される例外を統一させられる.
					Throwable iex = firstException.accumulateAndGet(ex,
							(prev, cur) -> prev == null ? cur : prev);
					cf.completeExceptionally(iex);

					// 例外が発生した場合は他のタスクは中止する.
					tasks.forEach(sibling -> {
						if (!task.equals(sibling)) {
							if (!sibling.isDone()) {
								sibling.cancel(true);
							}
						}
					});
				}
			});
			runnables.add(new Pair<>(wrapper, cf));
		}

		for (Pair<Runnable, CompletableFuture<Object>> pair : runnables) {
			Runnable wrapper = pair.getKey();
			executor.execute(wrapper);
		}

		return CompletableFuture
				.allOf(runnables.stream().map(pair -> pair.getValue()).toArray(siz -> new CompletableFuture[siz]));
	}

	@Override
	protected List<Object> call() throws Exception {
		List<Object> result = new ArrayList<>();

		CompletableFuture<Void> cf = executeParallel(executor);
		cf.get();

		for (FutureTask<?> task : tasks) {
			result.add(task.get());
		}

		return result;
	}
}
