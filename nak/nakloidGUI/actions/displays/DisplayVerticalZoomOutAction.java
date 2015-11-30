package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayVerticalZoomOutAction extends AbstractAction {
	public DisplayVerticalZoomOutAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("垂直方向を縮小@Ctrl+Down");
		setAccelerator(SWT.CTRL | SWT.ARROW_DOWN);
	}
	@Override
	public void run() {
		double baseRate = Math.pow(NakloidGUI.preferenceStore.getInt("gui.mainWindow.baseMsByPixel"), 1/5.0);
		mainWindow.setVerticalScale(mainWindow.getVerticalScale()/baseRate);
	}
}
