package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BackgroundTaskService implements Executor {

	/**
	 * ロガー.
	 */
	private final Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * スレッドサービス.
	 */
	private ExecutorService executor;

	/**
	 * 初期化.
	 */
	@PostConstruct
	public void init() {
		logger.log(Level.INFO, "★BackgroundTaskService#init");
		executor = Executors.newCachedThreadPool();
	}

	/**
	 * 破棄処理.
	 */
	@PreDestroy
	public void dispose() {
		logger.log(Level.INFO, "★BackgroundTaskService#dispose");
		shutdown();
	}

	/**
	 * バックグラウンドジョブのキューに入れFutureを返す.
	 *
	 * @param <V>
	 *            データ型
	 * @param task
	 *            タスク
	 * @return Future
	 */
	public <V> Future<V> execute(final Callable<V> task) {
		Objects.requireNonNull(task);
		return executor.submit(task);
	}

	/**
	 * バックグラウンドジョブのキューに入れる.
	 *
	 * @param task
	 *            タスク
	 */
	@Override
	public void execute(final Runnable task) {
		Objects.requireNonNull(task);
		executor.execute(task);
	}

	/**
	 * 非同期完了可能フューチャを作成して返す.
	 * @param supplier
	 * @return
	 */
	public <U> CompletableFuture<U> createSupplyAsyncCompletableFuture(
			Supplier<U> supplier) {
		Objects.requireNonNull(supplier);
		return CompletableFuture.supplyAsync(supplier, executor);
	}

	/**
	 * 非同期完了可能フューチャを作成して返す.
	 * @param supplier
	 * @return
	 */
	public CompletableFuture<Void> createAsyncCompletableFuture(
			Runnable task) {
		Objects.requireNonNull(task);
		return CompletableFuture.runAsync(task, executor);
	}

	/**
	 * サービスを停止する.
	 */
	public void shutdown() {
		if (!executor.isShutdown()) {
			logger.log(Level.INFO, "shutdownNow");
			executor.shutdownNow();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
				logger.log(Level.INFO, "shutdown complete");

			} catch (InterruptedException ex) {
				logger.log(Level.WARNING, "サービス停止待機を割り込みにより解除しました。:" + ex, ex);
			}
		}
	}
}
