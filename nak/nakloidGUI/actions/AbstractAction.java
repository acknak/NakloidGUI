package nak.nakloidGUI.actions;

import org.eclipse.jface.action.Action;

import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class AbstractAction extends Action {
	protected MainWindow mainWindow;
	protected nak.nakloidGUI.coredata.CoreData coreData;

	public AbstractAction(MainWindow mainWindow, CoreData coreData) {
		this.mainWindow = mainWindow;
		this.coreData = coreData;
	}

	public AbstractAction(String text, int style, MainWindow mainWindow, CoreData coreData) {
		super(text, style);
		this.mainWindow = mainWindow;
		this.coreData = coreData;
	}

	@Override
	public void run() {}
}
