package nak.nakloidGUI.gui.mainWindowViews;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.models.Waveform;

public class OverView extends Canvas {
	private Waveform waveform;
	private String message = "";
	private int mainViewWidth=0, mainViewOffset=0;
	private int msByPixel = NakloidGUI.preferenceStore.getInt("gui.mainWindow.baseMsByPixel");

	private Rectangle savedArea=new Rectangle(0,0,0,0), tmpArea;
	private int[][] waveformPoints = null;
	private int seekPoint=0, wfSongId=0;
	private Rectangle draggableRectangle = new Rectangle(0, 0, 10, 0);
	private boolean isDragging = false;
	final private Cursor cursorHand=new Cursor(null,SWT.CURSOR_HAND), cursorArrow=new Cursor(null,SWT.CURSOR_ARROW);

	private List<OverViewListener> overViewListeners = new ArrayList<OverViewListener>();
	public interface OverViewListener {
		public void waveformSeeked();
	}
	public void addOverViewListener(OverViewListener overViewListener) {
		this.overViewListeners.add(overViewListener);
	}
	public void removeOverViewListener(OverViewListener overViewListener) {
		this.overViewListeners.remove(overViewListener);
	}

	public OverView(Composite parent, Waveform waveform) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
		this.waveform = waveform;
		GridData gdCnvOverView = new GridData(GridData.FILL_BOTH);
		setLayoutData(gdCnvOverView);
		CanvasPaintListener canvasPaintListener = new CanvasPaintListener();
		addPaintListener(canvasPaintListener);
		CanvasMouseListener canvasMouseistener = new CanvasMouseListener();
		addMouseListener(canvasMouseistener);
		addMouseTrackListener(canvasMouseistener);
		addMouseMoveListener(canvasMouseistener);
	}

	public void redraw(Waveform waveform) {
		if (!this.isDisposed()) {
			this.waveform = waveform;
			redraw();
		}
	}

	public void redraw(int mainViewWitdh, int mainViewOffset, int msByPixel) {
		if (!this.isDisposed()) {
			this.mainViewWidth = mainViewWitdh;
			this.mainViewOffset = mainViewOffset;
			this.msByPixel = msByPixel;
			redraw();
		}
	}

	public void redraw(Waveform waveform, int mainViewWitdh, int mainViewOffset, int msByPixel) {
		if (!this.isDisposed()) {
			this.waveform = waveform;
			this.mainViewWidth = mainViewWitdh;
			this.mainViewOffset = mainViewOffset;
			this.msByPixel = msByPixel;
			redraw();
		}
	}

	public void redraw(String message) {
		if (!this.isDisposed()) {
			this.waveform = null;
			this.message = message;
			redraw();
		}
	}

	public void setWaveformSeekPoint(int seekPoint) {
		waveform.setMicrosecond(point2ms(seekPoint*(waveform.getDataSize()/(double)getClientArea().width))*1000);
	}

	private class CanvasPaintListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent e) {
			Canvas canvas = (Canvas)e.widget;
			tmpArea = canvas.getClientArea();
			int halfHeight = (int)tmpArea.height / 2;

			Image image = new Image(e.display, canvas.getClientArea().width, canvas.getClientArea().height);
			GC gcImage = new GC(image);
			gcImage.setAntialias(SWT.ON);

			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
			gcImage.drawRectangle(0, 0, tmpArea.width-1, tmpArea.height-1);
			gcImage.drawLine(0, halfHeight, tmpArea.width, halfHeight);

			if (waveform==null || !waveform.isLoaded()) {
				gcImage.drawString(message, 5, 3);
			} else {
				int pointWidth = (int)(waveform.getDataSize()/(double)tmpArea.width);
				if (!savedArea.equals(canvas.getClientArea()) || wfSongId!=waveform.getId()) {
					wfSongId = waveform.getId();
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
						(int)(ms2point(-mainViewOffset*msByPixel)/(waveform.getDataSize()/(double)tmpArea.width)),
						0,
						(int)(ms2point(mainViewWidth*msByPixel)/(waveform.getDataSize()/(double)tmpArea.width)),
						tmpArea.height-1);
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
				if (waveform.isPlaying()) {
					seekPoint = (int)(ms2point(waveform.getMicrosecond()/1000)/(waveform.getDataSize()/(double)tmpArea.width));
				}
				gcImage.fillOval(seekPoint-5,1,11,12);
				gcImage.drawLine(seekPoint, 0, seekPoint, tmpArea.height);
			}

			e.gc.drawImage(image, 0, 0);
			gcImage.dispose();
			image.dispose();
		}
	}

	private class CanvasMouseListener implements MouseListener, MouseMoveListener, MouseTrackListener {
		@Override
		public void mouseDown(MouseEvent e) {
			draggableRectangle.x = seekPoint - 5;
			draggableRectangle.height = tmpArea.height;
			if (draggableRectangle.contains(new Point(e.x, e.y))) {
				if (waveform != null) {
					waveform.pause();
					isDragging = true;
				}
			}
		}

		@Override
		public void mouseMove(MouseEvent e) {
			draggableRectangle.x = seekPoint - 5;
			draggableRectangle.height = tmpArea.height - 1;
			if (draggableRectangle.contains(new Point(e.x, e.y))) {
				getShell().setCursor(cursorHand);
			} else if (!isDragging) {
				getShell().setCursor(cursorArrow);
			}
			if (isDragging) {
				if (e.x < 0) {
					seekPoint = 0;
				} else if (e.x > tmpArea.width) {
					seekPoint = tmpArea.width;
				} else {
					seekPoint = e.x;
				}
				if (waveform != null) {
					setWaveformSeekPoint(seekPoint);
					overViewListeners.stream().forEach(OverViewListener::waveformSeeked);
				}
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if (isDragging && waveform!=null) {
				setWaveformSeekPoint(seekPoint);
				overViewListeners.stream().forEach(OverViewListener::waveformSeeked);
			}
			isDragging = false;
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {}

		@Override
		public void mouseEnter(MouseEvent e) {}

		@Override
		public void mouseExit(MouseEvent e) {
			getShell().setCursor(cursorArrow);
			mouseUp(e);
		}

		@Override
		public void mouseHover(MouseEvent e) {}
	}

	private int ms2point(double ms) {
		if (waveform == null) {
			return 0;
		}
		return (int)(ms/1000.0*waveform.getSampleRate());
	}

	private int point2ms(double point) {
		if (waveform == null) {
			return 0;
		}
		return (int)(point*1000/waveform.getSampleRate());
	}
}
