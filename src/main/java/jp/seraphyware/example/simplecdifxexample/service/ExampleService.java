package jp.seraphyware.example.simplecdifxexample.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import jp.seraphyware.example.simplecdifxexample.ex2.Bar;

@ApplicationScoped
public class ExampleService {

	@FunctionalInterface
	public interface ProgressCallback {

		void progress(long cur, long max);
	}

	@Inject
	private Bar bar;

	public long fetchCurrentValue(ProgressCallback callback) {
		if ((Math.random() * 1000) > 900) {
			throw new RuntimeException("Dummy Error!!");
		}

		// ※ 実験用
		bar.test();

		try {
			long max = (long)(1000 + Math.random() * 10000);
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
