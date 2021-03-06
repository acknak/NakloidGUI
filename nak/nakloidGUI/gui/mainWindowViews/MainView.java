package nak.nakloidGUI.gui.mainWindowViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSubscriber;
import nak.nakloidGUI.gui.MainWindow.MainWindowDisplayMode;
import nak.nakloidGUI.gui.MainWindow.MusicalScales;
import nak.nakloidGUI.gui.NoteOption;
import nak.nakloidGUI.models.Note;

public class MainView extends Canvas implements CoreDataSubscriber {
	private CoreData coreData;
	private ScrollBar horizontalBar=getHorizontalBar(), verticalBar=getVerticalBar();
	private Point viewSize=new Point(0, 0), offset=new Point(0,0);
	private MainWindowDisplayMode displayMode = MainWindowDisplayMode.NOTES;
	private int margin;
	private double msByPixel = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.baseMsByPixel");
	private int noteHeight = NakloidGUI.preferenceStore.getInt("gui.mainWindow.baseNoteHeight");
	final private Cursor cursorCross=new Cursor(null,SWT.CURSOR_CROSS), cursorArrow=new Cursor(null,SWT.CURSOR_ARROW);
	private TreeMap<Integer, Integer> cursorLocus = new TreeMap<Integer, Integer>();

	private List<MainViewListener> mainViewListeners = new ArrayList<MainViewListener>();
	public interface MainViewListener {
		public void mainViewHorizontalBarUpdated(SelectionEvent e);
		public void mainViewVerticalBarUpdated(SelectionEvent e);
		public void pitchesDrawn();
		public void waveformSeeked();
	}
	public void addMainViewListener(MainViewListener mainViewListener) {
		this.mainViewListeners.add(mainViewListener);
	}
	public void removeMainViewListener(MainViewListener mainViewListener) {
		this.mainViewListeners.remove(mainViewListener);
	}

	public MainView(Composite parent, CoreData coreData) {
		super(parent, SWT.NO_REDRAW_RESIZE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.NO_BACKGROUND);
		this.coreData = coreData;
		coreData.addSubscribers(this);
		margin = (int)((double)coreData.nakloidIni.output.ms_margin/msByPixel);
		viewSize.x = (int)(((double)coreData.getScoreLength())/msByPixel) + margin;
		viewSize.y = (getMidiNoteUpperLimit()-getMidiNoteLowerLimit()+1) * noteHeight;

		GridData gdCnvMainView = new GridData(GridData.FILL_BOTH);
		setLayoutData(gdCnvMainView);

		CanvasPaintListener canvasPaintListener = new CanvasPaintListener();
		addPaintListener(canvasPaintListener);
		CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
		addMouseListener(canvasMouseListener);
		addMouseWheelListener(canvasMouseListener);
		addMouseMoveListener(canvasMouseListener);
		addMouseTrackListener(canvasMouseListener);

		horizontalBar.setEnabled(false);
		horizontalBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int hSelection = horizontalBar.getSelection();
				int vSelection = verticalBar.getSelection();
				scroll(-hSelection-offset.x, -vSelection-offset.y, 0, 0, viewSize.x, viewSize.y, false);
				offset.x = -hSelection;
				offset.y = -vSelection;
				for (MainViewListener mainViewListener : mainViewListeners) {
					mainViewListener.mainViewHorizontalBarUpdated(e);
				}
			}
		});
		verticalBar.setEnabled(false);
		verticalBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int hSelection = horizontalBar.getSelection();
				int vSelection = verticalBar.getSelection();
				scroll(-hSelection-offset.x, -vSelection-offset.y, 0, 0, viewSize.x, viewSize.y, false);
				offset.x = -hSelection;
				offset.y = -vSelection;
				for (MainViewListener mainViewListener : mainViewListeners) {
					mainViewListener.mainViewVerticalBarUpdated(e);
				}
			}
		});
	}

	@Override
	public void redraw() {
		if (!this.isDisposed()) {
			reloadScrollBarsBaseData();
			int hPage = viewSize.x - getClientArea().width;
			int vPage = viewSize.y - getClientArea().height;
			int hSelection = horizontalBar.getSelection();
			int vSelection = verticalBar.getSelection();
			if (hSelection >= hPage) {
				if (hPage <= 0) {
					hSelection = 0;
				}
				offset.x = -hSelection;
			}
			if (vSelection >= vPage) {
				if (vPage <= 0) {
					vSelection = 0;
				}
				offset.y = -vSelection;
			}
			super.redraw();
		}
	}

	public void redraw(MainWindowDisplayMode mainWindowDisplayMode) {
		if (!this.isDisposed()) {
			this.displayMode = mainWindowDisplayMode;
			if (mainWindowDisplayMode == MainWindowDisplayMode.PITCHES) {
				getShell().setCursor(cursorCross);
			} else {
				getShell().setCursor(cursorArrow);
			}
			super.redraw();
		}
	}

	public void redraw(double msByPixel, int noteHeight) {
		if (!this.isDisposed()) {
			margin = (int)((double)coreData.nakloidIni.output.ms_margin/msByPixel);
			viewSize.x = (int)(((double)coreData.getScoreLength())/msByPixel)+margin;
			viewSize.y = (getMidiNoteUpperLimit()-getMidiNoteLowerLimit()+1)*noteHeight;
			int clientHeight = getClientArea().height;
			int clientWidth = getClientArea().width;
			offset.x = (int)(this.msByPixel/(double)msByPixel*(offset.x-(clientWidth/2))) + (clientWidth/2);
			offset.y = (int)((double)noteHeight/this.noteHeight*(offset.y-(clientHeight/2))) + (clientHeight/2);
			if (-offset.x+clientWidth > viewSize.x || offset.x > 0) {
				offset.x = 0;
			}
			if (-offset.y+clientHeight > viewSize.y) {
				offset.y = clientHeight - viewSize.y;
			} else if (offset.y > 0) {
				offset.y = 0;
			}
			reloadScrollBarsBaseData();
			verticalBar.setSelection(-offset.y);
			horizontalBar.setSelection(-offset.x);
			scroll(offset.x, offset.y, offset.x, offset.y, viewSize.x, viewSize.y, false);
			this.msByPixel = msByPixel;
			this.noteHeight = noteHeight;
			super.redraw();
		}
	}

	public Point getTimelineSize() {
		return viewSize;
	}

	public Point getOffset() {
		return offset;
	}

	@Override
	public void updateVocal() {}

	@Override
	public void updatePitches() {}

	@Override
	public void updateScore() {
		margin = (int)((double)coreData.nakloidIni.output.ms_margin/msByPixel);
		viewSize.x = (int)(((double)coreData.getScoreLength())/msByPixel)+margin;
		viewSize.y = (getMidiNoteUpperLimit()-getMidiNoteLowerLimit()+1)*noteHeight;
		reloadScrollBarsBaseData();
	}

	@Override
	public void updateSongWaveform() {}

	@Override
	public void updateSaveState() {}

	private class CanvasPaintListener implements PaintListener {
		Rectangle clientArea = getClientArea();
		@Override
		public void paintControl(PaintEvent e) {
			if (clientArea != getClientArea()) {
				clientArea = getClientArea();
				reloadScrollBarsBaseData();
			}
			Image image = new Image(e.display, getClientArea().width, getClientArea().height);
			GC gcImage = new GC(image);
			gcImage.setAntialias(SWT.ON);

			// draw piano-roll background
			for (int i=0; i<getMidiNoteUpperLimit()-getMidiNoteLowerLimit()+1; i++) {
				int numMidiNote = getMidiNoteUpperLimit()-i;
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
				if (MusicalScales.getMusicalScalesFromNumMidiNote(numMidiNote).isMajor()) {
					gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_GRAY));
					gcImage.fillRectangle(new Rectangle(offset.x, i*noteHeight+offset.y, viewSize.x, noteHeight));
				}
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
				gcImage.drawLine(offset.x, (i+1)*noteHeight+offset.y, viewSize.x+offset.x, (i+1)*noteHeight+offset.y);
			}
			if (clientArea.width > viewSize.x) {
				gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				gcImage.fillRectangle(new Rectangle(viewSize.x, 0, clientArea.width-viewSize.x, clientArea.height));
			}

			// draw notes
			for (Note tmpNote : coreData.getNotes()) {
				if (tmpNote.getBasePitch()<getMidiNoteUpperLimit() && tmpNote.getBasePitch()>getMidiNoteLowerLimit()) {
					Point tmpPoint = new Point((int)(margin+(tmpNote.getStart()/msByPixel))+offset.x,
							(getMidiNoteUpperLimit()-tmpNote.getBasePitch())*noteHeight+offset.y);
					Rectangle tmpRectangle = new Rectangle(tmpPoint.x, tmpPoint.y, (int)(tmpNote.getLength()/msByPixel), noteHeight);
					if (displayMode == MainWindowDisplayMode.PITCHES) {
						Rectangle rectPadding = new Rectangle(tmpRectangle.x-(int)(tmpNote.getFrontPadding()/msByPixel), tmpRectangle.y,
								tmpRectangle.width+(int)((tmpNote.getFrontPadding()-tmpNote.getBackPadding())/msByPixel), tmpRectangle.height);
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_YELLOW));
						gcImage.drawRectangle(rectPadding);
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
						gcImage.drawRectangle(tmpRectangle);
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
						gcImage.drawText(tmpNote.getPronunciationAliasString(), tmpPoint.x+3, tmpPoint.y+3, true);
					} else {
						gcImage.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_MAGENTA));
						gcImage.fillRectangle(tmpRectangle);
						gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_WHITE));
						gcImage.drawRectangle(tmpRectangle);
						gcImage.drawText(tmpNote.getPronunciationAliasString(), tmpPoint.x+3, tmpPoint.y+3, true);
					}
				}
			}

			// draw pitches
			if (displayMode==MainWindowDisplayMode.PITCHES && coreData.getPitches()!=null && coreData.getPitches().size()>0) {
				// saved pitches
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_CYAN));
				double[] midiNoteNumbers = new double[coreData.getPitches().size()];
				midiNoteNumbers = coreData.getPitches().getMidiNoteNumbers();
				int[] tmpPitchPositions = new int[(int)(midiNoteNumbers.length/msByPixel)*2+2];
				tmpPitchPositions[0] = 0;
				tmpPitchPositions[1] = viewSize.y;
				for (int i=0; i<tmpPitchPositions.length/2-1; i++) {
					tmpPitchPositions[(i+1)*2] = (int)(i+offset.x);
					double tmpPitch = midiNoteNumbers[(int)(i*msByPixel)];
					tmpPitchPositions[(i+1)*2+1] = (tmpPitch>getMidiNoteLowerLimit()&&tmpPitch<getMidiNoteUpperLimit())?midiNoteNumber2pos(tmpPitch):viewSize.y;
				}
				gcImage.drawPolyline(tmpPitchPositions);

				// temporary pitches
				gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_CYAN));
				int[] tmpCursorLocus = new int[cursorLocus.size()*2];
				int i = 0;
				for (Entry<Integer, Integer> entry : cursorLocus.entrySet()) {
					tmpCursorLocus[i] = entry.getKey();
					tmpCursorLocus[i+1] = entry.getValue();
					i += 2;
				}
				gcImage.drawPolyline(tmpCursorLocus);
			}

			// draw seekbar
			gcImage.setForeground(e.display.getSystemColor(SWT.COLOR_MAGENTA));
			if (coreData.getSongWaveform()!=null && coreData.getSongWaveform().isLoaded()) {
				int tmpSeekPoint = (int)(coreData.getSongWaveform().getMicrosecond()/1000/msByPixel + offset.x);
				gcImage.drawLine(tmpSeekPoint, 0, tmpSeekPoint, viewSize.y);
			}

			e.gc.drawImage(image, 0, 0);
			gcImage.dispose();
			image.dispose();
		}
	}

	private class CanvasMouseListener implements MouseListener, MouseWheelListener, MouseMoveListener, MouseTrackListener {
		boolean writingMode = false;
		int maxCursorX=0, minCursorX=0;
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			Point clickPoint = new Point(e.x, e.y);
			Note clickedNote = null;
			for (Note tmpNote : coreData.getNotes()) {
				if (tmpNote.getBasePitch()<getMidiNoteUpperLimit() && tmpNote.getBasePitch()>getMidiNoteLowerLimit()) {
					Point tmpPoint = new Point((int)(tmpNote.getStart()/msByPixel)+margin+offset.x,
							(getMidiNoteUpperLimit()-tmpNote.getBasePitch())*noteHeight+offset.y);
					Rectangle tmpRectangle = new Rectangle(tmpPoint.x, tmpPoint.y, (int)(tmpNote.getLength()/msByPixel), noteHeight);
					if (tmpRectangle.contains(clickPoint)) {
						clickedNote = tmpNote;
						break;
					}
				}
			}
			if (clickedNote != null) {
				NoteOption dialog = new NoteOption(getShell(), coreData, clickedNote);
				dialog.open();
			}
		}

		@Override
		public void mouseScrolled(MouseEvent e) {
			verticalBar.setSelection(verticalBar.getSelection()-e.count);
		}

		@Override
		public void mouseMove(MouseEvent e) {
			if (writingMode && getClientArea().contains(e.x, e.y)) {
				if (e.x > maxCursorX) {
					maxCursorX = e.x;
				} else if (e.x < minCursorX) {
					minCursorX = e.x;
				} else {
					return;
				}
				cursorLocus.put(e.x, e.y);
				mainViewListeners.stream().forEach(MainViewListener::pitchesDrawn);
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (displayMode == MainWindowDisplayMode.PITCHES) {
				writingMode = true;
				maxCursorX = minCursorX = e.x;
				cursorLocus.put(e.x, e.y);
				mainViewListeners.stream().forEach(MainViewListener::pitchesDrawn);
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
			writingMode = false;
			if (cursorLocus.size() > 0) {
				List<Integer> cursorKeys = cursorLocus.keySet().stream()
						.map(i->(int)(i*msByPixel))
						.collect(Collectors.toList());
				Integer[] cursorValues = cursorLocus.values().toArray(new Integer[cursorLocus.size()]);
				ArrayList<Double> tmpMidiNoteNumbers = new ArrayList<Double>();
				for (int i=0; i<cursorLocus.size()-1; i++) {
					int tmp = cursorKeys.get(i+1) - cursorKeys.get(i);
					for (int j=0; j<tmp; j++) {
						tmpMidiNoteNumbers.add(pos2midiNoteNumber((cursorValues[i]*(tmp-j)+(cursorValues[i+1]*j))/tmp));
					}
				}
				tmpMidiNoteNumbers.add(pos2midiNoteNumber(cursorValues[cursorValues.length-1]));
				coreData.replaceMidiNoteNumbers(tmpMidiNoteNumbers, (int)((minCursorX-offset.x)*msByPixel));
				try {
					coreData.savePitches();
				} catch (IOException ex) {
					ErrorDialog.openError(getShell(), "NakloidGUI",
							"ピッチ情報の保存に失敗しました。",
							new MultiStatus(".", IStatus.ERROR,
									Stream.of(ex.getStackTrace())
											.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
											.collect(Collectors.toList()).toArray(new Status[]{}),
									ex.getLocalizedMessage(), ex));
				}
				cursorLocus.clear();
				mainViewListeners.stream().forEach(MainViewListener::pitchesDrawn);
			}
		}

		@Override
		public void mouseEnter(MouseEvent e) {
			if (displayMode == MainWindowDisplayMode.PITCHES) {
				getShell().setCursor(cursorCross);
			}
		}

		@Override
		public void mouseExit(MouseEvent e) {
			getShell().setCursor(cursorArrow);
		}

		@Override
		public void mouseHover(MouseEvent e) {}
	}

	private void reloadScrollBarsBaseData() {
		Rectangle clientArea = getClientArea();
		if (clientArea.width>0 && viewSize.x>clientArea.width) {
			horizontalBar.setEnabled(true);
			horizontalBar.setMaximum(viewSize.x);
			horizontalBar.setPageIncrement((int)(clientArea.width*0.8));
			horizontalBar.setIncrement(10);
			horizontalBar.setThumb(clientArea.width);
		} else {
			horizontalBar.setEnabled(false);
		}
		if (clientArea.height>0 && viewSize.y > clientArea.height) {
			verticalBar.setEnabled(true);
			verticalBar.setMaximum(viewSize.y);
			verticalBar.setPageIncrement((int)(clientArea.height*0.8));
			verticalBar.setIncrement(10);
			verticalBar.setThumb(clientArea.height);
		} else {
			verticalBar.setEnabled(false);
		}
	}

	private int getMidiNoteUpperLimit() {
		return NakloidGUI.preferenceStore.getInt("gui.mainWindow.numMidiNoteUpperLimit");
	}

	private int getMidiNoteLowerLimit() {
		return NakloidGUI.preferenceStore.getInt("gui.mainWindow.numMidiNoteLowerLimit");
	}

	private int midiNoteNumber2pos(double midiNoteNumber) {
		return (int)((getMidiNoteUpperLimit()-midiNoteNumber)*noteHeight) + offset.y + (noteHeight/2);
	}

	private double pos2midiNoteNumber(int pos) {
		return getMidiNoteUpperLimit() - (((double)pos-(noteHeight/2+offset.y))/noteHeight);
	}
}
