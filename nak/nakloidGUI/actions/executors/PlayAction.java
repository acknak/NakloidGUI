package nak.nakloidGUI.actions.executors;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import nak.nakloidGUI.actions.AbstractAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.MainWindow;

public class PlayAction extends AbstractAction {
	Thread thrRedraw;
	boolean isPlaying = false;

	public PlayAction(MainWindow mainWindow, CoreData coreData) {
		super(mainWindow, coreData);
		setText("再生(&P)@Space");
		setAccelerator(SWT.SPACE);
	}

	@Override
	public void run() {
		if (coreData.getSongWaveform() != null) {
			if (coreData.getSongWaveform().isPlaying()) {
				isPlaying = false;
				coreData.getSongWaveform().pause();
			} else {
				isPlaying = true;
				try {
					coreData.getSongWaveform().play();
				} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
					MessageDialog.openError(mainWindow.getShell(), "NakloidGUI", "Wavファイルの読み込みに失敗しました。\n"+e.toString()+e.getMessage());
				}
				Display.getCurrent().asyncExec(new Runnable () {
					@Override
					public void run() {
						mainWindow.waveformSeeked();
						if (isPlaying) {
							Display.getCurrent().timerExec(50, this);
						}
					}
				});
			}
		}
	}
}
