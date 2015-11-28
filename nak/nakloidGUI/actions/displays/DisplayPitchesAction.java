package nak.nakloidGUI.actions.displays;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.MainWindow.MainWindowDisplayMode;

public class DisplayPitchesAction extends AbstractAction {
	public DisplayPitchesAction(MainWindow mainWindow, CoreData coreData) {
		super("CHECKBOX_ACTION", Action.AS_RADIO_BUTTON, mainWindow, coreData);
		setText("ピッチ情報を表示(&P)@F2");
		setAccelerator(SWT.F2);
		setChecked(mainWindow.getDisplayMode()==MainWindowDisplayMode.PITCHES);
	}
	@Override
	public void run() {
		mainWindow.displayPitches();
	}
}
