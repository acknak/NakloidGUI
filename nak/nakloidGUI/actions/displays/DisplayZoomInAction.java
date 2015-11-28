package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayZoomInAction extends AbstractAction {
	public DisplayZoomInAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("拡大@Alt+Z");
		setAccelerator(SWT.ALT + 'Z');
	}
	@Override
	public void run() {
		mainWindow.displayHorizontalZoomInAction.run();
		mainWindow.displayVerticalZoomInAction.run();
	}
}