package nak.nakloidGUI.actions.options;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.VocalOption;

public class VocalOptionAction extends AbstractAction {
	public VocalOptionAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("ボーカル設定(&V)");
	}

	@Override
	public void run() {
		if (coreData.getVoicesSize() > 0) {
			VocalOption dialog = new VocalOption(mainWindow.getShell(), coreData);
			dialog.open();
		}
	}
}
