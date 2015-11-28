package nak.nakloidGUI.gui.mainWindowViews;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.gui.MainWindow.MusicalScales;

public class KeyboardView extends Canvas {
	private int mainViewOffset = 0;
	private int noteHeight = NakloidGUI.preferenceStore.getInt("gui.mainWindow.baseNoteHeight");

	public KeyboardView(Composite parent) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
		GridData gdCnvKeyboard = new GridData(GridData.FILL_VERTICAL);
		gdCnvKeyboard.widthHint = NakloidGUI.preferenceStore.getInt("gui.mainWindow.keyboardWidth");

		setLayoutData(gdCnvKeyboard);
		CnvKeyboardListener cnvKeyboardListener = new CnvKeyboardListener();
		addPaintListener(cnvKeyboardListener);
	}

	public void redraw(int mainViewOffset, int noteHeight) {
		if (!this.isDisposed()) {
			this.mainViewOffset = mainViewOffset;
			this.noteHeight = noteHeight;
			redraw();
		}
	}

	private class CnvKeyboardListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent e) {
			int numMidiNoteUpperLimit = NakloidGUI.preferenceStore.getInt("gui.mainWindow.numMidiNoteUpperLimit");
			int numMidiNoteLowerLimit = NakloidGUI.preferenceStore.getInt("gui.mainWindow.numMidiNoteLowerLimit");
			Rectangle tmpArea = getClientArea();
			Image image = new Image(e.display, tmpArea.width, tmpArea.height);
			GC gcImage = new GC(image);
			gcImage.setAntialias(SWT.ON);

			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
			gcImage.fillRectangle(0, 0, tmpArea.width, tmpArea.height);
			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
			gcImage.drawLine(0, 0, tmpArea.width, 0);
			gcImage.drawLine(tmpArea.width-1, 0, tmpArea.width-1, tmpArea.height);
			for (int i=0; i<numMidiNoteUpperLimit-numMidiNoteLowerLimit+1; i++) {
				int numMidiNote = numMidiNoteUpperLimit-i;
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
				if (MusicalScales.F_MAJOR.equals(numMidiNote)||MusicalScales.C_MAJOR.equals(numMidiNote)) {
					gcImage.drawLine(0, (i+1)*noteHeight+mainViewOffset-13, tmpArea.width, (i+1)*noteHeight+mainViewOffset-13);
				} else if (MusicalScales.A_MAJOR.equals(numMidiNote)||MusicalScales.D_MAJOR.equals(numMidiNote)) {
					gcImage.drawLine(0, (i+1)*noteHeight+mainViewOffset-7, tmpArea.width, (i+1)*noteHeight+mainViewOffset-7);
				} else if (MusicalScales.G_MAJOR.equals(numMidiNote)) {
					gcImage.drawLine(0, (i+1)*noteHeight+mainViewOffset-10, tmpArea.width, (i+1)*noteHeight+mainViewOffset-10);
				} else if (MusicalScales.F.equals(numMidiNote) || MusicalScales.C.equals(numMidiNote)) {
					gcImage.drawLine(0, (i+1)*noteHeight+mainViewOffset, tmpArea.width, (i+1)*noteHeight+mainViewOffset);
				}
				if (MusicalScales.C.equals(numMidiNote)) {
					gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
					gcImage.drawText(MusicalScales.getStringFromNumMidiNote(numMidiNote,true), 50, i*noteHeight+mainViewOffset+2, true);
				} else {
					if (MusicalScales.getMusicalScalesFromNumMidiNote(numMidiNote).isMajor()) {
						gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_BLACK));
						gcImage.fillRectangle(new Rectangle(0, i*noteHeight+mainViewOffset, 70, noteHeight));
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
					} else {
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
					}
					gcImage.drawText(MusicalScales.getStringFromNumMidiNote(numMidiNote,false), 50, i*noteHeight+mainViewOffset+2, true);
				}
			}

			e.gc.drawImage(image, 0, 0);
			gcImage.dispose();
			image.dispose();
		}
	}
}
