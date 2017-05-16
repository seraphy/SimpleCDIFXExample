package jp.seraphyware.example.simplecdifxexample.ui;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import jp.seraphyware.example.simplecdifxexample.utils.ChainedJavaFXTask;
import jp.seraphyware.example.simplecdifxexample.utils.FXMLWindowController;

@Dependent
@FXMLWindowController("Progress.fxml")
public class ProgressController extends AbstractFXMLWindowController {

	private static final Logger log = Logger.getLogger(ProgressController.class.getName());

	@FXML
	private Button btnCancel;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private Label txtLabel;

	@Override
	public void onCloseRequest(WindowEvent event) {
		doCancel();
	}

	@FXML
	protected void onCancel(ActionEvent evt) {
		doCancel();
	}

	protected void doCancel() {
		ActionEvent evt = new ActionEvent(this, ActionEvent.NULL_SOURCE_TARGET);
		if (handlerCancel != null) {
			handlerCancel.handle(evt);
		}
		if (!evt.isConsumed()) {
			closeWindow();
		}
	}

	private EventHandler<ActionEvent> handlerCancel;

	public void setOnCancel(EventHandler<ActionEvent> handler) {
		this.handlerCancel = handler;
	}

	public StringProperty labelTextProperty() {
		return txtLabel.textProperty();
	}

	public DoubleProperty progressProperty() {
		return progressIndicator.progressProperty();
	}

	@Override
	protected Stage createStage() {
		Stage stg = super.createStage();
		stg.initModality(Modality.WINDOW_MODAL);
		return stg;
	}

	private void bind(Task<?> bgTask) {
		Stage stg = getStage();

		stg.titleProperty().bind(bgTask.titleProperty());
		progressProperty().set(-1); // 既定はintermediate (サークル表示)
		labelTextProperty().bind(bgTask.messageProperty());

		bgTask.setOnRunning(e -> {
			progressProperty().bind(bgTask.progressProperty());
		});

		setOnCancel(evt -> {
			log.info("☆☆☆request cancel☆☆☆"); //$NON-NLS-1$
			bgTask.cancel();
			evt.consume();
		});
	}

	private void unbind() {
		getStage().titleProperty().unbind();
		progressProperty().unbind();
		labelTextProperty().unbind();

		// ※ intermediateを解除しないとメモリリークする. (java8u77現在)
		progressProperty().set(1);
	}

	/**
	 * 連続したワーカーの実行と、実行中のプログレスダイアログの表示制御を行う.<br>
	 * 複数のタスクを指定した場合は、最初のタスクから順番に実行される.<br>
	 * ワーカーに{@link javafx.concurrent.Task}を指定した場合はUI制御も行うことができる.<br>
	 * 実行中のタスクがキャンセルまたは失敗した場合は、以降のタスクは処理されない.<br>
	 * ワーカー群はChainedJavaFXTaskによって1つのタスクにまとめられて、
	 * {@link #showProgressAndWait(Window, Task, Executor)}が呼び出されている.<br>
	 * @param owner 親ウィンドウ、null可
	 * @param jobExecutor ジョブを実行するエグゼキュータ
	 * @param bgTasks ワーカーのリスト、Taskクラスの場合はUI制御も可能
	 * @return ジョブ全体の待ち合わせに使われたCompletableFuture
	 * @see ChainedJavaFXTask
	 */
	public static CompletableFuture<List<Object>> doProgressAndWait(Window owner, Executor jobExecutor,
			FutureTask<?>... bgTasks) {
		Objects.requireNonNull(bgTasks);

		ChainedJavaFXTask bgTask = new ChainedJavaFXTask();
		for (FutureTask<?> task : bgTasks) {
			bgTask.addTask(task);
		}

		return doProgressAndWait(owner, jobExecutor, bgTask);
	}

	/**
	 * ワーカーを指定したエグゼキュータで実行し、実行中のプログレスダイアログの表示制御を行う.<br>
	 * @param owner 親ウィンドウ、null可
	 * @param bgTask UIタスク
	 * @param jobExecutor ジョブを実行するエグゼキュータ
	 * @return ジョブの待ち合わせに使用されたCompletableFuture.(戻り値を受けた時点で、すでに完了済みである)
	 */
	public static <T> CompletableFuture<T> doProgressAndWait(Window owner, Executor jobExecutor, Task<T> bgTask) {
		Objects.requireNonNull(bgTask);
		Objects.requireNonNull(jobExecutor);

		return showProgressAndWait(owner, bgTask, task -> {
			CompletableFuture<T> cf = new CompletableFuture<>();
			Runnable uiTaskWrap = () -> {
				try {
					bgTask.run();
					cf.complete(bgTask.get());

				} catch (Throwable ex) {
					cf.completeExceptionally(ex);
				}
			};
			jobExecutor.execute(uiTaskWrap);;
			return cf;
		});
	}

	/**
	 * プログレスダイアログを表示する.<br>
	 * このメソッド自身ではジョブの実行制御は行わないため、すでに起動しているbgTaskを与えるか、もしくは
	 * cfFactory関数が呼び出された時点で開始することを想定している.<br>
	 * cfFactoryが返したCompluetableFutureで完了状態になったらプログレスダイアログは閉じられる.<br>
	 * @param owner 親ウィンドウ、null可
	 * @param bgTask UI制御プロパティもつ持つジョブ。ジョブのUIを接続するだけで、開始等の制御については関知しない。
	 * @param cfFactory ジョブを指定して、そのジョブに対するCompletableFutureを返す関数.<br>
	 * @return ジョブの待ち合わせに使用されたCompletableFuture.(戻り値を受けた時点で、すでに完了済みである)
	 */
	public static <T, R> CompletableFuture<R> showProgressAndWait(Window owner, Task<T> bgTask,
			Function<Task<T>, CompletableFuture<R>> cfFactory) {
		Objects.requireNonNull(bgTask);
		Objects.requireNonNull(cfFactory);

		Instance<ProgressController> progProv = CDI.current()
				.select(ProgressController.class);
		ProgressController controller = progProv.get();
		try {
			controller.setOwner(owner);

			Stage stg = controller.getStage();
			controller.bind(bgTask);

			CompletableFuture<R> cf = cfFactory.apply(bgTask);

			// タスク完了した場合にダイアログを閉じる.
			cf.whenCompleteAsync((ret, ex) -> {
				controller.unbind();

				// ダイアログを閉じる
				controller.closeWindow();

			} , Platform::runLater); // JavaFXスレッドで実行する.

			// モーダルダイアログで表示する.
			if (!cf.isDone()) {
				stg.showAndWait();
			}

			return cf;

		} finally {
			progProv.destroy(controller);
		}
	}

}
