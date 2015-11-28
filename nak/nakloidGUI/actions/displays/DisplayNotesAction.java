package nak.nakloidGUI.actions.displays;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.MainWindow.MainWindowDisplayMode;

public class DisplayNotesAction extends AbstractAction {
	public DisplayNotesAction(MainWindow mainWindow, CoreData coreData) {
		super("CHECKBOX_ACTION", Action.AS_RADIO_BUTTON, mainWindow, coreData);
		setText("音符を表示(&N)@F1");
		setAccelerator(SWT.F1);
		setChecked(mainWindow.getDisplayMode()==MainWindowDisplayMode.NOTES);
	}

	@Override
	public void run() {
		mainWindow.displayNotes();
	}
}
