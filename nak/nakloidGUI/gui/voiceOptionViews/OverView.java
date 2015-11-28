package nak.nakloidGUI.gui.voiceOptionViews;

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
import nak.nakloidGUI.models.Voice;
import nak.nakloidGUI.models.Waveform;

public class OverView extends Canvas {
	Voice voice;
	private Waveform waveform;
	private String message="";
	private int voiceViewWidth=0, voiceViewOffset=0;

	public OverView(Composite parent, Voice voice) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
		this.voice = voice;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = NakloidGUI.preferenceStore.getInt("gui.voiceOption.waveformGraphHeight");
		gridData.horizontalSpan = 5;
		setLayoutData(gridData);
		CanvasListener canvasListener = new CanvasListener();
		addPaintListener(canvasListener);
	}

	public void redraw(int voiceViewWitdh, int voiceViewOffset) {
		if (!this.isDisposed()) {
			this.voiceViewWidth = voiceViewWitdh;
			this.voiceViewOffset = voiceViewOffset;
			redraw();
		}
	}

	public void redraw(Waveform waveform) {
		if (!this.isDisposed()) {
			this.waveform = waveform;
			redraw();
		}
	}

	public void redraw(String message) {
		if (!this.isDisposed()) {
			this.message = message;
			redraw();
		}
	}

	private class CanvasListener implements PaintListener {
		private Rectangle savedArea = new Rectangle(0, 0, 0, 0);
		private int[][] waveformPoints = null;
		@Override
		public void paintControl(PaintEvent e) {
			Canvas canvas = (Canvas)e.widget;
			Rectangle tmpArea = canvas.getClientArea();
			int halfHeight = (int)tmpArea.height / 2;

			Image image = new Image(e.display, canvas.getClientArea().width, canvas.getClientArea().height);
			GC gcImage = new GC(image);
			gcImage.setAntialias(SWT.ON);
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));

			if (waveform!=null && waveform.isLoaded()) {
				double msOffset = (double)voice.getOffset();
				double msOverlap = (double)voice.getOverlap();
				double msPreutterance = (double)voice.getPreutterance();
				double msConsonant = (double)voice.getConsonant();
				double msBlank = (double)voice.getBlank();
				msBlank = (msBlank<0)?msOffset-msBlank:(waveform.getDataSize()*1000.0/waveform.getSampleRate())-msBlank;

				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
				gcImage.fillRectangle(0, 0, ms2point(msOffset, tmpArea.width), tmpArea.height);
				gcImage.fillRectangle(ms2point(msBlank, tmpArea.width), 0, tmpArea.width, tmpArea.height);

				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(0, halfHeight, tmpArea.width, halfHeight);

				int pointWidth = (int)(waveform.getDataSize()/(double)tmpArea.width);
				if (!savedArea.equals(canvas.getClientArea())) {
					savedArea = canvas.getClientArea();
					waveformPoints = new int[tmpArea.width][2];
					for (int i=0; i<tmpArea.width-1; i++) {
						double waveformPointMax = -1.0;
						double waveformPointMin = 1.0;
						for (int j=pointWidth*i; j<pointWidth*(i+1); j++) {
							if (waveformPointMax < waveform.getData(j)) {
								waveformPointMax = waveform.getData(j);
							}
							if (waveformPointMin > waveform.getData(j)) {
								waveformPointMin = waveform.getData(j);
							}
						}
						waveformPoints[i][0] = halfHeight - (int)(waveformPointMax*halfHeight);
						waveformPoints[i][1] = halfHeight - (int)(waveformPointMin*halfHeight);
					}
				}
				for (int i=0; i<tmpArea.width-1; i++) {
					gcImage.drawLine(i, waveformPoints[i][0], i, waveformPoints[i][1]);
				}

				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_MAGENTA));
				gcImage.drawRectangle(
						(int)(-voiceViewOffset/(waveform.getDataSize()/(double)tmpArea.width)),
						0,
						(int)(voiceViewWidth/(waveform.getDataSize()/(double)tmpArea.width)),
						tmpArea.height-1);

				int offsetPoint = ms2point(msOffset,tmpArea.width);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(offsetPoint, 0, offsetPoint, tmpArea.height);
				int overlapPoint = ms2point(msOffset+msOverlap,tmpArea.width);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GREEN));
				gcImage.drawLine(overlapPoint, 0, overlapPoint, tmpArea.height);
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GREEN));
				gcImage.fillOval(overlapPoint-5,0,10,10);
				int preutterancePoint = ms2point(msOffset+msPreutterance,tmpArea.width);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
				gcImage.drawLine(preutterancePoint, 0, preutterancePoint, tmpArea.height);
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
				gcImage.fillOval(preutterancePoint-5,tmpArea.height-10,10,10);
				int consonantPoint = ms2point(msOffset+msConsonant,tmpArea.width);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_RED));
				gcImage.drawLine(consonantPoint, 0, consonantPoint, tmpArea.height);
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_RED));
				gcImage.fillOval(consonantPoint-5,halfHeight-5,10,10);
				int blankPoint = ms2point(msBlank,tmpArea.width);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(blankPoint, 0, blankPoint, tmpArea.height);
			} else {
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(0, halfHeight, tmpArea.width, halfHeight);
				if (message.isEmpty()) {
					gcImage.drawString("歌声読込中...", 5, 3);
				} else {
					gcImage.drawString(message, 5, 3);
				}
			}

			e.gc.drawImage(image, 0, 0);
			gcImage.dispose();
			image.dispose();
		}

		public int ms2point (double ms, int width) {
			return (int)(ms/1000*waveform.getSampleRate()*(width/(double)waveform.getDataSize()));
		}
	}
}

