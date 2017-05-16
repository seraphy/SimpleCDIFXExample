package jp.seraphyware.example.simplecdifxexample.ui;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import javafx.application.Platform;

@ApplicationScoped
public class WindowManager {

	@Inject
	private BeanManager beanManager;

	/**
	 * 開いているウィンドウのリスト.
	 */
	private Map<AbstractWindowController, CreationalContext<?>> windowList = new IdentityHashMap<>();

	/**
	 * 新しいウィンドウを開く.<br>
	 */
	public AbstractWindowController newWindow(Class<? extends AbstractWindowController> cls) {
		Objects.requireNonNull(cls);

		//BeanManager beanManager = CDI.current().getBeanManager();

		// 指定されたAbstractWindowController派生クラスをCDI管理されたビーンとして生成する.
		Set<Bean<?>> beans = beanManager.getBeans(cls);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        AbstractWindowController window = (AbstractWindowController) beanManager.getReference(bean, cls,
				creationalContext);

		// 生成したビーンと、生成時のCreationContextを保存する.
		windowList.put(window, creationalContext);

		return window;
	}

	/**
	 * ウィンドウを閉じる.<br>
	 * すべてのウィンドウが閉じられた場合はアプリケーションを明示的に終了させる.<br>
	 */
	public void destroyWindow(AbstractWindowController window) {
		Objects.requireNonNull(window);

		// 閉じていなければ閉じる.
		window.closeWindow();
		unregister(window);
	}

	public void register(AbstractWindowController window) {
		Objects.requireNonNull(window);
		windowList.putIfAbsent(window, null);
	}

	public void unregister(AbstractWindowController window) {
		Objects.requireNonNull(window);

		CreationalContext<?> creationalContext = windowList.remove(window);
		if (creationalContext != null) {
			// CreationContextをReleaseする.
			// これにより@Dependentなインスタンスの@PreDestroyメソッドなどの呼び出しが行われる.
			creationalContext.release();
		}

		if (windowList.isEmpty()) {
			// すべてのウィンドウが閉じられている場合
			shutdown();
		}
	}

	/**
	 * アプリケーションを終了させる.
	 */
	protected void shutdown() {
		Platform.exit();
	}
}
