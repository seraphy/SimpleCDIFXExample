package jp.seraphyware.example.simplecdifxexample.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface WindowController {

	/**
	 * ルートノード<br>
	 * @return
	 */
	Parent getRoot();

	/**
	 * このコントローラが埋め込まれているStageを取得する.<br>
	 * @return
	 */
	default Stage getStage() {
		Parent root = getRoot();
		if (root != null) {
			Scene scene = root.getScene();
			if (scene != null) {
				return (Stage) scene.getWindow();
			}
		}
		return null;
	}
}
