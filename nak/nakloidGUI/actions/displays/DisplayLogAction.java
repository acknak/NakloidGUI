package nak.nakloidGUI.actions.displays;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayLogAction extends AbstractAction {
	public DisplayLogAction(MainWindow mainWindow, CoreData coreData) {
		super("CHECKBOX_ACTION", Action.AS_CHECK_BOX, mainWindow, coreData);
		setText("ログを表示(&L)@F3");
		setAccelerator(SWT.F3);
		setChecked(mainWindow.displayingLog());
	}
	@Override
	public void run() {
		mainWindow.displayLog(isChecked());
	}
}
