package jp.seraphyware.example.simplecdifxexample;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jp.seraphyware.example.simplecdifxexample.ui.MainWindowController;
import jp.seraphyware.example.simplecdifxexample.ui.WindowManager;

/**
 * Weld-SEを使ったアプリケーション例のエントリポイント
 */
public class Main extends Application {

	private static final Logger logger = Logger.getLogger(Main.class.toString());

	/**
	 * CDIコンテナ. (apache delta-spike)
	 */
	private CdiContainer cdiContainer;

	/**
	 * このBeanでの明示的Injectに使用したCreationContext.<br>
	 * releaseに用いる.<br>
	 */
	private CreationalContext<Main> creationalContext;

	/**
	 * ウィンドウを管理するための管理Bean.
	 */
	private WindowManager windowMgr;

	@Inject
	private void setWindowManager(WindowManager windowMgr) {
		this.windowMgr = windowMgr;
	}

	@Override
	public void init() throws Exception {
		logger.info("initializing...");
		try {
			// CDIコンテナの作成と起動
			cdiContainer = CdiContainerLoader.getCdiContainer();
			cdiContainer.boot();

			// コンテキストの有効化
			ContextControl contextControl = cdiContainer.getContextControl();
			contextControl.startContexts();

			// BeanManagerの取得
			BeanManager beanManager = CDI.current().getBeanManager();

			// このBeanのフィールド(およびメソッド注入)に明示的にInjectを行う
			logger.info("manual injection...");
			creationalContext = beanManager.createCreationalContext(null);

	        AnnotatedType<Main> annotatedType = beanManager.createAnnotatedType(Main.class);
	        InjectionTarget<Main> injectionTarget = beanManager.createInjectionTarget(annotatedType);
	        injectionTarget.inject(this, creationalContext);

			// 明示的に終了する
			Platform.setImplicitExit(false);

		} catch (RuntimeException ex) {
			logger.log(Level.SEVERE, "Exception in Application init method. " + ex, ex);
			throw ex;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		logger.info("start.");
		try {
			windowMgr.newWindow(MainWindowController.class).openWindow();

		} catch (RuntimeException ex) {
			logger.log(Level.SEVERE, "Exception in Application start method. " + ex, ex);
			throw ex;
		}
	}

	@Override
	public void stop() throws Exception {
		logger.info("Pre-Destroy for manual injection beans.");
		// injectしたオブジェクト群のreleaseを指示する.
		creationalContext.release();

		// CDIの終了
		logger.info("stop cdi context");
		ContextControl contextControl = cdiContainer.getContextControl();
		contextControl.stopContexts();

		cdiContainer.shutdown();
		logger.info("stopped.");

		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
