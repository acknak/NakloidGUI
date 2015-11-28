package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayHorizontalZoomInAction extends AbstractAction {
	public DisplayHorizontalZoomInAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("水平方向を拡大@Ctrl+Right");
		setAccelerator(SWT.CTRL | SWT.ARROW_RIGHT);
	}
	@Override
	public void run() {
		mainWindow.setHorizontalScale(mainWindow.getHorizontalScale()-0.2);
	}
}
