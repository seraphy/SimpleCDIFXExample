package jp.seraphyware.example.simplecdifxexample.ex2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;

@Dependent
public class FooBar {

	@PostConstruct
	public void postCtor() {
		System.out.println(toString() + "#ctor");
	}

	@PreDestroy
	public void preDtor() {
		System.out.println(toString() + "#dtor");
	}

}
