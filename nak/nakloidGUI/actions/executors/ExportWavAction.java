package nak.nakloidGUI.actions.executors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class ExportWavAction extends AbstractAction {
	final String[] ext = {"*.wav"};
	final String [] filterNames = {"RIFF waveform Audio Format (*.wav)"};

	public ExportWavAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("書き出し(&E)@Ctrl+E");
		setAccelerator(SWT.CTRL+'E');
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
		mainWindow.buildAction.run();
		Path pathExportWav = Paths.get(strPath);
		Path pathWav = coreData.nakloidIni.output.path_song;
		try {
			Files.deleteIfExists(pathExportWav);
			Files.copy(pathWav, pathExportWav, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ファイル操作に失敗しました。\n"+e.toString()+e.getMessage());
		}
	}
}
