package jp.seraphyware.example.simplecdifxexample.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * XMLプロパティ形式のリソースバンドルを読み込み可能にするコントローラ.<br>
 * リソース名はロケールおよびOSの種類によって識別されます.<br>
 * ロケールによる、標準のリソースバンドルの形式のサフィックスに加えて、
 * Macの場合は「_osx」、Windowsの場合は「_windows」のサフィックスが追加で検索され、
 * 該当のプロパティファイルがある場合は、サフィックスなしのものとマージされた結果として返されます.<br>
 */
public class XMLResourceBundleControl extends ResourceBundle.Control {

	private final Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * 拡張子
	 */
	private static final String XML = "xml";

	/**
	 * OS名
	 */
	private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

	/**
	 * このコントローラが対象する拡張子の一覧を返す. "xml"だけが有効である.
	 *
	 * @param baseName
	 *            ベース名
	 * @return 対象となる拡張子一覧
	 */
	@Override
	public List<String> getFormats(String baseName) {
		return Collections.singletonList(XML);
	}

	/**
	 * 新しいリソースバンドルを生成して返す. xml形式のプロパティファイルから現在のロケールに従って取得したリソースバンドルを返す.
	 * ただし、リソースはOSがmacまたはWindowsの場合は「_osx」または「_windows」のサフィックスをつけた
	 * XMLプロパティファイルが存在すれば、それを重ねて読み込んだものが返されます.<br>
	 *
	 * @return リソースバンドル
	 */
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		if (!XML.equals(format)) {
			return null;
		}

		// ロケールと結合したリソース名を求める
		String plainBundleName = toBundleName(baseName, locale);

		// osの種類(win/macごとの違いのリソースも試行するように)
		ArrayList<String> bundleNames = new ArrayList<>();
		bundleNames.add(plainBundleName);
		if (OS_NAME.indexOf("windows") > -1) {
			bundleNames.add(plainBundleName + "_windows");
		} else if (OS_NAME.indexOf("os x") > -1) {
			bundleNames.add(plainBundleName + "_osx");
		}

		// XMLプロパティを重ねてロードする.
		Properties props = new Properties();
		int numOfLoadedProps = 0;
		for (String bundleName : bundleNames) {
			// 対応するフォーマットと結合したリソース名を求める
			String resourceName = toResourceName(bundleName, format);
			logger.info("resourceName=" + resourceName);

			URL url = loader.getResource(resourceName);

			// XMLプロパティを上書きロードする.
			if (url != null) {
				props.loadFromXML(url.openStream());
				numOfLoadedProps++;
			}
		}

		if (numOfLoadedProps > 0) {
			// 少なくとも、どちらかのXMLプロパティが読み込めた場合、
			// XMLプロパティをリソースバンドルに接続する.
			return new ResourceBundle() {
				@Override
				protected Object handleGetObject(String key) {
					return props.getProperty(key);
				}

				@SuppressWarnings("unchecked")
				@Override
				public Enumeration<String> getKeys() {
					return (Enumeration<String>) props.propertyNames();
				}
			};
		}

		// ロードできず.
		return null;
	}
}
