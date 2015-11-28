package nak.nakloidGUI.actions.options;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.NakloidOption;

public class NakloidOptionAction extends AbstractAction {
	public NakloidOptionAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("設定(&P)@Ctrl+P");
		setAccelerator(SWT.CTRL + 'P');
	}

	@Override
	public void run() {
		NakloidOption nakloidGuiPreferenceDialog = new NakloidOption(mainWindow.getShell(), new PreferenceManager());
		nakloidGuiPreferenceDialog.open();
		mainWindow.preferenceReloaded();
	}
}
