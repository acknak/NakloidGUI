package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayZoomOutAction extends AbstractAction {
	public DisplayZoomOutAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("縮小@Alt+Shift+Z");
		setAccelerator(SWT.ALT + SWT.SHIFT + 'Z');
	}
	@Override
	public void run() {
		mainWindow.displayHorizontalZoomOutAction.run();
		mainWindow.displayVerticalZoomOutAction.run();
	}
}
