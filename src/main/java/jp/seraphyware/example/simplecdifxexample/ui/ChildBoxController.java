package jp.seraphyware.example.simplecdifxexample.ui;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import jp.seraphyware.example.simplecdifxexample.service.ExampleService;
import jp.seraphyware.example.simplecdifxexample.utils.BackgroundTaskService;
import jp.seraphyware.example.simplecdifxexample.utils.ErrorDialogUtils;
import jp.seraphyware.example.simplecdifxexample.utils.ParallelJavaFXTask;

@Dependent
public class ChildBoxController implements Initializable, WindowController {

	private static final Logger logger = Logger.getLogger(ChildBoxController.class.getName());

	@FXML
	private Parent root;

	@FXML
	private TextField textField;

	@Inject
	private ExampleService exampleService;

	@Inject
	private BackgroundTaskService bgTaskService;

	@PostConstruct
	public void postCtor() {
		logger.info("postCtor:" + this); //$NON-NLS-1$
	}

// includeされた子コントローラはdestroyの管理をしていないので
// このメソッドが呼び出されることはない。
//	@PreDestroy
//	public void preDestory() {
//		logger.info("preDestor: " + this); //$NON-NLS-1$
//	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		assert root != null;
		assert textField != null;
	}

	public StringProperty textProperty() {
		return textField.textProperty();
	}

	@Override
	public Parent getRoot() {
		return root;
	}

	/**
	 * アラートダイアログの例
	 */
	@FXML
	protected void onShow() {
		String text = textField.getText();
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(getStage());
		alert.initModality(Modality.WINDOW_MODAL);
		alert.setContentText(text);
		alert.showAndWait();
	}

	/**
	 * 1つのタスクの進捗を示すプログレス例
	 */
	@FXML
	protected void onFetch() {
		textField.setText("wait a moment");
		Task<Long> bgTask = createTask("");
		ProgressController
			.doProgressAndWait(getStage(), bgTaskService, bgTask)
			.whenComplete(this::showResult);
	}

	/**
	 * 複数のタスクの直列的な進捗を示すプログレス例
	 */
	@FXML
	protected void onFetchMulti() {
		Task<Long> bgTask1 = createTask("(1)");
		Task<Long> bgTask2 = createTask("(2)");

		ProgressController
			.doProgressAndWait(getStage(), bgTaskService, bgTask1, bgTask2)
			.whenComplete(this::showResult);
	}

	/**
	 * 複数のタスクの並列的な進捗を示すプログレス例.
	 */
	@FXML
	private void onFetchPara() {
		Task<Long> bgTask1 = createTask("(1)");
		Task<Long> bgTask2 = createTask("(2)");
		Task<Long> bgTask3 = createTask("(3)");

		// 並列で複数ジョブを実行する.
		// UIは複数のジョブのメッセージ、進捗を連結したものとなる.(タイトルは最初のジョブのみ)
		ParallelJavaFXTask parallelTask = new ParallelJavaFXTask(bgTaskService);
		parallelTask.addTask(bgTask1);
		parallelTask.addTask(bgTask2);
		parallelTask.addTask(bgTask3);

		ProgressController.doProgressAndWait(getStage(), bgTaskService, parallelTask)
			.whenComplete(this::showResult);
	}

	private Task<Long> createTask(String name) {
		return new Task<Long>() {
			@Override
			protected Long call() throws Exception {
				updateTitle("Calcurate Service");
				return exampleService.fetchCurrentValue((cur, max) -> {
					updateMessage("calcurating" + name + "..." + cur + "/" + max);
					updateProgress(cur, max);
				});
			}
		};
	}

	private <T> void showResult(T ret, Throwable ex) {
		if (ex != null) {
			if (ex instanceof CompletionException) {
				ex = ((CompletionException) ex).getCause();
			}
			if (ex instanceof CancellationException) {
				textField.setText("Cancel");

			} else {
				ErrorDialogUtils.showException(getStage(), ex);
			}
		} else {
			textField.setText(Objects.toString(ret));
		}
	}
}
