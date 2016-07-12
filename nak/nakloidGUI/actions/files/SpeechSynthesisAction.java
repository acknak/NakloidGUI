package nak.nakloidGUI.actions.files;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.SpeechSynthesisForm;

public class SpeechSynthesisAction extends AbstractAction {

	public SpeechSynthesisAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("話声合成用ファイルをインポート");
	}

	@Override
	public void run() {
		if (!mainWindow.showSaveConfirmDialog()) {
			return;
		}
		mainWindow.flushLoggerWindow();
		SpeechSynthesisForm dialog = new SpeechSynthesisForm(mainWindow.getShell(), coreData);
		dialog.open();
	}
}
