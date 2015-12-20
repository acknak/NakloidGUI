package nak.nakloidGUI.actions.options;

import org.eclipse.jface.dialogs.MessageDialog;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class AboutNakloidAction extends AbstractAction {
	public AboutNakloidAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("&Nakloidについて(&A)");
	}

	@Override
	public void run() {
		 MessageDialog.openInformation(mainWindow.getShell(), "Nakloidについて", "Nakloid (GUI) ver.151220\n"
		 	+ "https://github.com/acknak/Nakloid/\n\n"
		 	+ "this software includes the work that is distributed in the Apache License 2.0\n\n"
		 	+ "Library List (Nakloid)\n"
		 	+ "・Boost C++ Libraries\n"
		 	+ "・FFTSS\n"
		 	+ "・RapidJSON\n\n"
		 	+ "Library List (NakloidGUI)\n"
		 	+ "・SWT/JFace\n"
		 	+ "・Jackson\n\n"
		 	+ "バグ報告・感想等は acknak39@gmail.com か @acknak39 まで");
	}
}
