package nak.nakloidGUI.actions.files;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
		Path pathNar = Paths.get(strPath);
		Path pathScore = coreData.nakloidIni.input.path_input_score;
		Path pathPitches = coreData.nakloidIni.input.path_input_pitches;
		byte[] buf = new byte[1024];
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(pathNar.toFile()));
			zos.putNextEntry(new ZipEntry(pathScore.getFileName().toString()));
			try (InputStream is = new BufferedInputStream(new FileInputStream(pathScore.toFile()))) {
				while (true) {
					int len = is.read(buf);
					if (len < 0) break;
					zos.write(buf, 0, len);
				}
			}
			zos.putNextEntry(new ZipEntry(pathPitches.getFileName().toString()));
			try (InputStream is = new BufferedInputStream(new FileInputStream(pathPitches.toFile()))) {
				while (true) {
					int len = is.read(buf);
					if (len < 0) break;
					zos.write(buf, 0, len);
				}
			}
			NakloidGUI.preferenceStore.save();
			mainWindow.updateWindowName();
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"保存時のファイル入出力に失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		} finally {
			try {
				zos.close();
			} catch (IOException e) {}
		}
	}
}
