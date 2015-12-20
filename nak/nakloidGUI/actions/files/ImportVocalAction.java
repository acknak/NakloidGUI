package nak.nakloidGUI.actions.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipFile;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSynthesisListener;
import nak.nakloidGUI.gui.MainWindow;

public class ImportVocalAction extends AbstractAction {
	final String[] ext = {"*.uar;*.zip"};
	final String [] filterNames = {"Vocal Install Archive (*.uar, *.zip)"};

	public ImportVocalAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("ボーカルをインポート");
	}

	@Override
	public void run() {
		if (MessageDialog.openQuestion(mainWindow.getShell(), "NakloidGUI",
				"現在使用しているボーカルの設定情報等は削除されます。ボーカルの設定情報等を保存する場合は、この画面を一旦閉じて、ボーカルをエクスポートして下さい。\n"
				+ "\n"
				+ "新たにボーカルをインポートしてもよろしいですか？")) {
			FileDialog openDialog = new FileDialog(mainWindow.getShell(), SWT.OPEN);
			openDialog.setFilterExtensions(ext);
			openDialog.setFilterNames(filterNames);
			String strPath = openDialog.open();
			if (strPath==null || strPath.isEmpty()) {
				return;
			}
			Path pathVocal = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_singer"));

			mainWindow.flushLoggerWindow();
			System.out.println("ボーカルのインポートを開始します...");
			try {
				Files.walkFileTree(pathVocal, new SimpleFileVisitor<Path>(){
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch(IOException e){}
			try (ZipFile zipFile = new ZipFile(strPath, Charset.forName("Shift_JIS"))) {
				zipFile.stream().forEach(entry -> {
					try (InputStream is = zipFile.getInputStream(entry)) {
						File tmpFile = Paths.get(pathVocal.toString(), entry.getName()).toFile();
						tmpFile.getParentFile().mkdirs();
						if (entry.getName().lastIndexOf(".") > 0) {
							try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
								int size = 0;
								byte[] buf = new byte[1024];
								while ((size=is.read(buf)) != -1) {
									fos.write(buf, 0, size);
								}
								fos.flush();
							}
							System.out.println(entry.getName());
						}
					} catch (IOException e) {
						MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", entry.getName()+"の展開に失敗しました");
						return;
					}
				});
			} catch (IOException e) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ファイルの展開に失敗しました。\n"+e.toString()+e.getMessage());
				return;
			}
			try {
				if (MessageDialog.openQuestion(mainWindow.getShell(), "Nakloid GUI", "ピッチマークファイルを新規に生成しますか？")) {
					coreData.makeAllPmp(new CoreDataSynthesisListener() {
						@Override
						public void synthesisFinished() {
							MessageDialog.openInformation(mainWindow.getShell(), "NakloidGUI", strPath+"をインポートしました。");
						}
					});
				} else {
					MessageDialog.openInformation(mainWindow.getShell(), "NakloidGUI", strPath+"をインポートしました。");
				}
				coreData.reloadVocal();
			} catch (IOException e) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ボーカルの読み込みに失敗しました。\n"+e.toString()+e.getMessage());
				return;
			}
		}
	}
}
