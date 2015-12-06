package nak.nakloidGUI.actions.executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class BuildAndPlayAction extends AbstractAction {
	public BuildAndPlayAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("合成して再生(&R)@Ctrl+Space");
		setAccelerator(SWT.CTRL + SWT.SPACE);
	}

	@Override
	public void run() {
		mainWindow.buildAction.run();
		Display.getCurrent().asyncExec(new Runnable () {
			@Override
			public void run() {
				if (coreData.getSongWaveform().isLoaded()) {
					mainWindow.playAction.run();
				} else {
					Display.getCurrent().timerExec(50, this);
				}
			}
		});
	}
}
