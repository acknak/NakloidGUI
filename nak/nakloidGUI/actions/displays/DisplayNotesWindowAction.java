package nak.nakloidGUI.actions.displays;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class DisplayNotesWindowAction extends AbstractAction {
	public DisplayNotesWindowAction(MainWindow mainWindow, CoreData coreData) {
		super("CHECKBOX_ACTION", Action.AS_CHECK_BOX, mainWindow, coreData);
		setText("音符一覧を表示(&L)@F4");
		setAccelerator(SWT.F4);
		setChecked(mainWindow.displayingNotesWindow());
	}
	@Override
	public void run() {
		NakloidGUI.preferenceStore.setValue("gui.mainWindow.displayNotesWindow", isChecked());
		mainWindow.displayNotesWindow(isChecked());
	}
}
