package nak.nakloidGUI.actions.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSynthesisListener;
import nak.nakloidGUI.coredata.NakloidIni;
import nak.nakloidGUI.coredata.NakloidIni.ScoreMode;
import nak.nakloidGUI.gui.MainWindow;

public class ImportScoreAction extends AbstractAction {
	final private static String[] scoreExt = {"*.ust;*.smf;*.mid","*.ust","*.smf;*.mid"};
	final private static String[] lyricsExt = {"*.txt"};
	final private static String [] scoreFilterNames = {"インポート可能な楽譜形式 (*.ust, *.smf, *.mid)", "UTAU Sequence Text (*.ust)", "Standard MIDI File (*.smf, *.mid)"};
	final private static String [] lyricsFilterNames = {"コンマ区切り歌詞ファイル (*.txt)"};

	public ImportScoreAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("楽譜をインポート");
	}

	@Override
	public void run() {
		if (coreData.getVoicesSize() < 1) {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "歌声情報が読み込まれていません。先にボーカルをインポートして下さい。");
			return;
		}
		if (!mainWindow.showSaveConfirmDialog()) {
			return;
		}
		FileDialog openScoreDialog = new FileDialog(mainWindow.getShell(), SWT.OPEN);
		openScoreDialog.setFilterExtensions(scoreExt);
		openScoreDialog.setFilterNames(scoreFilterNames);
		String strScorePath = openScoreDialog.open();
		if (strScorePath==null || strScorePath.isEmpty()) {
			return;
		}
		Path pathImportScore = Paths.get(strScorePath);
		try {
			CoreData tmpCoreData = new CoreData.Builder()
					.loadOtoIni(coreData.getVocalPath())
					.build();
			tmpCoreData.nakloidIni.input.path_input_score = pathImportScore;
			if (strScorePath.endsWith(".ust")) {
				tmpCoreData.nakloidIni.input.score_mode = ScoreMode.score_mode_ust;
			} else {
				tmpCoreData.nakloidIni.input.score_mode = ScoreMode.score_mode_smf;
				FileDialog openSmfDialog = new FileDialog(mainWindow.getShell(), SWT.OPEN);
				openSmfDialog.setFilterExtensions(lyricsExt);
				openSmfDialog.setFilterNames(lyricsFilterNames);
				String strLyricsPath = openSmfDialog.open();
				if (strLyricsPath==null || strLyricsPath.isEmpty()) {
					return;
				}
				tmpCoreData.nakloidIni.input.path_lyrics = Paths.get(strLyricsPath);
			}
			tmpCoreData.nakloidIni.input.path_input_pitches = null;
			tmpCoreData.nakloidIni.input.pitches_mode = NakloidIni.PitchesMode.pitches_mode_none;
			tmpCoreData.nakloidIni.output.path_song = null;
			tmpCoreData.nakloidIni.output.path_output_score = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_score"));
			tmpCoreData.nakloidIni.output.path_output_pitches = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_pitches"));
			Files.deleteIfExists(coreData.nakloidIni.input.path_input_score);
			Files.deleteIfExists(coreData.nakloidIni.input.path_input_pitches);
			tmpCoreData.synthesize(new CoreDataSynthesisListener() {
				@Override
				public void synthesisFinished() {
					try {
						if (!coreData.nakloidIni.input.path_input_score.toFile().exists() || !coreData.nakloidIni.input.path_input_score.toFile().exists()) {
							MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "楽譜情報の読み込みに失敗しました。ログを確認して下さい。");
							return;
						}
						coreData.reloadScoreAndPitches();
						coreData.synthesize();
						mainWindow.updateSongWaveform();
					} catch (IOException e) {
						ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
								"楽譜情報の読み込みに失敗しました。",
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
			});
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"Nakloid用ファイルの入出力時にエラーが発生しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		} catch (InterruptedException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"Nakloid用ファイル合成中にスレッドが中断されました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		}
	}
}
