package jp.seraphyware.example.simplecdifxexample;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import javafx.scene.control.TableView;

@RunWith(JavaFxJUnit4ClassRunner.class)
public class FXComponentTest {

	@Test
	public void testExample() {
		// JavaFXコンポーネントはJavaFXスレッド内でないと例外が発生するため、
		// Runnerの仕組みでJavaFXスレッドを起こして、その中でテストする必要がある.
		TableView<Object> tableView = new TableView<>();
		assertTrue(tableView != null);
	}
}
