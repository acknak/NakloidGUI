package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayVerticalZoomInAction extends AbstractAction {
	public DisplayVerticalZoomInAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("垂直方向を拡大@Ctrl+Up");
		setAccelerator(SWT.CTRL | SWT.ARROW_UP);
	}
	@Override
	public void run() {
		mainWindow.setVerticalScale(mainWindow.getVerticalScale()+2.0);
	}
}
