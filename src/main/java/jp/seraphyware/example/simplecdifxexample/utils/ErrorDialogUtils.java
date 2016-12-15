package jp.seraphyware.example.simplecdifxexample.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

public final class ErrorDialogUtils {

	private ErrorDialogUtils() {
		super();
	}

	public static void showException(Window owner, Throwable ex) {
		if (ex == null) {
			return;
		}

		// 非同期タスク実行時例外は、その内側の例外を表示するためラップを外す
		if (ex instanceof ExecutionException) {
			Throwable iex = ex.getCause();
			if (iex != null) {
				ex = iex;
			}
		}

		// jdk1.8u40から、Alertクラスによるダイアログがサポートされた.
		// 使い方は以下引用
		// http://code.makery.ch/blog/javafx-dialogs-official/

		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(owner);
		alert.setTitle("Exception Dialog");
		alert.setHeaderText(ex.getClass().getName());
		alert.setContentText(ex.getMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}
}
