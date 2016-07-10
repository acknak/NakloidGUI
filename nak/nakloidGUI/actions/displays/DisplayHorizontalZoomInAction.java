package nak.nakloidGUI.actions.displays;

import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayHorizontalZoomInAction extends AbstractAction {
	private final double baseRate = Math.pow(NakloidGUI.preferenceStore.getDouble("gui.mainWindow.msByPixelUpperLimit")/NakloidGUI.preferenceStore.getDouble("gui.mainWindow.msByPixelLowerLimit"), 1/10.0);
	public DisplayHorizontalZoomInAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("水平方向を拡大@Ctrl+Right");
		setAccelerator(SWT.CTRL | SWT.ARROW_RIGHT);
	}
	@Override
	public void run() {
		mainWindow.setHorizontalScale(1.0/baseRate);
	}
}
