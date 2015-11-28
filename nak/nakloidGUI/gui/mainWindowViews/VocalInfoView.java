package nak.nakloidGUI.gui.mainWindowViews;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.models.VocalInfo;

public class VocalInfoView extends Canvas {
	private VocalInfo vi;
	final private CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
	final private UpdateInfo updateInfo = new UpdateInfo();
	private boolean displayLiner = false;
	private int numString = -1;

	public VocalInfoView(Composite parent, VocalInfo vi) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.NO_BACKGROUND);
		this.vi = vi;
		CanvasPaintListener canvasPaintListener = new CanvasPaintListener();
		addPaintListener(canvasPaintListener);
		if (vi != null) {
			addMouseListener(canvasMouseListener);
			addMouseTrackListener(canvasMouseListener);
		}
	}

	public void redraw(VocalInfo vi) {
		if (!this.isDisposed()) {
			this.vi = vi;
			removeMouseListener(canvasMouseListener);
			if (vi != null) {
				addMouseListener(canvasMouseListener);
			}
			redraw();
		}
	}

	private class CanvasPaintListener implements PaintListener {
		private String displayMode = "";

		@Override
		public void paintControl(PaintEvent e) {
			displayMode = NakloidGUI.preferenceStore.getString("gui.mainWindow.vocalInfoDisplayMode");
			Canvas canvas = (Canvas)e.widget;
			Image image = null;
			int cnvHeight=canvas.getClientArea().height, cnvWidth=canvas.getClientArea().width;
			if (vi!=null && vi.hasPathImage()) {
				image = loadImage(vi.getPathImage());
			} else {
				image = new Image(e.display, canvas.getClientArea().width, canvas.getClientArea().height);
			}
			GC gc = new GC(image);
			gc.setAntialias(SWT.ON);
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
			gc.drawRectangle(0, 0, cnvWidth-1, cnvHeight-1);
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			if (vi != null) {
				if (displayMode.equals("liner") && displayLiner && vi.hasName()) {
					setToolTipText(null);
					gc.setAlpha(100);
					gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					gc.fillRectangle(0, cnvHeight-20, cnvWidth, 20);
					gc.setAlpha(255);
					gc.drawText(vi.getName().substring(numString), 5, cnvHeight-18, true);
				} else if (displayMode.equals("tooltip") && vi.hasText()) {
					setToolTipText(vi.getText());
				}
			}
			e.gc.drawImage(image, 0, 0);
			gc.dispose();
		}
	}

	private class CanvasMouseListener implements MouseListener, MouseTrackListener {
		@Override
		public void mouseUp(MouseEvent e) {}

		@Override
		public void mouseDown(MouseEvent e) {}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(vi.getPathReadme().toFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void mouseEnter(MouseEvent e) {
			if (!displayLiner) {
				displayLiner = true;
				Display.getCurrent().asyncExec(updateInfo);
			}
		}

		@Override
		public void mouseExit(MouseEvent e) {
			displayLiner = false;
			numString = -1;
			redraw();
			update();
		}

		@Override
		public void mouseHover(MouseEvent e) {}
	}

	private class UpdateInfo implements Runnable {
		@Override
		public void run() {
			if(displayLiner && vi!=null && vi.hasName()) {
				numString = (vi.getName().length()>numString)?numString+1:0;
				Display.getCurrent().timerExec(500, this);
			}
			redraw();
		}
	}

	private Image loadImage(Path path) {
		URL url = getClass().getClassLoader().getResource(path.toString());
		Image tmpImage;
		if (url == null) {
			tmpImage = new Image(null, path.toString());
		} else {
			ImageDescriptor id = ImageDescriptor.createFromURL(url);
			tmpImage = id.createImage();
		}
		return tmpImage;
	}
}
