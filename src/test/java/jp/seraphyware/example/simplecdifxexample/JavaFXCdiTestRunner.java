package jp.seraphyware.example.simplecdifxexample;

import java.util.concurrent.CountDownLatch;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javafx.application.Platform;

public class JavaFXCdiTestRunner extends CdiTestRunner {

	public JavaFXCdiTestRunner(Class<?> cls) throws InitializationError {
		super(cls);
		JavaFxJUnit4Application.startJavaFX();
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		// テスト完了の待機用ラッチ
		CountDownLatch latch = new CountDownLatch(1);

		// JavaFXスレッド内でテストを実行する.
		Platform.runLater(() -> {
			try {
				super.runChild(method, notifier);
			} finally {
				latch.countDown();
			}
		});

		// 完了待機
		try {
			latch.await();

		} catch (InterruptedException ex) {
			ex.printStackTrace();
			// 無視する.
		}
	}
}
