package jp.seraphyware.example.simplecdifxexample.service;

import static org.mockito.Mockito.*;

import javax.inject.Inject;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.projectstage.ProjectStage.UnitTest;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.seraphyware.example.simplecdifxexample.ex2.Bar;


@RunWith(CdiTestRunner.class) // CDIを有効にする
@TestControl(projectStage = UnitTest.class)
public class ExampleServiceTest {

	// Mockitoのアノテーションを処理する
	@Rule
    public MockitoRule rule = MockitoJUnit.rule();

	// DeltaSpikeのMockManager
	@Inject
	private DynamicMockManager mockManager;

	// 現在のステージを取得する.
	@Inject
	private ProjectStage projectStage;

	// テスト対象サービス
	@Inject
	private ExampleService srv;

	// サービスの中で使われるbarをMockitoの@Spyで挙動を変更する
	@Spy
	private Bar bar;

	@Test
	public void test() {
		System.out.println("projectStage=" + projectStage);

		doNothing().when(bar).test(); // 何もしないようにする
		mockManager.addMock(bar);

		// META-INF/apache-deltaspike.propertiesのテスト用の値の確認
		System.out.println("base=" + srv.getBase());
		System.out.println("range=" + srv.getRange());

		// サービスの実行
		srv.fetchCurrentValue((cur, max) -> {
			System.out.println(String.format("cur=%d, max=%d", cur, max));
		});
	}

}
