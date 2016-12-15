package jp.seraphyware.example.simplecdifxexample.ui;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import javafx.fxml.FXMLLoader;
import jp.seraphyware.example.simplecdifxexample.utils.CDIFXMLLoader;
import jp.seraphyware.example.simplecdifxexample.utils.FXMLWindowController;

public abstract class AbstractFXMLWindowController extends AbstractWindowController {

	@Inject
	@CDIFXMLLoader
	private Instance<FXMLLoader> ldrProvider;

	@Override
	protected void makeRoot() {
		FXMLWindowController annt = getClass().getAnnotation(FXMLWindowController.class);
		if (annt == null) {
			throw new IllegalArgumentException("FXMLWindowController annotation must be specified.");
		}
		String fxmlName = annt.value();

		FXMLLoader ldr = ldrProvider.get();
		try {
			ldr.setController(this);
			ldr.setLocation(getClass().getResource(fxmlName));
			setRoot(ldr.load());

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} finally {
			ldrProvider.destroy(ldr);
		}

	}
}
