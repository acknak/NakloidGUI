package nak.nakloidGUI.actions.files;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class SaveAsAction extends AbstractAction {
	final String[] ext = {"*.nar"};
	final String [] filterNames = {"Nakloid Archive (*.nar)"};

	public SaveAsAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("名前を付けて保存@Shift+Ctrl+S");
		setAccelerator(SWT.CTRL+'A');
	}

	@Override
	public void run() {
		FileDialog openDialog = new FileDialog(mainWindow.getShell(), SWT.SAVE);
		openDialog.setFilterExtensions(ext);
		openDialog.setFilterNames(filterNames);
		String strPath = openDialog.open();
		if (strPath==null || strPath.isEmpty()) {
			return;
		}
		NakloidGUI.preferenceStore.setValue("workspace.path_nar", strPath);
		try {
			NakloidGUI.preferenceStore.save();
		} catch (IOException e) {}
		mainWindow.saveAction.run();
	}
}
