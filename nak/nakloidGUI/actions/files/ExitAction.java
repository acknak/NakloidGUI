package nak.nakloidGUI.actions.files;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class ExitAction extends AbstractAction {
	public ExitAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("終了(&W)@Ctrl+W");
		setAccelerator(SWT.CTRL + 'W');
	}

	@Override
	public void run() {
		if (MessageDialog.openQuestion(mainWindow.getShell(), "NakloidGUI", "本当に終了してもよろしいですか？")) {
			mainWindow.getShell().close();
		}
	}
}
