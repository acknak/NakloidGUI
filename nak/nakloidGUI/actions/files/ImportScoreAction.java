package nak.nakloidGUI.actions.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
	final private static String[] scoreExt = {"*.nak;*.ust;*.smf;*.mid","*.nak","*.ust","*.smf;*.mid"};
	final private static String[] lyricsExt = {"*.txt"};
	final private static String [] scoreFilterNames = {"インポート可能な楽譜形式 (*.nak, *.ust, *.smf, *.mid)", "Nakloid Score File (*.nak)", "UTAU Sequence Text (*.ust)", "Standard MIDI File (*.smf, *.mid)"};
	final private static String [] lyricsFilterNames = {"コンマ区切り歌詞ファイル (*.txt)"};

	public ImportScoreAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("楽譜をインポート");
	}

	@Override
	public void run() {
		FileDialog openScoreDialog = new FileDialog(mainWindow.getShell(), SWT.OPEN);
		openScoreDialog.setFilterExtensions(scoreExt);
		openScoreDialog.setFilterNames(scoreFilterNames);
		String strScorePath = openScoreDialog.open();
		if (strScorePath==null || strScorePath.isEmpty()) {
			return;
		}
		Path pathImportScore = Paths.get(strScorePath);
		if (!strScorePath.endsWith(".nak")) {
			try {
				CoreData tmpCoreData = new CoreData.Builder()
						.loadOtoIni(coreData.getVocalPath())
						.build();
				tmpCoreData.idling(true);
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
				tmpCoreData.nakloidIni.output.path_output_score = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_score"));
				tmpCoreData.nakloidIni.output.path_output_pitches = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_pitches"));
				Files.deleteIfExists(coreData.nakloidIni.input.path_input_pitches);
				tmpCoreData.synthesize(new CoreDataSynthesisListener() {
					@Override
					public void finishSynthesis() {
						try {
							coreData.reloadScoreAndPitches();
						} catch (IOException e) {
							MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "生成したファイルの読込に失敗しました。\n"+e.getMessage());
						}
						coreData.reloadSongWaveform();
					}
				});
			} catch (IOException e1) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ファイル入出力に失敗しました。\n"+e1.getMessage());
			} catch (InterruptedException e2) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "Nakloidにエラーが発生しました。\n"+e2.getMessage());
			}
		} else {
			try {
				Files.copy(pathImportScore, coreData.getScorePath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ファイルのコピーに失敗しました。\n"+e.getMessage());
			}
		}
	}
}
