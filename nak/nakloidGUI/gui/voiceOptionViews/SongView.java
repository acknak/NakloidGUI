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
import nak.nakloidGUI.models.Waveform;

public class SongView extends Canvas {
	Waveform waveform;
	String message = "";
	boolean isSongGenerated;

	public SongView(Composite parent, Waveform waveform) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
		this.waveform = waveform;

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = NakloidGUI.preferenceStore.getInt("gui.voiceOption.waveformGraphHeight");
		gridData.verticalSpan = 5;
		setLayoutData(gridData);
		addPaintListener(new CanvasListener());
	}

	public void redraw(Waveform waveform) {
		if (!this.isDisposed()) {
			this.waveform = waveform;
			isSongGenerated = true;
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

			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			gcImage.drawLine(0, halfHeight, tmpArea.width, halfHeight);

			if (waveform!=null && waveform.isLoaded()) {
				int pointWidth = (int)(waveform.getDataSize()/(double)tmpArea.width);
				if (!savedArea.equals(canvas.getClientArea()) || waveformPoints == null) {
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
			} else {
				if (isSongGenerated) {
					gcImage.drawString("歌声生成中...", 5, 3);
				} else {
					gcImage.drawString(message, 5, 3);
				}
				waveformPoints = null;
			}

			e.gc.drawImage(image, 0, 0);
			gcImage.dispose();
			image.dispose();
		}
	}
}
