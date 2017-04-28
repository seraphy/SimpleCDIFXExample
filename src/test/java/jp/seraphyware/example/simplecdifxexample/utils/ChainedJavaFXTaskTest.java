package jp.seraphyware.example.simplecdifxexample.utils;

import static org.junit.Assert.*;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.sun.javafx.tk.Toolkit;

import javafx.application.Platform;
import javafx.concurrent.Task;
import jp.seraphyware.example.simplecdifxexample.JavaFxJUnit4ClassRunner;

@SuppressWarnings("restriction")
@RunWith(JavaFxJUnit4ClassRunner.class)
public class ChainedJavaFXTaskTest {

	@Test
	public void testEmpty() throws Exception {
		ChainedJavaFXTask task = new ChainedJavaFXTask();
		ForkJoinPool.commonPool().execute(task);
		Object ret = task.get();
		assertNull(ret);
	}

	@Test
	public void testSingle() throws Exception {
		FutureTask<String> childTask = new FutureTask<>(() -> "OK");

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask);

		ForkJoinPool.commonPool().execute(task);
		assertEquals("OK", task.get());
		assertEquals("OK", childTask.get());
	}

	@Test
	public void testSingleFX() throws Exception {
		Task<String> childTask = new Task<String>() {
			@Override
			protected String call() throws Exception {
				updateTitle("title1");
				updateMessage("message1");
				updateProgress(-1, 0);
				return "OK";
			}
		};

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask);

		ForkJoinPool.commonPool().execute(task);
		assertEquals("OK", task.get());
		assertEquals("OK", childTask.get());

		assertTrue(Platform.isFxApplicationThread());
		Platform.runLater(() -> {
			assertEquals("title1", task.getTitle());
			assertEquals("message1", task.getMessage());
			assertTrue(-1d == task.getProgress());
			Toolkit.getToolkit().exitNestedEventLoop(task, null);
		});
		Toolkit.getToolkit().enterNestedEventLoop(task);

	}

	@Test
	public void testMulti() throws Exception {
		FutureTask<String> childTask1 = new FutureTask<>(() -> "OK1");
		FutureTask<String> childTask2 = new FutureTask<>(() -> "OK2");
		FutureTask<String> childTask3 = new FutureTask<>(() -> "OK3");

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask1);
		task.addTask(childTask2);
		task.addTask(childTask3);

		ForkJoinPool.commonPool().execute(task);
		assertEquals("OK3", task.get());
		assertEquals("OK1", childTask1.get());
		assertEquals("OK2", childTask2.get());
		assertEquals("OK3", childTask3.get());
	}

	@Test
	public void testMultiFX() throws Exception {
		Task<String> childTask1 = new Task<String>() {
			@Override
			protected String call() throws Exception {
				updateTitle("title1");
				updateMessage("message1");
				updateProgress(-1, 0);
				return "OK1";
			}
		};
		Task<String> childTask2 = new Task<String>() {
			@Override
			protected String call() throws Exception {
				updateTitle("title2");
				updateMessage("message2");
				updateProgress(50, 100);
				return "OK2";
			}
		};

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask1);
		task.addTask(childTask2);

		ForkJoinPool.commonPool().execute(task);
		assertEquals("OK2", task.get());
		assertEquals("OK1", childTask1.get());
		assertEquals("OK2", childTask2.get());

		Platform.runLater(() -> {
			assertEquals("title2", task.getTitle());
			assertEquals("message2", task.getMessage());
			assertTrue(0.5d == task.getProgress());
			Toolkit.getToolkit().exitNestedEventLoop(task, null);
		});
		Toolkit.getToolkit().enterNestedEventLoop(task);
	}

	@Test
	public void testSingleException() throws Exception {
		FutureTask<String> childTask = new FutureTask<>(() -> {
			throw new RuntimeException("ERROR");
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask);

		ForkJoinPool.commonPool().execute(task);

		{
			Throwable result = null;
			try {
				task.get();

			} catch (ExecutionException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertEquals("ERROR", result.getCause().getMessage());
		}

		{
			Throwable result = null;
			try {
				childTask.get();

			} catch (ExecutionException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertEquals("ERROR", result.getCause().getMessage());
		}
	}

	@Test
	public void testMultiException() throws Exception {
		FutureTask<String> childTask1 = new FutureTask<>(() -> {
			throw new RuntimeException("ERROR");
		});
		FutureTask<String> childTask2 = new FutureTask<>(() -> {
			return "OK2";
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask1);
		task.addTask(childTask2);

		ForkJoinPool.commonPool().execute(task);

		{
			Throwable result = null;
			try {
				task.get();

			} catch (ExecutionException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertEquals("ERROR", result.getCause().getMessage());
		}

		{
			Throwable result = null;
			try {
				childTask1.get();

			} catch (ExecutionException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertEquals("ERROR", result.getCause().getMessage());
		}

		{
			Throwable result = null;
			try {
				childTask2.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
		}
	}

	@Test
	public void testMultiException2() throws Exception {
		FutureTask<String> childTask1 = new FutureTask<>(() -> {
			return "OK1";
		});
		FutureTask<String> childTask2 = new FutureTask<>(() -> {
			throw new RuntimeException("ERROR");
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask1);
		task.addTask(childTask2);

		ForkJoinPool.commonPool().execute(task);

		{
			Throwable result = null;
			try {
				task.get();

			} catch (ExecutionException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertEquals("ERROR", result.getCause().getMessage());
		}

		{
			assertEquals("OK1", childTask1.get());
		}

		{
			Throwable result = null;
			try {
				childTask2.get();

			} catch (ExecutionException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertEquals("ERROR", result.getCause().getMessage());
		}
	}

	@Test
	public void testSingleCancel() throws Exception {
		FutureTask<String> childTask = new FutureTask<>(() -> {
			Thread.sleep(10000);
			return null;
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask);

		ForkJoinPool.commonPool().execute(task);
		task.cancel();

		{
			Throwable result = null;
			try {
				task.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}

		{
			Throwable result = null;
			try {
				childTask.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}
	}

	@Test
	public void testSingleCancelChild() throws Exception {
		FutureTask<String> childTask = new FutureTask<>(() -> {
			Thread.sleep(10000);
			return null;
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask);

		ForkJoinPool.commonPool().execute(task);
		childTask.cancel(true); // 子側にキャンセルを通知した場合

		{
			Throwable result = null;
			try {
				task.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}

		{
			Throwable result = null;
			try {
				childTask.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}
	}

	@Test
	public void testMultiCancel() throws Exception {
		FutureTask<String> childTask1 = new FutureTask<>(() -> {
			Thread.sleep(10000);
			return null;
		});
		FutureTask<String> childTask2 = new FutureTask<>(() -> {
			Thread.sleep(10000);
			return null;
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask1);
		task.addTask(childTask2);

		ForkJoinPool.commonPool().execute(task);
		task.cancel();

		{
			Throwable result = null;
			try {
				task.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}

		{
			Throwable result = null;
			try {
				childTask1.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}

		{
			Throwable result = null;
			try {
				childTask2.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}
	}

	@Test
	public void testMultiCancel2() throws Exception {
		FutureTask<String> childTask1 = new FutureTask<>(() -> {
			return "OK1";
		});
		FutureTask<String> childTask2 = new FutureTask<>(() -> {
			Thread.sleep(10000);
			return null;
		});

		ChainedJavaFXTask task = new ChainedJavaFXTask();
		task.addTask(childTask1);
		task.addTask(childTask2);

		ForkJoinPool.commonPool().execute(task);
		Thread.sleep(100);
		task.cancel();

		{
			Throwable result = null;
			try {
				task.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}

		{
			assertEquals("OK1", childTask1.get());
		}

		{
			Throwable result = null;
			try {
				childTask2.get();

			} catch (CancellationException ex) {
				result = ex;
			}
			assertNotNull(result);
			assertTrue(result instanceof CancellationException);
		}
	}
}
