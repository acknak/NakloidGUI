package nak.nakloidGUI.actions.executors;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSynthesisListener;
import nak.nakloidGUI.coredata.NakloidIni;
import nak.nakloidGUI.gui.MainWindow;

public class BuildAction extends AbstractAction {
	public BuildAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("合成(&B)@Ctrl+B");
		setAccelerator(SWT.CTRL+'B');
	}

	@Override
	public void run() {
		if (coreData.getScoreLength() > 0) {
			if (coreData.getSongWaveform() != null) {
				coreData.getSongWaveform().pause();
				coreData.getSongWaveform().close();
			}
			mainWindow.showWaveformStatus("歌声合成中...");
			mainWindow.flushLoggerWindow();
			if (coreData.getPitches() == null) {
				coreData.nakloidIni.input.pitches_mode = NakloidIni.PitchesMode.pitches_mode_none;
				coreData.nakloidIni.output.path_output_pitches = Paths.get(NakloidGUI.preferenceStore.getString("ini.input.path_input_pitches"));
			}
			try {
				coreData.synthesize(new CoreDataSynthesisListener() {
					@Override
					public void synthesisFinished() {
						coreData.reloadSongWaveform();
					}
				});
			} catch (IOException e) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ファイルの入出力にエラーが発生しました。\n"+e.getMessage());
			} catch (InterruptedException e) {
				MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "Nakloidが中断されました。\n"+e.getMessage());
			} finally {
				coreData.nakloidIni.input.pitches_mode = NakloidIni.PitchesMode.pitches_mode_pit;
				coreData.nakloidIni.output.path_output_pitches = null;
			}
		}
	}
}
