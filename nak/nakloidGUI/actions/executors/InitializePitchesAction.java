package nak.nakloidGUI.actions.executors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.MessageDialog;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSynthesisListener;
import nak.nakloidGUI.coredata.NakloidIni;
import nak.nakloidGUI.gui.MainWindow;

public class InitializePitchesAction extends AbstractAction {
	public InitializePitchesAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("ピッチ情報の再計算");
	}

	@Override
	public void run() {
		final Path tmpInputPath = coreData.nakloidIni.input.path_input_pitches;
		try {
			Files.deleteIfExists(tmpInputPath);
		} catch (IOException e) {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ピッチ情報の削除に失敗しました。"+e.getMessage());
			return;
		}
		if (coreData.getSongWaveform() != null) {
			coreData.getSongWaveform().pause();
			coreData.getSongWaveform().close();
		}
		mainWindow.showWaveformStatus("歌声合成中...");
		mainWindow.flushLoggerWindow();
		try {
			CoreData tmpCoreData = new CoreData.Builder()
					.loadScore(coreData.getScorePath())
					.loadOtoIni(coreData.getVocalPath())
					.build();
			tmpCoreData.idling(true);
			tmpCoreData.nakloidIni.input.path_input_pitches = null;
			tmpCoreData.nakloidIni.output.path_output_pitches = tmpInputPath;
			tmpCoreData.nakloidIni.input.pitches_mode = NakloidIni.PitchesMode.pitches_mode_none;
			tmpCoreData.synthesize(new CoreDataSynthesisListener() {
				@Override
				public void finishSynthesis() {
					try {
						coreData.reloadPitches();
					} catch (IOException e) {
						MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ピッチ情報の読込に失敗しました。\n"+e.getMessage());
					}
					coreData.reloadSongWaveform();
				}
			});
		} catch (IOException e1) {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ファイル入出力に失敗しました。\n"+e1.getMessage());
		} catch (InterruptedException e2) {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "Nakloidにエラーが発生しました。\n"+e2.getMessage());
		}
	}
}
