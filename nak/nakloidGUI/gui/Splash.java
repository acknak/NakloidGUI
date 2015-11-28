package nak.nakloidGUI.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class Splash extends Composite {
	Label lblLoadText;

	public Splash(Shell parent, Image imgLoad) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));

		Label lblLoadImage = new Label(this, SWT.NONE);
		lblLoadImage.setImage(imgLoad);

		lblLoadText = new Label(this, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		lblLoadText.setLayoutData(gd);
		parent.pack();

		Display display = Display.getCurrent();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle rectShell = parent.getBounds();
		Rectangle rectMonitor = primary.getBounds();
		parent.setLocation(rectMonitor.x+(rectMonitor.width-rectShell.width)/2, rectMonitor.y+(rectMonitor.height-rectShell.height)/2);
	}

	public void setText(String text) {
		lblLoadText.setText("NakloidGUI: " + text);
	}
}
