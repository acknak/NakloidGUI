package nak.nakloidGUI.actions.files;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
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
		String strPath;
		while (true) {
			strPath = openDialog.open();
			if (strPath==null || strPath.isEmpty()) {
				return;
			}
			File tmpFile = new File(strPath);
			if (!tmpFile.exists() || MessageDialog.openConfirm(mainWindow.getShell(), "NakloidGUI", "選択されたファイルは既に存在します。上書きしますか？")) {
				break;
			}
		}
		NakloidGUI.preferenceStore.setValue("workspace.path_nar", strPath);
		try {
			NakloidGUI.preferenceStore.save();
		} catch (IOException e) {}
		mainWindow.saveAction.run();
	}
}
