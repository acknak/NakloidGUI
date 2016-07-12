package nak.nakloidGUI.gui;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.actions.displays.DisplayHorizontalZoomInAction;
import nak.nakloidGUI.actions.displays.DisplayHorizontalZoomOutAction;
import nak.nakloidGUI.actions.displays.DisplayLogAction;
import nak.nakloidGUI.actions.displays.DisplayNotesAction;
import nak.nakloidGUI.actions.displays.DisplayPitchesAction;
import nak.nakloidGUI.actions.displays.DisplayVerticalZoomInAction;
import nak.nakloidGUI.actions.displays.DisplayVerticalZoomOutAction;
import nak.nakloidGUI.actions.displays.DisplayZoomInAction;
import nak.nakloidGUI.actions.displays.DisplayZoomOutAction;
import nak.nakloidGUI.actions.editors.AddNoteAction;
import nak.nakloidGUI.actions.editors.EditLyricsAction;
import nak.nakloidGUI.actions.executors.BuildAction;
import nak.nakloidGUI.actions.executors.BuildAndPlayAction;
import nak.nakloidGUI.actions.executors.ExportWavAction;
import nak.nakloidGUI.actions.executors.InitializePitchesAction;
import nak.nakloidGUI.actions.executors.PlayAction;
import nak.nakloidGUI.actions.files.ExitAction;
import nak.nakloidGUI.actions.files.ExportVocalAction;
import nak.nakloidGUI.actions.files.ImportScoreAction;
import nak.nakloidGUI.actions.files.ImportVocalAction;
import nak.nakloidGUI.actions.files.OpenAction;
import nak.nakloidGUI.actions.files.SaveAction;
import nak.nakloidGUI.actions.files.SaveAsAction;
import nak.nakloidGUI.actions.files.SpeechSynthesisAction;
import nak.nakloidGUI.actions.options.AboutNakloidAction;
import nak.nakloidGUI.actions.options.NakloidOptionAction;
import nak.nakloidGUI.actions.options.VocalOptionAction;
import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSubscriber;
import nak.nakloidGUI.gui.mainWindowViews.KeyboardView;
import nak.nakloidGUI.gui.mainWindowViews.MainView;
import nak.nakloidGUI.gui.mainWindowViews.MainView.MainViewListener;
import nak.nakloidGUI.gui.mainWindowViews.OverView;
import nak.nakloidGUI.gui.mainWindowViews.OverView.OverViewListener;
import nak.nakloidGUI.gui.mainWindowViews.VocalInfoView;
import nak.nakloidGUI.models.Waveform;
import nak.nakloidGUI.models.Waveform.WaveformStatus;

public class MainWindow extends ApplicationWindow implements CoreDataSubscriber, MainViewListener, OverViewListener {
	private CoreData coreData;
	private MainWindowDisplayMode displayMode;
	private OverView overView;
	private VocalInfoView vocalInfoView;
	private KeyboardView keyboardView;
	private MainView mainView;
	private LoggerWindow loggerWindow;
	private boolean displayLog = true;
	private double msByPixel = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.baseMsByPixel");
	private double noteHeight = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.baseNoteHeight");
	final public Action saveAction, saveAsAction, openAction, importNarAction, importVocalAction, exportVocalAction, speechSynthesisAction,exitAction,
			addNoteAction, editLyricsAction, displayNotesAction, displayPitchesAction, displayLogAction, displayZoomInAction, displayZoomOutAction,
			displayHorizontalZoomInAction, displayHorizontalZoomOutAction, displayVerticalZoomInAction, displayVerticalZoomOutAction,
			playAction, buildAction, buildAndPlayAction, exportWavAction, initializePitchesAction,
			nakloidOptionAction, vocalOptionAction, aboutNakloidAction;

	static public enum MusicalScales {
		C(0),C_MAJOR(1),D(2),D_MAJOR(3),E(4),F(5),F_MAJOR(6),G(7),G_MAJOR(8),A(9),A_MAJOR(10),B(11);
		private static final String[] MusicalScaleStrings = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
		private static final MusicalScales[] MusicalScaleInstances = {C,C_MAJOR,D,D_MAJOR,E,F,F_MAJOR,G,G_MAJOR,A,A_MAJOR,B};
		private final int id;
		private MusicalScales(int id) {
			this.id = id;
		}
		public String getString() {
			return MusicalScaleStrings[id];
		}
		public boolean isMajor() {
			return MusicalScaleStrings[id].endsWith("#");
		}
		public boolean equals(int numMidiNote) {
			return id == numMidiNote%12;
		}
		public static String getStringFromNumMidiNote(int numMidiNote, boolean withOctave) {
			return MusicalScaleStrings[numMidiNote%12]+((withOctave)?Integer.toString(numMidiNote/12-1):"");
		}
		public static MusicalScales getMusicalScalesFromNumMidiNote(int numMidiNote) {
			return MusicalScaleInstances[numMidiNote%12];
		}
	}

	public enum MainWindowDisplayMode {NOTES, PITCHES}

	public MainWindow() {
		super(null);

		Shell shellSplash = new Shell(SWT.ON_TOP);
		shellSplash.setLayout(new FillLayout());
		Image imgLoad = loadImage("icon256.png");
		Splash splash = new Splash(shellSplash, imgLoad);
		splash.setText("読み込み中...");
		shellSplash.open();

		StringBuilder sb = new StringBuilder();
		try {
			NakloidGUI.preferenceStore.load();
		} catch (IOException e) {
			sb.append("設定ファイルが読み込めませんでした。デフォルトに戻します。\n");
			try {
				NakloidGUI.preferenceStore.save();
			} catch (IOException e1) {
				sb.append("設定ファイルを保存できませんでした。設定は次回起動時にデフォルトに戻されます。\n");
			}
		}
		CoreData.Builder cdb = new CoreData.Builder();

		splash.setText("歌手情報を読み込み中...");
		try {
			cdb.loadOtoIni();
		} catch (IOException e) {
			sb.append("歌手情報が読み込めませんでした。\n");
		}
		if (!NakloidGUI.preferenceStore.getString("workspace.path_nar").isEmpty()) {
			splash.setText("最後に開いたnarファイルを読み込み中...");
			try {
				OpenAction.open(NakloidGUI.preferenceStore.getString("workspace.path_nar"));
			} catch (IOException e) {
				sb.append("narファイルが読み込めませんでした。\n");
			}
			splash.setText("楽譜情報を読み込み中...");
			try {
				cdb.loadScore();
			} catch (IOException e) {
				sb.append("楽譜情報が読み込めませんでした。\n");
			}
			splash.setText("ピッチ情報を読み込み中...");
			try {
				cdb.loadPitches();
			} catch (IOException e) {
				sb.append("ピッチ情報が読み込めませんでした。\n");
			}
		}

		splash.setText("準備中...");
		coreData = cdb.build();
		coreData.addSubscribers(this);
		displayMode = MainWindowDisplayMode.NOTES;
		saveAction = new SaveAction(this, coreData);
		saveAsAction = new SaveAsAction(this, coreData);
		openAction = new OpenAction(this, coreData);
		importNarAction = new ImportScoreAction(this, coreData);
		importVocalAction = new ImportVocalAction(this, coreData);
		exportVocalAction = new ExportVocalAction(this, coreData);
		speechSynthesisAction = new SpeechSynthesisAction(this, coreData);
		exitAction = new ExitAction(this, coreData);
		addNoteAction = new AddNoteAction(this, coreData);
		editLyricsAction = new EditLyricsAction(this, coreData);
		displayNotesAction = new DisplayNotesAction(this, coreData);
		displayPitchesAction = new DisplayPitchesAction(this, coreData);
		displayLogAction = new DisplayLogAction(this, coreData);
		displayZoomInAction = new DisplayZoomInAction(this, coreData);
		displayZoomOutAction = new DisplayZoomOutAction(this, coreData);
		displayHorizontalZoomInAction = new DisplayHorizontalZoomInAction(this, coreData);
		displayHorizontalZoomOutAction = new DisplayHorizontalZoomOutAction(this, coreData);
		displayVerticalZoomInAction = new DisplayVerticalZoomInAction(this, coreData);
		displayVerticalZoomOutAction = new DisplayVerticalZoomOutAction(this, coreData);
		playAction = new PlayAction(this, coreData);
		buildAction = new BuildAction(this, coreData);
		buildAndPlayAction = new BuildAndPlayAction(this, coreData);
		exportWavAction = new ExportWavAction(this, coreData);
		initializePitchesAction = new InitializePitchesAction(this, coreData);
		nakloidOptionAction = new NakloidOptionAction(this, coreData);
		vocalOptionAction = new VocalOptionAction(this, coreData);
		aboutNakloidAction = new AboutNakloidAction(this, coreData);
		addMenuBar();

		if (sb.length() > 0) {
			MessageDialog.openWarning(shellSplash, "NakloidGUI", sb.toString());
		}

		shellSplash.close();
		imgLoad.dispose();
		splash.dispose();
		shellSplash.dispose();
	}

	@Override
	protected final void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getWindowName());
		Image[] images = new Image[5];
		images[0] = loadImage("icon16.png");
		images[1] = loadImage("icon32.png");
		images[2] = loadImage("icon64.png");
		images[3] = loadImage("icon128.png");
		images[4] = loadImage("icon256.png");
		shell.setImages(images);
		shell.setSize(900, 600);
		shell.setMaximized(true);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		{
			GridLayout layContainer = new GridLayout(1, false);
			layContainer.marginHeight = layContainer.horizontalSpacing = layContainer.marginWidth = layContainer.verticalSpacing = 0;
			container.setLayout(layContainer);
			container.addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {}
				public void controlResized(ControlEvent e) {
					overView.redraw();
					keyboardView.redraw();
					mainView.redraw();
				}
			});
		}
		{
			Composite cntPlayer = new Composite(container, SWT.NONE);
			GridLayout layCntPlayer = new GridLayout(3, false);
			layCntPlayer.marginHeight = layCntPlayer.marginWidth = layCntPlayer.verticalSpacing = 0;
			layCntPlayer.marginLeft = 3;
			layCntPlayer.marginBottom = 3;
			layCntPlayer.horizontalSpacing = 3;
			cntPlayer.setLayout(layCntPlayer);
			GridData gdCntPlayer = new GridData(GridData.FILL_HORIZONTAL);
			int headerHeight = NakloidGUI.preferenceStore.getInt("gui.mainWindow.headerHeight");
			gdCntPlayer.heightHint = headerHeight;
			cntPlayer.setLayoutData(gdCntPlayer);
			{
				vocalInfoView = new VocalInfoView(cntPlayer, coreData.getVocalInfo());
				GridData gdlblLoadImage = new GridData(GridData.FILL_VERTICAL);
				gdlblLoadImage.heightHint = headerHeight;
				gdlblLoadImage.widthHint = headerHeight;
				vocalInfoView.setLayoutData(gdlblLoadImage);
			}
			{
				overView = new OverView(cntPlayer, coreData.getSongWaveform());
				overView.addOverViewListener(this);
			}
			{
				Scale sclPlayer = new Scale(cntPlayer, SWT.VERTICAL);
				sclPlayer.setMinimum(0);
				sclPlayer.setMaximum(100);
				sclPlayer.setIncrement(20);
				sclPlayer.setSelection(0);
				GridData gdSclPlayer = new GridData(GridData.FILL_VERTICAL);
				sclPlayer.setLayoutData(gdSclPlayer);
				sclPlayer.addListener(SWT.MouseWheel, new Listener(){
					@Override
					public void handleEvent(Event e) {
						e.doit = false;
					}
				});
				sclPlayer.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e){
						if (coreData.getSongWaveform()!=null && coreData.getSongWaveform().isLoaded()) {
							coreData.getSongWaveform().setVolume(100-sclPlayer.getSelection());
						}
					}
				});
			}
		}
		{
			Composite cntMainView = new Composite(container, SWT.NONE);
			GridLayout layCntMainView = new GridLayout(2, false);
			layCntMainView.marginHeight = layCntMainView.horizontalSpacing = layCntMainView.marginWidth = layCntMainView.verticalSpacing = 0;
			cntMainView.setLayout(layCntMainView);
			GridData gdCntMainView = new GridData(GridData.FILL_BOTH);
			cntMainView.setLayoutData(gdCntMainView);
			keyboardView = new KeyboardView(cntMainView);
			mainView = new MainView(cntMainView, coreData);
			mainView.addMainViewListener(this);
			mainView.setFocus();
		}
		if (displayLog) {
			displayLogAction.run();
		}

		if (coreData.getNotes().size()>0 && coreData.getVoicesSize()>0) {
			try {
				coreData.synthesize();
			} catch (IOException | InterruptedException e) {}
		}

		return container;
	}

	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("");
		{
			MenuManager exitMenu = new MenuManager("ファイル(&F)");
			menuBar.add(exitMenu);
			exitMenu.add(saveAction);
			exitMenu.add(saveAsAction);
			exitMenu.add(openAction);
			exitMenu.add(new org.eclipse.jface.action.Separator());
			exitMenu.add(importNarAction);
			exitMenu.add(importVocalAction);
			exitMenu.add(exportVocalAction);
			exitMenu.add(new org.eclipse.jface.action.Separator());
			exitMenu.add(speechSynthesisAction);
			exitMenu.add(new org.eclipse.jface.action.Separator());
			exitMenu.add(exitAction);
		}
		{
			MenuManager displayMenu = new MenuManager("編集(&E)");
			menuBar.add(displayMenu);
			displayMenu.add(addNoteAction);
			displayMenu.add(editLyricsAction);
		}
		{
			MenuManager displayMenu = new MenuManager("表示(&D)");
			menuBar.add(displayMenu);
			displayMenu.add(displayNotesAction);
			displayMenu.add(displayPitchesAction);
			displayMenu.add(displayLogAction);
			displayMenu.add(new org.eclipse.jface.action.Separator());
			displayMenu.add(displayZoomInAction);
			displayMenu.add(displayZoomOutAction);
			displayMenu.add(displayHorizontalZoomInAction);
			displayMenu.add(displayHorizontalZoomOutAction);
			displayMenu.add(displayVerticalZoomInAction);
			displayMenu.add(displayVerticalZoomOutAction);
		}
		{
			MenuManager editMenu = new MenuManager("実行(&R)");
			menuBar.add(editMenu);
			editMenu.add(playAction);
			editMenu.add(buildAction);
			editMenu.add(buildAndPlayAction);
			editMenu.add(exportWavAction);
			editMenu.add(new org.eclipse.jface.action.Separator());
			editMenu.add(initializePitchesAction);
		}
		{
			MenuManager optionMenu = new MenuManager("オプション(&O)");
			menuBar.add(optionMenu);
			optionMenu.add(nakloidOptionAction);
			optionMenu.add(vocalOptionAction);
			optionMenu.add(aboutNakloidAction);
		}
		return menuBar;
	}

	@Override
	protected boolean canHandleShellCloseEvent() {
		return showSaveConfirmDialog();
	}

	public boolean showSaveConfirmDialog() {
		if (!coreData.isSaved()) {
			MessageDialog dialog = new MessageDialog(
					getShell(),
					"NakloidGUI",
					null,
					"楽譜が変更されています。変更を保存しますか？",
					MessageDialog.QUESTION,
					new String[] { "はい", "いいえ", "キャンセル" },
					0);
			int result = dialog.open();
			if (result == 0) {
				saveAction.run();
				return true;
			} else if (result == 1) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateScore() {
		mainView.redraw();
		updateWindowName();
	}

	@Override
	public void updatePitches() {
		mainView.redraw();
		updateWindowName();
	}

	@Override
	public void updateVocal() {
		overView.redraw((Waveform)coreData.getSongWaveform());
		vocalInfoView.redraw(coreData.getVocalInfo());
	}

	@Override
	public void updateSongWaveform() {
		if (coreData.getSongWaveform() == null) {
			showWaveformStatus("歌声未生成");
			return;
		}
		showWaveformStatus("歌声読込中...");
		Display.getCurrent().syncExec(new Runnable() {
			@Override
			public void run() {
				if (coreData.getSongWaveform().getStatus()==WaveformStatus.LOADING) {
					Display.getCurrent().timerExec(100, this);
					return;
				}
				if (coreData.getSongWaveform().isLoaded()) {
					overView.redraw(coreData.getSongWaveform(), mainView.getClientArea().width, mainView.getOffset().x, msByPixel);
					mainView.redraw();
					return;
				}
				showWaveformStatus("歌声が合成されていません");
			}
		});
	}

	@Override
	public void mainViewHorizontalBarUpdated(SelectionEvent e) {
		overView.redraw(mainView.getClientArea().width, mainView.getOffset().x, msByPixel);
	}

	@Override
	public void mainViewVerticalBarUpdated(SelectionEvent e) {
		keyboardView.redraw(mainView.getOffset().y, (int)noteHeight);
	}

	@Override
	public void pitchesDrawn() {
		mainView.redraw();
		mainView.update();
	}

	@Override
	public void waveformSeeked() {
		if (getShell()!=null && !getShell().isDisposed()) {
			overView.redraw();
			mainView.redraw();
		}
	}

	public void preferenceReloaded() {
		try {
			coreData.reloadPreference();
			mainView.redraw();
			keyboardView.redraw();
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), "NakloidGUI",
					"NakloidGUIの設定ファイル（nakloidGUI.properties）の再読込に失敗しました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		}
	}

	public MainWindowDisplayMode getDisplayMode() {
		return displayMode;
	}

	public void displayNotes() {
		displayMode = MainWindowDisplayMode.NOTES;
		mainView.redraw(MainWindowDisplayMode.NOTES);
	}

	public void displayPitches() {
		displayMode = MainWindowDisplayMode.PITCHES;
		mainView.redraw(MainWindowDisplayMode.PITCHES);
	}

	public boolean displayingLog() {
		return displayLog;
	}

	public void displayLog(boolean displayLog) {
		this.displayLog = displayLog;
		if (displayLog) {
			loggerWindow = new LoggerWindow(getShell());
			loggerWindow.open();
			mainView.forceFocus();
		} else {
			loggerWindow.close();
			loggerWindow = null;
		}
	}

	public int getMainViewWidth() {
		return mainView.getClientArea().width;
	}

	public int getMainViewHorizontalOffset() {
		return mainView.getOffset().x;
	}

	public int getMainViewVerticalOffset() {
		return mainView.getOffset().y;
	}

	public void setHorizontalScale(double scale) {
		double upperLimit = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.msByPixelUpperLimit");
		double lowerLimit = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.msByPixelLowerLimit");
		msByPixel *= scale;
		if (msByPixel > upperLimit) {
			msByPixel = upperLimit;
		} else if (msByPixel < lowerLimit) {
			msByPixel = lowerLimit;
		}
		mainView.redraw(msByPixel, (int)noteHeight);
		overView.redraw(mainView.getClientArea().width, mainView.getOffset().x, msByPixel);
	}

	public void setVerticalScale(double scale) {
		double upperLimit = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.noteHeightUpperLimit");
		double lowerLimit = NakloidGUI.preferenceStore.getDouble("gui.mainWindow.noteHeightLowerLimit");
		noteHeight *= scale;
		if (noteHeight > upperLimit) {
			noteHeight = upperLimit;
		} else if (noteHeight < lowerLimit) {
			noteHeight = lowerLimit;
		}
		mainView.redraw(msByPixel, (int)noteHeight);
		keyboardView.redraw(mainView.getOffset().y, (int)noteHeight);
	}

	public void flushLoggerWindow() {
		if (loggerWindow != null) {
			loggerWindow.flush();
		}
	}

	public void showWaveformStatus(String message) {
		overView.redraw(message);
	}

	private Image loadImage(String filename) {
		return ImageDescriptor.createFromURL(getClass().getResource(filename)).createImage();
	}

	private String getWindowName() {
		String windowName = " - NakloidGUI";
		String fileName = NakloidGUI.preferenceStore.getString("workspace.path_nar");
		if (fileName!=null && !fileName.isEmpty()) {
			windowName = Paths.get(fileName).toFile().getName() + (coreData.isSaved()?"":"*") + windowName;
		} else {
			windowName = "（無題）" +  (coreData.getNotes().size()>0?"*":"") + windowName;
		}
		return windowName;
	}

	public void updateWindowName() {
		if (getShell() != null) {
			getShell().setText(getWindowName());
		}
	}
}
