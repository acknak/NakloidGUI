package nak.nakloidGUI.gui.voiceOptionViews;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
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
import nak.nakloidGUI.models.Pmp;
import nak.nakloidGUI.models.Voice;
import nak.nakloidGUI.models.Waveform;

public class VoiceView extends Canvas {
	private VoiceViewListener voiceViewListener;
	private Voice voice;
	private Pmp pmp;
	private Waveform waveform, wfPmpTeacher, wfPmpPrefixTeacher;
	private ScrollBar horizontalBar=getHorizontalBar();
	private String message="";
	private int offset = 0;
	private double[] base_vowel_waveform, prefix_vowel_waveform;
	private boolean showPmpPoint=true, showUpsideDown=false, showPmpTeacher=false, showPmpPrefixTeacher=false;

	public VoiceView(Composite parent, Voice voice, Pmp pmp) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.H_SCROLL|SWT.NO_BACKGROUND);
		this.voice = voice;
		this.pmp = pmp;
		int waveformGraphHeight = NakloidGUI.preferenceStore.getInt("gui.voiceOption.waveformGraphHeight");

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = waveformGraphHeight;
		setLayoutData(gridData);
		CanvasPaintListener canvasPaintListener = new CanvasPaintListener();
		addPaintListener(canvasPaintListener);
		CanvasControlListener canvasControlListener = new CanvasControlListener();
		addControlListener(canvasControlListener);

		horizontalBar.setEnabled(false);
		horizontalBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int hSelection = horizontalBar.getSelection();
				if (waveform!=null && waveform.isLoaded()) {
					scroll(-hSelection - offset, 0, 0, 0, waveform.getDataSize(), waveformGraphHeight, false);
				}
				offset = -hSelection;
				if (voiceViewListener != null) {
					voiceViewListener.voiceViewHorizontalBarUpdated(e);
				}
			}
		});
	}

	public void redraw(Voice voice) {
		if (!this.isDisposed()) {
			this.voice = voice;
			redraw();
		}
	}

	public void redraw(Pmp pmp) {
		if (!this.isDisposed()) {
			this.pmp = pmp;
			redraw();
		}
	}

	public void redraw(boolean showPmpPoint, boolean showUpsideDown, boolean showPmpTeacher, boolean showPmpPrefixTeacher) {
		if (!this.isDisposed()) {
			this.showPmpPoint = showPmpPoint;
			this.showUpsideDown = showUpsideDown;
			this.showPmpTeacher = showPmpTeacher;
			this.showPmpPrefixTeacher=showPmpPrefixTeacher;
			redraw();
		}
	}

	public void redraw(Waveform waveform) {
		if (!this.isDisposed()) {
			this.waveform = waveform;
			redraw();
		}
	}

	public void redraw(Waveform wfPmpTeacher, Waveform wfPmpPrefixTeacher) {
		if (!this.isDisposed()) {
			this.wfPmpTeacher = wfPmpTeacher;
			this.wfPmpPrefixTeacher = wfPmpPrefixTeacher;
			redraw();
		}
	}

	public void redraw(String message) {
		if (!this.isDisposed()) {
			this.message = message;
			redraw();
		}
	}

	public int getOffset() {
		return offset;
	}

	public boolean showPmpPoint() {
		return showPmpPoint;
	}

	public boolean showUpsideDown() {
		return showUpsideDown;
	}

	public boolean showPmpTeacher() {
		return showPmpTeacher;
	}

	public boolean showPmpPrefixTeacher() {
		return showPmpPrefixTeacher;
	}

	public interface VoiceViewListener {
		public void voiceViewHorizontalBarUpdated(SelectionEvent e);
		public void voiceViewResized();
	}

	public void setVoiceViewListener(VoiceViewListener voiceViewListener){
		this.voiceViewListener = voiceViewListener;
	}

	private class CanvasPaintListener implements PaintListener {
		int[] baseWaveformPoints;
		int[] prefixWaveformPoints;

		@Override
		public void paintControl(PaintEvent e) {
			Rectangle clientArea = getClientArea();
			int halfHeight = (int)clientArea.height / 2;

			if (waveform!=null && waveform.isLoaded()) {
				int waveformSize = waveform.getDataSize();
				if (waveformSize > clientArea.width) {
					horizontalBar.setEnabled(true);
					horizontalBar.setMaximum(waveformSize);
					horizontalBar.setThumb(clientArea.width);
					horizontalBar.setPageIncrement(clientArea.width);
				}
			} else {
				horizontalBar.setEnabled(false);
			}

			Image image = new Image(e.display, clientArea.width, clientArea.height);
			GC gcImage = new GC(image);
			gcImage.setAntialias(SWT.ON);
			gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_WHITE));

			if (waveform!=null && waveform.isLoaded()) {
				int waveformSize = waveform.getDataSize();
				int[] waveformPoints = new int[waveformSize];
				{
					for (int i=0; i<waveformSize; i++) {
						if (showUpsideDown) {
							waveformPoints[i] = (int)(waveform.getData(i)*halfHeight+halfHeight);
						} else {
							waveformPoints[i] = (int)(halfHeight-waveform.getData(i)*halfHeight);
						}
					}
				}

				double msOffset = (double)voice.getOffset();
				double msOverlap = (double)voice.getOverlap();
				double msPreutterance = (double)voice.getPreutterance();
				double msConsonant = (double)voice.getConsonant();
				double msBlank = (double)voice.getBlank();
				msBlank = (msBlank<0)?msOffset-msBlank:(waveformSize*1000.0/waveform.getSampleRate())-msBlank;

				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
				gcImage.fillRectangle(0, 0, ms2point(msOffset), clientArea.height);
				gcImage.fillRectangle(ms2point(msBlank), 0, clientArea.width, clientArea.height);

				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(0, halfHeight, clientArea.width, halfHeight);
				for (int i=0; i<waveformSize-1; i++) {
					gcImage.drawLine(i+offset, waveformPoints[i], i+offset+1, waveformPoints[i+1]);
				}

				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_CYAN));
				for (int i=0; i<pmp.size(); i++) {
					int tmpPoint = pmp.get(i) + offset;
					if (i == pmp.getSubFadeStart()) {
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
					}
					gcImage.drawLine(tmpPoint, 0, tmpPoint, clientArea.height);
					if (showPmpPoint) {
						gcImage.drawText(Integer.toString(pmp.get(i)), tmpPoint+5, 5, true);
					}
				}

				int offsetPoint = ms2point(msOffset);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(offsetPoint, 0, offsetPoint, clientArea.height);
				int overlapPoint = ms2point(msOffset+msOverlap);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GREEN));
				gcImage.drawLine(overlapPoint, 0, overlapPoint, clientArea.height);
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_GREEN));
				gcImage.fillOval(overlapPoint-5,0,10,10);
				int preutterancePoint = ms2point(msOffset+msPreutterance);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
				gcImage.drawLine(preutterancePoint, 0, preutterancePoint, clientArea.height);
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_BLUE));
				gcImage.fillOval(preutterancePoint-5,clientArea.height-10,10,10);
				int consonantPoint = ms2point(msOffset+msConsonant);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_RED));
				gcImage.drawLine(consonantPoint, 0, consonantPoint, clientArea.height);
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_RED));
				gcImage.fillOval(consonantPoint-5,halfHeight-5,10,10);
				int blankPoint = ms2point(msBlank);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(blankPoint, 0, blankPoint, clientArea.height);

				if (showPmpTeacher && wfPmpTeacher!=null && wfPmpTeacher.isLoaded()) {
					double[] tmp = wfPmpTeacher.getData();
					base_vowel_waveform = new double[pmp.getBaseVowelWav().getLength()];
					for (int i=0; i<base_vowel_waveform.length; i++) {
						base_vowel_waveform[i] = tmp[pmp.getBaseVowelWav().from+i];
					}
					baseWaveformPoints = new int[base_vowel_waveform.length];
					for (int i=0; i<base_vowel_waveform.length; i++) {
						baseWaveformPoints[i] = (int)(halfHeight-base_vowel_waveform[i]*halfHeight);
					}
					int tmp_from = clientArea.width/2 - (baseWaveformPoints.length/2);
					for (int i=0; i<baseWaveformPoints.length-1; i++) {
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_BLUE));
						gcImage.drawLine(tmp_from+i, baseWaveformPoints[i], tmp_from+i+1, baseWaveformPoints[i+1]);
					}
					gcImage.drawLine(tmp_from, 0, tmp_from, clientArea.height);
					gcImage.drawLine(tmp_from+(baseWaveformPoints.length/2), 0, tmp_from+(baseWaveformPoints.length/2), clientArea.height);
					gcImage.drawLine(tmp_from+baseWaveformPoints.length, 0, tmp_from+baseWaveformPoints.length, clientArea.height);
				}

				if (showPmpPrefixTeacher && wfPmpPrefixTeacher!=null && wfPmpPrefixTeacher.isLoaded()) {
					double[] tmp = wfPmpPrefixTeacher.getData();
					prefix_vowel_waveform = new double[pmp.getPrefixVowelWav().getLength()];
					for (int i=0; i<prefix_vowel_waveform.length; i++) {
						prefix_vowel_waveform[i] = tmp[pmp.getPrefixVowelWav().from+i];
					}
					prefixWaveformPoints = new int[prefix_vowel_waveform.length];
					for (int i=0; i<prefix_vowel_waveform.length; i++) {
						prefixWaveformPoints[i] = (int)(halfHeight-prefix_vowel_waveform[i]*halfHeight);
					}
					int tmp_from = clientArea.width/2 - (prefixWaveformPoints.length/2);
					for (int i=0; i<prefixWaveformPoints.length-1; i++) {
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
						gcImage.drawLine(tmp_from+i, prefixWaveformPoints[i], tmp_from+i+1, prefixWaveformPoints[i+1]);
					}
					gcImage.drawLine(tmp_from, 0, tmp_from, clientArea.height);
					gcImage.drawLine(tmp_from+(prefixWaveformPoints.length/2), 0, tmp_from+(prefixWaveformPoints.length/2), clientArea.height);
					gcImage.drawLine(tmp_from+prefixWaveformPoints.length, 0, tmp_from+prefixWaveformPoints.length, clientArea.height);
				}
			} else {
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gcImage.drawLine(0, halfHeight, clientArea.width, halfHeight);
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

		public int ms2point (double ms) {
			return (int)(ms/1000.0*waveform.getSampleRate()) + offset;
		}
	}

	private class CanvasControlListener implements ControlListener {
		@Override
		public void controlMoved(ControlEvent e) {}

		@Override
		public void controlResized(ControlEvent e) {
			if (waveform!=null && waveform.isLoaded()) {
				Rectangle client = getClientArea();
				int waveformSize = waveform.getDataSize();
				horizontalBar.setMaximum(waveformSize);
				horizontalBar.setThumb(Math.min(waveformSize, client.width));
				int hPage = waveformSize - client.width;
				int hSelection = horizontalBar.getSelection();
				if (hSelection >= hPage) {
					offset -= (hPage <= 0)?0:hSelection;
				}
			}
			voiceViewListener.voiceViewResized();
		}
	}
}
