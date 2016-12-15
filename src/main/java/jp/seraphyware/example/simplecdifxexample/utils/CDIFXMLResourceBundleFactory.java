package jp.seraphyware.example.simplecdifxexample.utils;

import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class CDIFXMLResourceBundleFactory {

	@Produces
	@CDIFXMLLoader
	@Dependent
	public ResourceBundle getMessages() {
		return ResourceBundle.getBundle("messages", //$NON-NLS-1$
				new XMLResourceBundleControl());
	}
}
