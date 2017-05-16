package jp.seraphyware.example.simplecdifxexample.ui;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import jp.seraphyware.example.simplecdifxexample.Main;

public abstract class AbstractWindowController extends AbstractController {

	/**
	 * ウィンドウマネージャ
	 */
	@Inject
	private WindowManager windowMgr;

	/**
	 * 親ウィンドウ、null可
	 */
	private Window owner;

	/**
	 * 作成されたステージ、未作成ならばnull
	 */
	private Stage stage;

	/**
	 * 作成されたシーン、未作成ならnull
	 */
	private Scene scene;

	/**
	 * ステージの閉じるボタン押下時のイベントハンドラ
	 */
	private final EventHandler<WindowEvent> closeRequestHandler = event -> {
		onCloseRequest(event);
		event.consume();
	};

	/**
	 * デフォルトコンストラクタ
	 */
	protected AbstractWindowController() {
		this(null);
	}

	/**
	 * コンストラクタ
	 *
	 * @param owner
	 *            親(null可)
	 */
	protected AbstractWindowController(Window owner) {
		this.owner = owner;
	}

	/**
	 * ステージの閉じるボタン押下時に処理する内容.
	 *
	 * @param event
	 */
	public abstract void onCloseRequest(WindowEvent event);

	/**
	 * 親を設定する.<br>
	 * (ステージを構築する前に指定する必要がある.)<br>
	 *
	 * @param owner
	 */
	public void setOwner(Window owner) {
		this.owner = owner;
	}

	/**
	 * 親を取得する.
	 *
	 * @return
	 */
	public Window getOwner() {
		return owner;
	}

	/**
	 * ステージを設定する.<br>
	 * すでにステージが設定済みであってはならない.<br>
	 *
	 * @param stage
	 */
	public void setStage(Stage stage) {
		if (this.stage != null) {
			throw new IllegalStateException();
		}
		this.stage = stage;
	}

	/**
	 * ステージを取得する.<br>
	 * ステージがまだ作成されていない場合はステージを作成する.<br>
	 * ステージはシーンが設定済みとなる.<br>
	 *
	 * @return
	 */
	@Override
	public Stage getStage() {
		if (stage == null) {
			stage = createStage();
			stage.setScene(getScene());
		}
		return stage;
	}

	/**
	 * ステージを作成する.<br>
	 * アイコンが設定される.<br>
	 *
	 * @return
	 */
	protected Stage createStage() {
		Stage stage = new Stage();
		stage.initOwner(owner);
		stage.setOnCloseRequest(closeRequestHandler);

		stage.setOnShown(evt -> {
			windowMgr.register(this);
		});
		stage.setOnHidden(evt -> {
			windowMgr.unregister(this);
		});

		// アイコンの設定 (最適なサイズが選択される)
		Collection<Image> icons = getIcons();
		if (icons != null) {
			stage.getIcons().addAll(icons);
		}

		return stage;
	}

	protected Collection<Image> getIcons() {
		// (Main.classと同じパッケージ上からアイコンを取得)
		Class<?> cls = Main.class;
		return Arrays.asList(
				new Image(cls.getResourceAsStream("icon128.png")),
				new Image(cls.getResourceAsStream("icon48.png")),
				new Image(cls.getResourceAsStream("icon32.png")),
				new Image(cls.getResourceAsStream("icon24.png")),
				new Image(cls.getResourceAsStream("icon16.png")));
	}

	/**
	 * シーンを取得する.<br>
	 * シーンが作成されていなければ作成される.<br>
	 * シーンにはJavaFXルート要素が設定済みとなる.<br>
	 *
	 * @return
	 */
	public Scene getScene() {
		assert Platform.isFxApplicationThread();

		if (scene == null) {
			scene = new Scene(getRoot());
		}

		return scene;
	}

	/**
	 * ステージを表示する.<br>
	 * ステージが作成されていなけば作成される.<br>
	 */
	public void openWindow() {
		assert Platform.isFxApplicationThread();

		getStage().show();
		getStage().toFront();
	}

	/**
	 * ステージを閉じる.<br>
	 */
	public void closeWindow() {
		assert Platform.isFxApplicationThread();
		if (stage != null) {
			stage.close();
		}
	}
}
