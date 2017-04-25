package jp.seraphyware.example.simplecdifxexample.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.ConfigProperty;

import jp.seraphyware.example.simplecdifxexample.ex2.Bar;

@ApplicationScoped
public class ExampleService {

	@FunctionalInterface
	public interface ProgressCallback {

		void progress(long cur, long max);
	}

	@Inject
	private Bar bar;

	// META-INF/apache-deltaspike.propertiesからプロパティを注入する.
	@Inject
	@ConfigProperty(name = "exampleService.loop.base", defaultValue = "999")
	private int base;

	// META-INF/apache-deltaspike.propertiesからプロパティを注入する.
	@Inject
	@ConfigProperty(name = "exampleService.loop.range", defaultValue = "9999")
	private int range;

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public long fetchCurrentValue(ProgressCallback callback) {
		if ((Math.random() * 1000) > 900) {
			throw new RuntimeException("Dummy Error!!");
		}

		// ※ 実験用
		bar.test();

		try {
			long max = (long)(base + Math.random() * range);
			long st = System.currentTimeMillis();

			for (;;) {
				long span = System.currentTimeMillis() - st;
				if (span >= max) {
					break;
				}

				if (callback != null) {
					callback.progress(span, max);
				}

				Thread.sleep(100);
			}
			callback.progress(max, max);

		} catch (InterruptedException ex) {
			// 無視する.
		}
		return System.currentTimeMillis();
	}
}
