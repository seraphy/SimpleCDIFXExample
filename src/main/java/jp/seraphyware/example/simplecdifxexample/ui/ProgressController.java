package jp.seraphyware.example.simplecdifxexample.ui;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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

	/**
	 * ワーカーの実行と、実行中のプログレスダイアログの表示制御を行う.<br>
	 * @param owner 親ウィンドウ
	 * @param bgTask ワーカー
	 */
	public static void doProgressAndWait(Window owner, Task<?> bgTask) {
		Objects.requireNonNull(bgTask);

		Instance<ProgressController> progProv = CDI.current().select(ProgressController.class);
		ProgressController controller = progProv.get();
		try {
			controller.setOwner(owner);

			Stage stg = controller.getStage();

			stg.titleProperty().bind(bgTask.titleProperty());
			controller.labelTextProperty().bind(bgTask.messageProperty());
			controller.progressProperty().set(-1); // 既定はintermediate (サークル表示)

			bgTask.setOnRunning(e -> {
				// タスク開始により進行状態をバインドする.
				controller.progressProperty().bind(bgTask.progressProperty());
			});

			controller.setOnCancel(evt -> {
				log.info("☆☆☆request cancel☆☆☆"); //$NON-NLS-1$
				bgTask.cancel();
				evt.consume();
			});

			// Taskを開始しCompletableFutureとして返す.
			CompletableFuture<Void> cf = controller.bgTaskSerive
					.createAsyncCompletableFuture(bgTask);

			// タスク完了した場合にダイアログを閉じる.
			cf.whenCompleteAsync((ret, ex) -> {
				// バインド解除
				stg.titleProperty().unbind();
				controller.progressProperty().unbind();
				controller.labelTextProperty().unbind();

				// ※ intermediateを解除しないとメモリリークする. (java8u77現在)
				controller.progressProperty().set(1);

				// ダイアログを閉じる
				controller.closeWindow();

			}, controller.guiExecutor); // JavaFXスレッドで実行する.

			// モーダルダイアログで表示する.
			if (!bgTask.isDone()) {
				stg.showAndWait();
			}
			assert !bgTask.isRunning();

		} finally {
			progProv.destroy(controller);
		}
	}
}
