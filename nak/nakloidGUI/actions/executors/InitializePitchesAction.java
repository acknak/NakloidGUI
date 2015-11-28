package nak.nakloidGUI.actions.executors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.MessageDialog;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class InitializePitchesAction extends AbstractAction {
	public InitializePitchesAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("ピッチ情報の再計算");
	}

	@Override
	public void run() {
		final Path tmpInputPath = coreData.nakloidIni.input.path_input_pitches;
		final Path tmpOutputPath = coreData.nakloidIni.output.path_output_pitches;
		coreData.nakloidIni.input.path_input_pitches = null;
		coreData.nakloidIni.output.path_output_pitches = tmpInputPath;
		try {
			Files.deleteIfExists(tmpInputPath);
		} catch (IOException e) {
			MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "ピッチ情報の削除に失敗しました。"+e.getMessage());
		}
		mainWindow.buildAction.run();
		coreData.nakloidIni.input.path_input_pitches = tmpInputPath;
		coreData.nakloidIni.output.path_output_pitches = tmpOutputPath;
	}
}
