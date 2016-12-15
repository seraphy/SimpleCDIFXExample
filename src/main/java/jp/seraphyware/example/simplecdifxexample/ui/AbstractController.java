package jp.seraphyware.example.simplecdifxexample.ui;

import java.util.Objects;

import javafx.scene.Parent;

public abstract class AbstractController implements WindowController {

	/**
	 * ルートとなるJavaFXコンテナ要素
	 */
	private Parent root;

	/**
	 * ルートとなるJavaFXコンテナ要素を(まだ作成していなければ)作成する.
	 */
	protected abstract void makeRoot();

	/**
	 * ルートとなるJavaFXコンテナ要素を取得する.<br>
	 * まだ作成されていない場合は作成される.<br>
	 *
	 * @return
	 */
	@Override
	public Parent getRoot() {
		if (root == null) {
			makeRoot();
			assert root != null;
		}
		return root;
	}

	/**
	 * ルートとなるJavaFXコンテナ要素を設定する.
	 *
	 * @param root
	 */
	protected final void setRoot(Parent root) {
		Objects.requireNonNull(root);

		// 作成したParentのuserDataに、
		// このコントローラのインスタンスを設定する.
		root.setUserData(this);

		this.root = root;
	}
}
