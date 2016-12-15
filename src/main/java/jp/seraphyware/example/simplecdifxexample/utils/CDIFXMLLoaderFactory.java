package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.provider.BeanProvider;

import javafx.fxml.FXMLLoader;

@ApplicationScoped
public class CDIFXMLLoaderFactory {

	/**
	 * メッセージリソース定義
	 */
	@Inject
	@CDIFXMLLoader
	private ResourceBundle resources;

	/**
	 * リソースとCDIインスタンスと関連づけられたFXMLLoaderを作成して返す.
	 *
	 * @return
	 */
	@Produces
	@Dependent
	@CDIFXMLLoader
	public FXMLLoader createLoader(InjectionPoint ip) {
		FXMLLoader ldr = new FXMLLoader();
		ldr.setResources(resources);
		ldr.setControllerFactory(cls -> {

			// FXML内でコントローラクラス名が指定されている場合、
			// もしくは子FXMLでコントローラクラスが指定されている場合、
			// コントローラクラスのインスタンスを作成する場合に
			// CDI経由でインスタンスを取得する.
			return BeanProvider.getContextualReference(cls, false);
		});
		return ldr;
	}
}
