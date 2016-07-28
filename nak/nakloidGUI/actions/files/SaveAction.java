package nak.nakloidGUI.actions.files;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class SaveAction extends AbstractAction {
	final String[] ext = {"*.nar"};
	final String [] filterNames = {"Nakloid Archive (*.nar)"};

	public SaveAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("保存@Ctrl+S");
		setAccelerator(SWT.CTRL+'S');
	}

	@Override
	public void run() {
		String strPath = NakloidGUI.preferenceStore.getString("workspace.path_nar");
		if (strPath==null || strPath.isEmpty()) {
			mainWindow.saveAsAction.run();
			return;
		}
		try {
			coreData.saveScore();
			coreData.savePitches();
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"一時保存のファイル入出力に失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
			return;
		}
		try {
			coreData.saveNar(Paths.get(strPath));
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"保存時のファイル入出力に失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		}
	}
}
