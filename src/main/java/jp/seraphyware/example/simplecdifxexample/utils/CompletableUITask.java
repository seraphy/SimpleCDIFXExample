package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.concurrent.Task;

/**
 * javafx.concurrent.TaskにCompletionStageの性質を加えたもの.<br>
 *
 * @param <T>
 */
public abstract class CompletableUITask<T> extends Task<T> implements CompletionStage<T> {

	private CompletableFuture<T> cf = new CompletableFuture<>();

	@Override
	protected void succeeded() {
		super.succeeded();
		try {
			cf.complete(get());
		} catch (Exception ex) {
			// success時にgetが失敗することはないはず
			cf.completeExceptionally(ex);
		}
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		cf.completeExceptionally(new CancellationException());
	}

	@Override
	protected void failed() {
		super.failed();
		Throwable ex = getException();
		cf.completeExceptionally(
				(ex == null)
						? new RuntimeException("failed for unknown reason")
						: ex);
	}

	public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
		return cf.thenApply(fn);
	}

	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
		return cf.thenApplyAsync(fn);
	}

	public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
		return cf.thenApplyAsync(fn, executor);
	}

	public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
		return cf.thenAccept(action);
	}

	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
		return cf.thenAcceptAsync(action);
	}

	public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
		return cf.thenAcceptAsync(action, executor);
	}

	public CompletableFuture<Void> thenRun(Runnable action) {
		return cf.thenRun(action);
	}

	public CompletableFuture<Void> thenRunAsync(Runnable action) {
		return cf.thenRunAsync(action);
	}

	public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
		return cf.thenRunAsync(action, executor);
	}

	public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return cf.thenCombine(other, fn);
	}

	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn) {
		return cf.thenCombineAsync(other, fn);
	}

	public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
		return cf.thenCombineAsync(other, fn, executor);
	}

	public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return cf.thenAcceptBoth(other, action);
	}

	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action) {
		return cf.thenAcceptBothAsync(other, action);
	}

	public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super T, ? super U> action, Executor executor) {
		return cf.thenAcceptBothAsync(other, action, executor);
	}

	public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
		return cf.runAfterBoth(other, action);
	}

	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
		return cf.runAfterBothAsync(other, action);
	}

	public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		return cf.runAfterBothAsync(other, action, executor);
	}

	public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
		return cf.applyToEither(other, fn);
	}

	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
		return cf.applyToEitherAsync(other, fn);
	}

	public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn,
			Executor executor) {
		return cf.applyToEitherAsync(other, fn, executor);
	}

	public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
		return cf.acceptEither(other, action);
	}

	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
		return cf.acceptEitherAsync(other, action);
	}

	public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action,
			Executor executor) {
		return cf.acceptEitherAsync(other, action, executor);
	}

	public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
		return cf.runAfterEither(other, action);
	}

	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
		return cf.runAfterEitherAsync(other, action);
	}

	public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		return cf.runAfterEitherAsync(other, action, executor);
	}

	public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
		return cf.thenCompose(fn);
	}

	public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
		return cf.thenComposeAsync(fn);
	}

	public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
			Executor executor) {
		return cf.thenComposeAsync(fn, executor);
	}

	public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
		return cf.whenComplete(action);
	}

	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
		return cf.whenCompleteAsync(action);
	}

	public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
		return cf.whenCompleteAsync(action, executor);
	}

	public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
		return cf.handle(fn);
	}

	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
		return cf.handleAsync(fn);
	}

	public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
		return cf.handleAsync(fn, executor);
	}

	public CompletableFuture<T> toCompletableFuture() {
		return cf;
	}

	public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
		return cf.exceptionally(fn);
	}
}