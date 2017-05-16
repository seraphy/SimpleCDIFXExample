package jp.seraphyware.example.simplecdifxexample.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.WindowEvent;
import jp.seraphyware.example.simplecdifxexample.utils.CDIFXMLLoader;
import jp.seraphyware.example.simplecdifxexample.utils.FXMLWindowController;

@Dependent
@FXMLWindowController("MainWindow.fxml")
public class MainWindowController extends AbstractFXMLWindowController
	implements Initializable {

	private static final Logger logger = Logger.getLogger(MainWindowController.class.getName());

	@Inject
	@CDIFXMLLoader
	private ResourceBundle resources;

	@FXML
	private ChildBoxController childBoxController;

	@PostConstruct
	public void postCtor() {
		logger.info("postCtor:" + this); //$NON-NLS-1$
	}

	@PreDestroy
	public void preDestory() {
		logger.info("preDestor: " + this); //$NON-NLS-1$
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		assert childBoxController != null;

		StringProperty prop = childBoxController.textProperty();
		prop.set(resources.getString("initial.message"));
	}

	@Override
	public void onCloseRequest(WindowEvent event) {
		performClose();
	}

	@FXML
	protected void onClose() {
		performClose();
	}

	/**
	 * 閉じて良いか確認してからウィンドウを閉じる.
	 * @return 閉じた場合はtrue、キャンセルした場合はfalse
	 */
	protected boolean performClose() {
		getStage().toFront(); // 前面にしてから問い合わせる

		// ウィンドウを閉じて良いか確認する.
		Alert closeConfirmAlert = new Alert(AlertType.CONFIRMATION);
		closeConfirmAlert.initOwner(getStage());
		closeConfirmAlert.setHeaderText(resources.getString("mainWindow.closeConfirm")); //$NON-NLS-1$

		ButtonType result = closeConfirmAlert.showAndWait().orElse(null);
		if (ButtonType.OK.equals(result)) {
			closeWindow();
			return true;
		}

		return false;
	}
}
