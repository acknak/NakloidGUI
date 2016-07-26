package nak.nakloidGUI.actions.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSynthesisListener;
import nak.nakloidGUI.gui.MainWindow;

public class OpenAction extends AbstractAction {
	final String[] ext = {"*.nar"};
	final String [] filterNames = {"Nakloid Archive (*.nar)"};

	public OpenAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("開く@Ctrl+O");
		setAccelerator(SWT.CTRL+'O');
	}

	@Override
	public void run() {
		if (!mainWindow.showSaveConfirmDialog()) {
			return;
		}
		FileDialog openDialog = new FileDialog(mainWindow.getShell(), SWT.SAVE);
		openDialog.setFilterExtensions(ext);
		openDialog.setFilterNames(filterNames);
		String strPath = openDialog.open();
		if (strPath!=null && !strPath.isEmpty()) {
			try {
				open(strPath);
			} catch (IOException e) {
				ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
						"narファイルの展開に失敗しました。",
						new MultiStatus(".", IStatus.ERROR,
								Stream.of(e.getStackTrace())
										.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
										.collect(Collectors.toList()).toArray(new Status[]{}),
								e.getLocalizedMessage(), e));
			}
			try {
				coreData.reloadScoreAndPitches();
				coreData.saveScore();
				coreData.synthesize(new CoreDataSynthesisListener() {
					@Override
					public void synthesisFinished() {
						mainWindow.updateSongWaveform();
					}
				});
			} catch (IOException e) {
				ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
						"歌声合成のファイルの入出力時にエラーが発生しました。\ntemporaryフォルダ及びNakloid.iniに書き込み権限があるか確認してください。",
						new MultiStatus(".", IStatus.ERROR,
								Stream.of(e.getStackTrace())
										.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
										.collect(Collectors.toList()).toArray(new Status[]{}),
								e.getLocalizedMessage(), e));
			} catch (InterruptedException e) {
				ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
						"歌声合成中にスレッドが中断されました。",
						new MultiStatus(".", IStatus.ERROR,
								Stream.of(e.getStackTrace())
										.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
										.collect(Collectors.toList()).toArray(new Status[]{}),
								e.getLocalizedMessage(), e));
			}
		}
	}

	static public void open(String strPath) throws IOException {
		NakloidGUI.preferenceStore.setValue("workspace.path_nar", strPath);
		try (ZipFile zipFile = new ZipFile(strPath, Charset.forName("Shift_JIS"))) {
			zipFile.stream()
				.filter(entry -> {
					String tmpFilename = Paths.get(entry.getName()).getFileName().toString();
					return tmpFilename.equals("pitches.pit")||tmpFilename.equals("score.nak");
				}).forEach(entry -> {
					try (InputStream is = zipFile.getInputStream(entry)) {
						File tmpFile = Paths.get("temporary", Paths.get(entry.getName()).getFileName().toString()).toFile();
						tmpFile.getParentFile().mkdirs();
						try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
							int size = 0;
							byte[] buf = new byte[1024];
							while ((size=is.read(buf)) != -1) {
								fos.write(buf, 0, size);
							}
							fos.flush();
						}
					} catch (Exception e) {}
				});
		}
	}
}
