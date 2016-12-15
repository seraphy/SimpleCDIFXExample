package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.enterprise.context.ApplicationScoped;

import javafx.application.Platform;

@ApplicationScoped
public class FXThreadExecutor implements Executor {

	@Override
	public void execute(Runnable command) {
		Objects.requireNonNull(command);
		Platform.runLater(command);
	}

	public void runAndWait(Runnable command) {
		Objects.requireNonNull(command);
		if (Platform.isFxApplicationThread()) {
			// 現在のスレッドがJavaFXのアプリケーションスレッドなら、そのまま実行
			command.run();

		} else {
			// そうでなければJavaFXのスレッドキューに入れて実行を待機する.
			CompletableFuture<Void> cf = new CompletableFuture<>();
			Runnable r = () -> {
				try {
					command.run();
					cf.complete(null);

				} catch (Throwable ex) {
					cf.completeExceptionally(ex);
				}
			};
			Platform.runLater(r);
			cf.join();
		}
	}
}
