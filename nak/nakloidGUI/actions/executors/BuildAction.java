package nak.nakloidGUI.actions.executors;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
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
				coreData.synthesize();
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
			} finally {
				coreData.nakloidIni.input.pitches_mode = NakloidIni.PitchesMode.pitches_mode_pit;
				coreData.nakloidIni.output.path_output_pitches = null;
			}
		} else {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "音符が見つからないため歌声合成ができません。");
		}
	}
}
