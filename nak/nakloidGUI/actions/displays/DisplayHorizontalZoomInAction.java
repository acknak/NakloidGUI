package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
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
		double baseRate = Math.pow(NakloidGUI.preferenceStore.getInt("gui.mainWindow.baseMsByPixel"), 1/5.0);
		mainWindow.setHorizontalScale(mainWindow.getHorizontalScale()*baseRate);
	}
}
