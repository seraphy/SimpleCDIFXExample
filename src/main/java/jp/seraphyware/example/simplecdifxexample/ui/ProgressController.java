package jp.seraphyware.example.simplecdifxexample.ui;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

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
import jp.seraphyware.example.simplecdifxexample.utils.BackgroundTaskService;
import jp.seraphyware.example.simplecdifxexample.utils.ChainedJavaFXTask;
import jp.seraphyware.example.simplecdifxexample.utils.FXMLWindowController;
import jp.seraphyware.example.simplecdifxexample.utils.FXThreadExecutor;

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

	@Inject
	private BackgroundTaskService bgTaskSerive;

	@Inject
	private FXThreadExecutor guiExecutor;

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
	 * ワーカーの実行と、実行中のプログレスダイアログの表示制御を行う.<br>
	 * @param owner 親ウィンドウ
	 * @param bgTask ワーカー
	 */
	public static void doProgressAndWait(Window owner, Task<?> bgTask) {
		Objects.requireNonNull(bgTask);
		progressAndWait(owner, bgTask, (self) -> {
			return self.bgTaskSerive.createAsyncCompletableFuture(bgTask);
		});
	}

	/**
	 * 連続したワーカーの実行と、実行中のプログレスダイアログの表示制御を行う.<br>
	 * 複数のタスクを指定した場合は、最初のタスクから順番に実行される.<br>
	 * 実行中のタスクがキャンセルまたは失敗した場合は、以降のタスクは処理されない.<br>
	 * @param owner 親ウィンドウ
	 * @param bgTasks ワーカーのリスト
	 */
	public static void doProgressAndWait(Window owner, FutureTask<?>... bgTasks) {
		Objects.requireNonNull(bgTasks);

		ChainedJavaFXTask bgTask = new ChainedJavaFXTask();
		for (FutureTask<?> task : bgTasks) {
			bgTask.addTask(task);
		}
		progressAndWait(owner, bgTask, (self) -> {
			return self.bgTaskSerive.createAsyncCompletableFuture(bgTask);
		});
	}

	/**
	 * すでに実行されているCompletableなワーカーに対するプログレスダイアログの表示制御を行う.<br>
	 * @param owner
	 * @param bgTask プログレスに表示するプロパティをもつTask
	 * @param cf タスクの実際の実行状態を示すCompletableFuture
	 */
	public static void progressAndWait(Window owner, Task<?> bgTask,
			CompletableFuture<?> cf) {
		progressAndWait(owner, bgTask, (self) -> cf);
	}

	/**
	 * ワーカーに対するプログレスダイアログの表示制御を行う.<br>
	 * cfFactoryは、すでに開始されているCompletableFutureを返してもよいし、
	 * あるいはファクトリによってCompletableFutureを作成して返しても良い.<br>
	 * @param owner 親ウィンドウ
	 * @param bgTask プログレスに表示するプロパティをもつTask
	 * @param cfFactory タスクの実際の実行状態を示すCompletableFutureを返す.
	 */
	private static void progressAndWait(Window owner, Task<?> bgTask,
			Function<ProgressController, CompletableFuture<?>> cfFactory) {
		Objects.requireNonNull(bgTask);

		Instance<ProgressController> progProv = CDI.current()
				.select(ProgressController.class);
		ProgressController controller = progProv.get();
		try {
			controller.setOwner(owner);

			Stage stg = controller.getStage();
			controller.bind(bgTask);

			CompletableFuture<?> cf = cfFactory.apply(controller);

			// タスク完了した場合にダイアログを閉じる.
			cf.whenCompleteAsync((ret, ex) -> {
				controller.unbind();

				// ダイアログを閉じる
				controller.closeWindow();

			} , controller.guiExecutor); // JavaFXスレッドで実行する.

			// モーダルダイアログで表示する.
			if (!cf.isDone()) {
				stg.showAndWait();
			}

		} finally {
			progProv.destroy(controller);
		}
	}
}
