package jp.seraphyware.example.simplecdifxexample.ui;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;

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
import jp.seraphyware.example.simplecdifxexample.utils.ErrorDialogUtils;

@Dependent
public class ChildBoxController implements Initializable, WindowController {

	private static final Logger logger = Logger.getLogger(ChildBoxController.class.getName());

	@FXML
	private Parent root;

	@FXML
	private TextField textField;

	@Inject
	private ExampleService exampleService;

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

	@FXML
	protected void onShow() {
		String text = textField.getText();
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(getStage());
		alert.initModality(Modality.WINDOW_MODAL);
		alert.setContentText(text);
		alert.showAndWait();
	}

	@FXML
	protected void onFetch() {
		textField.setText("wait a moment");

		Task<Long> bgTask = new Task<Long>() {
			@Override
			protected Long call() throws Exception {
				updateTitle("Calcurate Service");
				return exampleService.fetchCurrentValue((cur, max) -> {
					updateMessage("calcurating..." + cur + "/" + max);
					updateProgress(cur, max);
				});
			}
		};

		ProgressController.doProgressAndWait(getStage(), bgTask);

		try {
			Long result = bgTask.get();
			textField.setText(new Timestamp(result).toString());

		} catch (CancellationException ex) {
			textField.setText("cancel");

		} catch (Exception ex) {
			ErrorDialogUtils.showException(getStage(), ex);
		}
	}
}
