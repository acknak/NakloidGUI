package nak.nakloidGUI.actions.editors;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.NoteOption;

public class AddNoteAction extends AbstractAction {
	public AddNoteAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("音符の追加");
	}

	@Override
	public void run() {
		NoteOption dialog = new NoteOption(mainWindow.getShell(), coreData, coreData.getNewNote());
		dialog.open();
	}
}
