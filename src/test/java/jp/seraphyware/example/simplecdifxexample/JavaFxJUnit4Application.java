package jp.seraphyware.example.simplecdifxexample;

import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFXアプリとして起動しないとJavaFX関連のassertでひっかかるため.<br>
 * 参考: http://namihira.hatenablog.com/entry/2014/12/29/125834
 */
public class JavaFxJUnit4Application extends Application {

	private static final CountDownLatch latch = new CountDownLatch(1);

	@Override
	public void start(Stage primaryStage) throws Exception {
		latch.countDown();
	}

	public static synchronized void startJavaFX() {
		if (latch.getCount() != 0) {
			new Thread() {
				public void run() {
					// JavaFXを開始する.
					// JavaFXが終了するまで制御は返さない.
					internalLaunch();
				}
			}.start();

			try {
				latch.await();

			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private static void internalLaunch() {
		// (内部でコールスタックから呼び出し元クラスがApplication派生であるか確認しているため
		// インナークラスで実行するとエラーになるためメソッドを切り出している.)
		launch();
	}
}
