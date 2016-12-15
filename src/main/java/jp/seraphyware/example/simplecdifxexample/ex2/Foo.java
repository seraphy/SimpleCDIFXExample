package jp.seraphyware.example.simplecdifxexample.ex2;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class Foo {

	@Inject
	private FooBar fooBar;

	@Inject
	private AnyService srv;

	private String arg;

	public void setArg(String arg) {
		this.arg = arg;
	}

	public String getArg() {
		System.out.println(srv.toString());
		return arg;
	}

	@Override
	public String toString() {
		return super.toString() + "{args: '" + arg + "', fooBar:" + fooBar + "}";
	}
}
