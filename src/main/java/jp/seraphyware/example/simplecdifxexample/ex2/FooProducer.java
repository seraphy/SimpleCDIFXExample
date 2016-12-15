package jp.seraphyware.example.simplecdifxexample.ex2;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.logging.Logger;

@ApplicationScoped
public class FooProducer {

	private static final Logger logger = Logger.getLogger(FooProducer.class.getName());

	@Inject
	@Default
	private Instance<Foo> fooProvider;

	@Dependent
	@Produces
	@FooParameter("")
	public Foo create(InjectionPoint ip) {
		FooParameter fooParam = ip.getAnnotated().getAnnotation(FooParameter.class);
		String arg = fooParam.value();

		// Defaultの@Dependentのインスタンスを生成してargを設定する.
		Foo inst = fooProvider.get();
		inst.setArg(arg);

		// ※ もしくはnewしてApache DeltaSpikeのBeanProvider.injectFieldsを使っても良い
		// BeanProvider.injectFields(inst)

		logger.info("☆create Foo instance with arg: " + arg);
		return inst;
	}

	public void dispose(@Disposes @FooParameter("") Foo inst) {
		logger.info("☆disposer called: " + inst);
		fooProvider.destroy(inst);
	}
}
