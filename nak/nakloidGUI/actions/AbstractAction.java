package nak.nakloidGUI.actions;

import org.eclipse.jface.action.Action;

import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;
import nak.nakloidGUI.gui.MainWindow.MainWindowDisplayMode;
import nak.nakloidGUI.gui.MainWindow.MainWindowListener;

public class AbstractAction extends Action implements MainWindowListener {
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

	@Override
	public void updateHorizontalScale(int msByPixel) {}

	@Override
	public void updateVerticalScale(int noteHeight) {}

	@Override
	public void updateMainWindowSize() {}

	@Override
	public void updateDisplayMode(MainWindowDisplayMode mainWindowDisplayMode) {}
}
