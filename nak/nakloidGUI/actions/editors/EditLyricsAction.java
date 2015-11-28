package nak.nakloidGUI.actions.editors;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.LyricsEditor;
import nak.nakloidGUI.gui.MainWindow;

public class EditLyricsAction extends AbstractAction {
	public EditLyricsAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("歌詞の編集");
	}

	@Override
	public void run() {
		LyricsEditor lyricsEditor = new LyricsEditor(mainWindow.getShell(), coreData);
		lyricsEditor.open();
	}
}
