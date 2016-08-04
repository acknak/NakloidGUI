package nak.nakloidGUI.actions.executors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
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
		mainWindow.buildAction.run();
		Path pathExportWav = Paths.get(strPath);
		Path pathWav = coreData.nakloidIni.output.path_song;
		try {
			Files.deleteIfExists(pathExportWav);
			Files.copy(pathWav, pathExportWav, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"wavファイル出力のためのファイル操作に失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		}
	}
}
