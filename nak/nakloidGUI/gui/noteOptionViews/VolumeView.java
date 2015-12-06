package nak.nakloidGUI.gui.noteOptionViews;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.models.Note;
import nak.nakloidGUI.models.Voice;

public class VolumeView extends Canvas {
	private ScrollBar horizontalBar = getHorizontalBar();
	private Note note;
	private Voice voice;
	private int offset = 0;
	final static int maxVolume = 128;
	int areaHeight=0, areaWidth=0, noteLength, posBase;

	private List<VolumeViewListener> volumeViewListeners = new ArrayList<VolumeViewListener>();
	public interface VolumeViewListener {
		public void volumeViewAddVelPoint(int ms, int size);
		public void volumeViewDeleteVelPoint(int ms);
		public void volumeViewBarUpdated(SelectionEvent e);
	}
	public void setVolumeViewListener(VolumeViewListener volumeViewListener){
		volumeViewListeners.add(volumeViewListener);
	}
	public void removeVolumeViewListener(VolumeViewListener volumeViewListener){
		volumeViewListeners.remove(volumeViewListener);
	}

	public VolumeView(Composite parent, Note note, Voice voice) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.H_SCROLL|SWT.NO_BACKGROUND);
		this.note = note;
		this.voice = voice;
		int volumeViewHeight = NakloidGUI.preferenceStore.getInt("gui.noteOption.volumeViewHeight");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = volumeViewHeight;
		setLayoutData(gridData);
		CanvasPaintListener canvasPaintListener = new CanvasPaintListener();
		addPaintListener(canvasPaintListener);
		CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
		addMouseListener(canvasMouseListener);
		horizontalBar.setEnabled(false);
		horizontalBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int hSelection = horizontalBar.getSelection();
				scroll(-hSelection-offset, 0, 0, 0, note.getLength(), volumeViewHeight, false);
				offset = -hSelection;
				for (VolumeViewListener volumeViewListener : volumeViewListeners) {
					volumeViewListener.volumeViewBarUpdated(e);
				}
			}
		});
	}

	public void redraw(Note note, Voice voice) {
		if (!this.isDisposed()) {
			this.note = note;
			this.voice = voice;
			redraw();
		}
	}

	private class CanvasPaintListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent e) {
			areaHeight = getClientArea().height;
			areaWidth = getClientArea().width;
			noteLength = note.getPronLength(voice);
			posBase = areaHeight - maxVolume;

			if (noteLength > areaWidth) {
				horizontalBar.setEnabled(true);
				horizontalBar.setMaximum(noteLength);
				horizontalBar.setThumb(areaWidth);
				horizontalBar.setPageIncrement(areaWidth);
			} else {
				horizontalBar.setEnabled(false);
			}

			Image image = new Image(e.display, areaWidth, areaHeight);
			GC gcImage = new GC(image);
			gcImage.setAntialias(SWT.ON);
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gcImage.fillRectangle(0, 0, areaWidth, areaHeight);

			// ruler
			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
			for(int i=1; i*10<noteLength; i++) {
				if (i%10 == 0) {
					gcImage.drawLine(i*10+offset, posBase, i*10+offset, posBase-10);
					gcImage.drawString(Integer.toString(i*10)+"ms", i*10+3+offset, posBase-23, true);
				} else {
					gcImage.drawLine(i*10+offset, posBase, i*10+offset, posBase-5);
				}
			}

			// margin & padding
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));
			gcImage.fillRectangle(offset, posBase, noteLength, maxVolume);
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			gcImage.fillRectangle(offset, posBase, note.getFrontMargin(), maxVolume);
			gcImage.fillRectangle(noteLength-note.getBackMargin()+offset, posBase, note.getBackMargin(), maxVolume);
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
			gcImage.fillRectangle(note.getFrontMargin()+offset, posBase, note.getFrontPadding(), maxVolume);
			gcImage.fillRectangle(noteLength-note.getBackMargin()-note.getBackPadding()+offset, posBase, note.getBackPadding(), maxVolume);

			// volume line
			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_CYAN));
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_CYAN));
			if (note.getVelPointsSize() > 0) {
				TreeMap<Long, Long> volumeMap = new TreeMap<Long, Long>() {{
					for (int i=0; i<note.getVelPointsSize(); i++) {
						long tmpPoint = 0;
						if (note.getVelPoints().get(i).get(0) < 0) {
							tmpPoint = noteLength + note.getVelPoint(i).get(0);
						} else {
							tmpPoint = note.getFrontMargin() + note.getVelPoint(i).get(0);
						}
						if (tmpPoint>note.getFrontMargin() && tmpPoint<noteLength-note.getBackMargin()) {
							put(tmpPoint, areaHeight - note.getVelPoint(i).get(1));
						}
					}
					put(0L, (long)areaHeight);
					put((long)note.getFrontMargin(), (long)areaHeight);
					put((long)noteLength-note.getBackMargin(), (long)areaHeight);
					put((long)noteLength, (long)areaHeight);
				};};
				int volumeLine[] = new int[volumeMap.size()*2];
				Iterator<Long> it = volumeMap.keySet().iterator();
				for (int i=0; i<volumeMap.size()*2&&it.hasNext(); i+=2) {
					Long ms = it.next();
					volumeLine[i] = ms.intValue() + offset;
					volumeLine[i+1] = volumeMap.get(ms).intValue();
					if (volumeLine[i]>note.getFrontMargin() && volumeLine[i]<noteLength-note.getBackMargin()) {
						gcImage.fillOval(volumeLine[i]-2, volumeLine[i+1]-2, 5, 5);
					}
				}
				gcImage.drawPolyline(volumeLine);
			} else {
				int volumeLine[] = {offset, areaHeight,
						note.getFrontMargin()+offset, areaHeight,
						note.getFrontMargin()+note.getFrontPadding()+offset, areaHeight-note.getBaseVelocity(),
						noteLength-note.getBackMargin()-note.getBackPadding()+offset, areaHeight-note.getBaseVelocity(),
						noteLength-note.getBackMargin()+offset, areaHeight,
						noteLength+offset, areaHeight};
				gcImage.drawPolyline(volumeLine);
			}

			// base volume line
			if (note.getVelPointsSize() > 0) {
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
				gcImage.drawLine(0, areaHeight-note.getBaseVelocity(), noteLength, areaHeight-note.getBaseVelocity());
			}

			e.gc.drawImage(image, 0, 0);
			gcImage.dispose();
			image.dispose();
		}
	}

	private class CanvasMouseListener implements MouseListener {
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			boolean deleteMode = false;
			for (int i=0; i<note.getVelPointsSize(); i++) {
				Rectangle tmpArea = new Rectangle(note.getVelPoint(i).get(0).intValue()+note.getFrontMargin()-2, areaHeight-note.getVelPoint(i).get(1).intValue()-2, 5, 5);
				if (tmpArea.contains(e.x-offset, e.y)) {
					for (VolumeViewListener volumeViewListener : volumeViewListeners) {
						volumeViewListener.volumeViewDeleteVelPoint(note.getVelPoint(i).get(0).intValue());
					}
					deleteMode = true;
					break;
				}
			}
			if (!deleteMode && e.x-offset>note.getFrontMargin() && e.x-offset<noteLength-note.getBackMargin() && e.y>posBase) {
				for (VolumeViewListener volumeViewListener : volumeViewListeners) {
					volumeViewListener.volumeViewAddVelPoint(e.x-note.getFrontMargin()-offset, areaHeight-e.y);
				}
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {}

		@Override
		public void mouseUp(MouseEvent e) {}
	}
}
