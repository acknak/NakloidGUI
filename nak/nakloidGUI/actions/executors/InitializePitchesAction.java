package nak.nakloidGUI.actions.executors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

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
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"ピッチ情報の削除に失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
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
			tmpCoreData.nakloidIni.input.path_input_pitches = null;
			tmpCoreData.nakloidIni.output.path_output_pitches = tmpInputPath;
			tmpCoreData.nakloidIni.input.pitches_mode = NakloidIni.PitchesMode.pitches_mode_none;
			tmpCoreData.synthesize(new CoreDataSynthesisListener() {
				@Override
				public void synthesisFinished() {
					try {
						coreData.reloadPitches();
					} catch (IOException e) {
						ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
								"ピッチ情報の読み込みに失敗しました。",
								new MultiStatus(".", IStatus.ERROR,
										Stream.of(e.getStackTrace())
												.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
												.collect(Collectors.toList()).toArray(new Status[]{}),
										e.getLocalizedMessage(), e));
					}
					coreData.reloadSongWaveform();
				}
			});
		} catch (IOException e) {
			ErrorDialog.openError(mainWindow.getShell(), "NakloidGUI",
					"楽譜情報の読み込みに失敗又は歌声合成のファイルの入出力時にエラーが発生しました。\ntemporaryフォルダ及びNakloid.iniに書き込み権限があるか確認してください。",
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
