package jp.seraphyware.example.simplecdifxexample.ex2;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class Bar {

	@Inject
	@FooParameter("baz") // ← Foo構築時にパラメータを渡せる
	private Foo foo;

	public void test() {
		System.out.println(foo.getArg());
	}
}

