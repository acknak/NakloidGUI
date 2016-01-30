package nak.nakloidGUI.actions.files;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

public class ExportVocalAction extends AbstractAction {
	final String[] ext = {"*.zip"};
	final String [] filterNames = {"ZIP (*.zip)"};

	public ExportVocalAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("ボーカルをエクスポート");
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
		Path pathExportVocal = Paths.get(strPath);

		System.out.println("ボーカルのエクスポートを開始します...");
		List<File> vocalFiles = null;
		try(Stream<Path> is = Files.walk(coreData.nakloidIni.input.path_singer)) {
			vocalFiles = is.map(Path::toFile).filter(File::isFile).collect(Collectors.toList());
		} catch (IOException e) {
			return;
		}
		int numParentDirectory = coreData.nakloidIni.input.path_singer.toString().length();
		try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(pathExportVocal.toFile())), Charset.forName("Shift_JIS"))) {
			zos.setLevel(0);
			byte[] buf = new byte[1024];
			for (File vocalFile : vocalFiles) {
				System.out.println(vocalFile.toString());
				ZipEntry entry = new ZipEntry(vocalFile.toString().substring(numParentDirectory));
				zos.putNextEntry(entry);
				try (FileInputStream fis = new FileInputStream(vocalFile)) {
					int len = 0;
					while ((len = fis.read(buf)) != -1) {
						zos.write(buf, 0, len);
					}
				}
			}
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"ボーカルのエクスポートに失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
			return;
		}
		MessageDialog.openInformation(mainWindow.getShell(), "NakloidGUI", (coreData.getVocalInfo()==null)?"ボーカル":coreData.getVocalInfo().getName()+"をエクスポートしました。");
	}
}
