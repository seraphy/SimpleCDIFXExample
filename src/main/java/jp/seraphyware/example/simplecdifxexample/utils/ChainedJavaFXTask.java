package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;

/**
 * 複数のjavafx.concurrent.Taskを順番に実行するjavafx.concurrent.Task。<br>
 * タスクは追加した順番で実行される.<br>
 * 各子タスクのUIへの通知は、すべて、このTaskのUI通知として転送される.<br>
 * このタスクの戻り値は最後の子タスクの結果である.<br>
 * 実行中のタスクがキャンセルまたは失敗した場合は、以降のタスクは処理されない.<br>
 * 実行中にタスクが増減することは想定されていない.<Br>
 */
public class ChainedJavaFXTask extends Task<List<Object>> {

	private LinkedList<FutureTask<?>> tasks = new LinkedList<>();

	private ChangeListener<String> messageListener = (self, old, value) -> {
		updateMessage(value);
	};

	private ChangeListener<String> titleListener = (self, old, value) -> {
		updateTitle(value);
	};

	private ChangeListener<Number> progressListener = (self, old, value) -> {
		updateProgress(value.doubleValue());
	};

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
	 * javafx.concurrent.Taskのタイトル、メッセージ、プログレスのプロパティを監視し、
	 * それを、このTaskへの操作として転送する.
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

	@Override
	protected List<Object> call() throws Exception {
		List<Object> result = new ArrayList<>();
		boolean cancelled = false;
		Exception occurredException = null;
		for (FutureTask<?> task : tasks) {
			if (occurredException != null || cancelled || isCancelled()) {
				// 先行するタスクが例外またはキャンセルされている場合は
				// 後続のタスクはすべてキャンセルとする.
				task.cancel(true);
				result = null;

			} else {
				try {
					// JavaFXTaskであれば初期プロパティの取り込み
					if (task instanceof Task) {
						updateTitle(((Task<?>) task).getTitle());
						updateMessage(((Task<?>) task).getMessage());
						updateProgress(((Task<?>) task).getProgress());
					}

					// タスクを実行する.
					task.run();

					// 実行結果を取得する.
					result.add(task.get());

				} catch (InterruptedException | CancellationException ex) {
					cancelled = true;

				} catch (Exception ex) {
					occurredException = ex;

				} catch (Throwable ex) {
					occurredException = new Exception(ex);
				}
			}
		}

		if (cancelled) {
			cancel();
			return null;
		}

		// 子タスクで発生した例外を返しなおす.<br>
		if (occurredException != null) {
			if (occurredException instanceof ExecutionException) {
				Throwable iex = ((ExecutionException) occurredException)
						.getCause();
				if (iex instanceof Exception) {
					throw (Exception) iex;
				}
			}
			throw occurredException;
		}

		return result;
	}
}