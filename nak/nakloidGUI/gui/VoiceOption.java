package nak.nakloidGUI.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSynthesisListener;
import nak.nakloidGUI.gui.voiceOptionViews.OverView;
import nak.nakloidGUI.gui.voiceOptionViews.SongView;
import nak.nakloidGUI.gui.voiceOptionViews.VoiceView;
import nak.nakloidGUI.gui.voiceOptionViews.VoiceView.VoiceViewListener;
import nak.nakloidGUI.models.Note;
import nak.nakloidGUI.models.Pmp;
import nak.nakloidGUI.models.Voice;
import nak.nakloidGUI.models.Voice.ParameterType;
import nak.nakloidGUI.models.Waveform;
import nak.nakloidGUI.models.Waveform.WaveformStatus;

public class VoiceOption extends Dialog implements VoiceViewListener {
	private final CoreData coreData;
	private CoreData tmpCoreData;
	private final Voice voiceOriginal, voicePrefix;
	private Voice tmpVoice;
	private final Pmp pmpOriginal;
	private Pmp tmpPmp;
	private Text txtOffset, txtOverlap, txtPreutterance, txtConsonant, txtBlank, txtMusicalScale;
	private OverView overView;
	private VoiceView voiceView;
	private SongView songView;
	private Text txtPmp;
	private Button btnPlayer, chkShowPmpTeacher, chkShowPmpPrefixTeacher;
	private Scale sclPlayer;
	private Optional<Waveform> wfBase, wfPmpTeacher, wfPmpPrefixTeacher, wfSong;
	private boolean showAllRangePmp = false;
	final private Path pathOtoIniTemporary, pathVoiceTemporary, pathPmpTemporary;
	private Set<Path> temporaryPaths = new HashSet<Path>();


	protected VoiceOption(Shell shell, CoreData coreData , Voice voice) throws IOException {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.coreData = coreData;

		this.voiceOriginal = tmpVoice = voice;
		if (!Files.exists(voice.getPmpPath())) {
			coreData.makePmp(voice.getPronunciationString());
			reloadPmpFileWithNakloid();
		}
		pmpOriginal = tmpPmp = new Pmp.Builder(voice).build();
		wfBase = wfSong = wfPmpTeacher = wfPmpPrefixTeacher = Optional.empty();
		pathOtoIniTemporary = Paths.get("temporary","oto.ini");
		pathPmpTemporary = Paths.get("temporary", voice.getPmpPath().getFileName().toString());
		pathVoiceTemporary = Paths.get("temporary", voice.getWavPath().getFileName().toString());

		Files.copy(voice.getWavPath(), pathVoiceTemporary, StandardCopyOption.REPLACE_EXISTING);
		temporaryPaths.add(pathVoiceTemporary);
		Files.copy(voice.getPmpPath(), pathPmpTemporary, StandardCopyOption.REPLACE_EXISTING);
		temporaryPaths.add(pathPmpTemporary);
		if (voice.isVCV()) {
			voicePrefix = coreData.getVoice(tmpVoice.getPronunciationAlias().getPrefixPron()+tmpVoice.getPronunciationAlias().getSuffix());
			Path prefixVoicePath = Paths.get("temporary", voicePrefix.getWavPath().getFileName().toString());
			Files.copy(voicePrefix.getWavPath(), prefixVoicePath, StandardCopyOption.REPLACE_EXISTING);
			temporaryPaths.add(prefixVoicePath);
			Path prefixVoicePmpPath = Paths.get("temporary", voicePrefix.getPmpPath().getFileName().toString());
			Files.copy(voicePrefix.getPmpPath(), prefixVoicePmpPath, StandardCopyOption.REPLACE_EXISTING);
			temporaryPaths.add(prefixVoicePmpPath);
		} else {
			voicePrefix = null;
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1000, 865);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		control.getShell().setText("\""+voiceOriginal.getPronunciationString()+"\"の原音設定");
		return control;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		wfBase.ifPresent(Waveform::close);
		wfPmpTeacher.ifPresent(Waveform::close);
		wfPmpPrefixTeacher.ifPresent(Waveform::close);
		wfSong.ifPresent(Waveform::close);
		if (buttonId==IDialogConstants.OK_ID && (!voiceOriginal.equals(tmpVoice)||!tmpPmp.equals(pmpOriginal))) {
			try {
				if (pmpOriginal.path != null) {
					tmpPmp.save(pmpOriginal.path);
				}
				for (Path path : temporaryPaths) {
					if (path != null) {
						Files.deleteIfExists(path);
					}
				}
				Files.deleteIfExists(tmpVoice.getUwcPath());
				coreData.saveVoice(tmpVoice);
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "NakloidGUI", "ファイル入出力に失敗しました。\n"+e.getMessage());
			}
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void handleShellCloseEvent() {
		buttonPressed(IDialogConstants.CANCEL_ID);
		super.handleShellCloseEvent();
	};

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "完了", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "キャンセル", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		{
			Group grpParameters = new Group(container, SWT.NONE);
			grpParameters.setText("\""+voiceOriginal.getPronunciationString()+"\"のパラメータ");
			grpParameters.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			grpParameters.setLayout(new GridLayout(5, false));
			createParametersText(grpParameters, Voice.ParameterType.OFFSET);
			createParametersText(grpParameters, Voice.ParameterType.OVERLAP);
			createParametersText(grpParameters, Voice.ParameterType.PREUTTERANCE);
			createParametersText(grpParameters, Voice.ParameterType.CONSONANT);
			createParametersText(grpParameters, Voice.ParameterType.BLANK);
			txtOffset.setText(String.valueOf(voiceOriginal.getOffset()));
			txtOverlap.setText(String.valueOf(voiceOriginal.getOverlap()));
			txtPreutterance.setText(String.valueOf(voiceOriginal.getPreutterance()));
			txtConsonant.setText(String.valueOf(voiceOriginal.getConsonant()));
			txtBlank.setText(String.valueOf(voiceOriginal.getBlank()));
			overView = new OverView(grpParameters, voiceOriginal);
		}
		{
			Group grpPitchMarks = new Group(container, SWT.NONE);
			grpPitchMarks.setText("\""+voiceOriginal.getPronunciationString()+"\"のピッチマーク");
			grpPitchMarks.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			grpPitchMarks.setLayout(new GridLayout());
			voiceView = new VoiceView(grpPitchMarks, voiceOriginal, pmpOriginal);
			voiceView.setVoiceViewListener(this);
			txtPmp = new Text(grpPitchMarks, SWT.WRAP|SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
			{
				GridData layTxtPmp = new GridData(GridData.FILL_HORIZONTAL);
				layTxtPmp.heightHint = 100;
				txtPmp.setLayoutData(layTxtPmp);
				txtPmp.setText(pmpOriginal.getPitchmarkPointsString());
				updateTxtPmp();
				txtPmp.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						updatePmp();
					}
				});
			}
			{
				Composite cntPmpRepeatPoint = (Composite)super.createDialogArea(grpPitchMarks);
				RowLayout layPmpRepeatPoint = new RowLayout();
				layPmpRepeatPoint.wrap = true;
				layPmpRepeatPoint.center = true;
				cntPmpRepeatPoint.setLayout(layPmpRepeatPoint);
				{
					Label lbRepeatPoint = new Label(cntPmpRepeatPoint, SWT.NONE);
					lbRepeatPoint.setText("フェード開始点");
					Text txtRepeatPoint = new Text(cntPmpRepeatPoint, SWT.BORDER|SWT.RIGHT);
					txtRepeatPoint.setText(String.valueOf(pmpOriginal.getSubFadeStart()));
					txtRepeatPoint.setLayoutData(new RowData(40,12));
					txtRepeatPoint.addModifyListener(new ModifyListener() {
						@Override
						public void modifyText(ModifyEvent e) {
							tmpPmp = new Pmp.Builder(tmpPmp).setSubFadeStart(Short.valueOf(((Text)e.getSource()).getText())).build();
							voiceView.redraw(tmpPmp);
						}
					});
				}
			}
			{
				Composite cntPmpOptions = (Composite)super.createDialogArea(grpPitchMarks);
				RowLayout layPmpOptions = new RowLayout();
				layPmpOptions.wrap = true;
				layPmpOptions.center = true;
				cntPmpOptions.setLayout(layPmpOptions);
				{
					Button chkPmpRangeAll = new Button(cntPmpOptions, SWT.CHECK);
					chkPmpRangeAll.setText("全体を表示");
					chkPmpRangeAll.setBounds(10,10,120,24);
					chkPmpRangeAll.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e){
							showAllRangePmp = ((Button)e.widget).getSelection();
							updateTxtPmp();
						}
					});
				}
				{
					Button chkShowPmpPoint = new Button(cntPmpOptions, SWT.CHECK);
					chkShowPmpPoint.setSelection(true);
					chkShowPmpPoint.setText("位置番号を表示");
					chkShowPmpPoint.setBounds(10,10,120,24);
					chkShowPmpPoint.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e){
							voiceView.redraw(((Button)e.widget).getSelection(), voiceView.showUpsideDown(), voiceView.showPmpTeacher(), voiceView.showPmpPrefixTeacher());
						}
					});
				}
				{
					Button chkWaveform2UpsideDown = new Button(cntPmpOptions, SWT.CHECK);
					chkWaveform2UpsideDown.setText("波形表示の上下を逆転");
					chkWaveform2UpsideDown.setBounds(10,10,120,24);
					chkWaveform2UpsideDown.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e){
							voiceView.redraw(voiceView.showPmpPoint(), ((Button)e.widget).getSelection(), voiceView.showPmpTeacher(), voiceView.showPmpPrefixTeacher());
						}
					});
				}
				{
					chkShowPmpTeacher = new Button(cntPmpOptions, SWT.CHECK);
					chkShowPmpTeacher.setText("教師信号(母音)を表示");
					chkShowPmpTeacher.setBounds(10,10,120,24);
					chkShowPmpTeacher.setEnabled(false);
					chkShowPmpTeacher.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e){
							voiceView.redraw(voiceView.showPmpPoint(), voiceView.showUpsideDown(), ((Button)e.widget).getSelection(), voiceView.showPmpPrefixTeacher());
						}
					});
				}
				{
					chkShowPmpPrefixTeacher = new Button(cntPmpOptions, SWT.CHECK);
					chkShowPmpPrefixTeacher.setText("教師信号(先行母音)を表示");
					chkShowPmpPrefixTeacher.setBounds(10,10,120,24);
					chkShowPmpPrefixTeacher.setEnabled(false);
					chkShowPmpPrefixTeacher.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e){
							voiceView.redraw(voiceView.showPmpPoint(), voiceView.showUpsideDown(), voiceView.showPmpTeacher(), ((Button)e.widget).getSelection());
						}
					});
				}
				{
					Button btnRemakePmp = new Button(cntPmpOptions,SWT.NULL);
					btnRemakePmp.setText("ピッチマークを再計算");
					btnRemakePmp.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e){
							reloadPmpFileWithNakloid();
							voiceView.redraw(pmpOriginal);
							updateTxtPmp();
							MessageBox box1 = new MessageBox(parent.getShell(), SWT.OK);
							box1.setMessage("Nakloidでピッチマークを再計算しました");
							box1.open();
						}
					});
				}
			}
		}
		{
			Group grpPlayer = new Group(container, SWT.NONE);
			grpPlayer.setText("\""+voiceOriginal.getPronunciationString()+"\"のテスト再生");
			grpPlayer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			grpPlayer.setLayout(new GridLayout(2, false));
			{
				btnPlayer = new Button(grpPlayer, SWT.PUSH);
				btnPlayer.setText("テスト合成+再生");
				btnPlayer.addSelectionListener(new PlayAction());
				GridData layBtnPlayer = new GridData();
				layBtnPlayer.widthHint = 100;
				btnPlayer.setLayoutData(layBtnPlayer);
			}
			songView = new SongView(grpPlayer, null);
			{
				Label labelVolumeScale = new Label(grpPlayer, SWT.NONE);
				labelVolumeScale.setText("音量");
				GridData layLblVolumeScale = new GridData();
				layLblVolumeScale.verticalAlignment = SWT.CENTER;
				layLblVolumeScale.grabExcessVerticalSpace = true;
				labelVolumeScale.setLayoutData(layLblVolumeScale);
			}
			{
				sclPlayer = new Scale(grpPlayer, SWT.HORIZONTAL);
				sclPlayer.setMinimum(0);
				sclPlayer.setMaximum(100);
				sclPlayer.setIncrement(10);
				sclPlayer.setSelection(100);
				GridData laySclPlayer = new GridData();
				laySclPlayer.widthHint = 100;
				sclPlayer.setLayoutData(laySclPlayer);
				sclPlayer.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e){
						wfSong.ifPresent(w->w.setVolume(sclPlayer.getSelection()));
					}
				});
			}
			{
				Label lblMusicalScale = new Label(grpPlayer, SWT.NONE);
				lblMusicalScale.setText("MIDIノート番号");
				GridData layLblMucicalScale = new GridData();
				layLblMucicalScale.verticalAlignment = SWT.CENTER;
				layLblMucicalScale.grabExcessVerticalSpace = true;
				lblMusicalScale.setLayoutData(layLblMucicalScale);
			}
			{
				txtMusicalScale = new Text(grpPlayer, SWT.BORDER|SWT.RIGHT);
				GridData layTxtMusicalScale = new GridData();
				layTxtMusicalScale.widthHint = 90;
				layTxtMusicalScale.heightHint = 12;
				txtMusicalScale.setLayoutData(layTxtMusicalScale);
				txtMusicalScale.setText("69");
			}
		}
		loadBaseWaveform(voiceOriginal.getWavPath());
		{
			Path pathPmpTeacher = (pmpOriginal.getBaseVowelWav().filename.isEmpty())?null:Paths.get(pmpOriginal.getBaseVowelWav().filename);
			Path pathPmpPrefixTeacher = (pmpOriginal.getPrefixVowelWav().filename.isEmpty())?null:Paths.get(pmpOriginal.getPrefixVowelWav().filename);
			loadPmpTeacherWaveform(pathPmpTeacher, pathPmpPrefixTeacher);
		}
		return parent;
	}

	@Override
	public void voiceViewHorizontalBarUpdated(SelectionEvent e) {
		voiceView.redraw();
		overView.redraw(voiceView.getClientArea().width, voiceView.getOffset());
		updateTxtPmp();
	}

	@Override
	public void voiceViewResized() {
		voiceView.redraw();
		overView.redraw(voiceView.getClientArea().width, voiceView.getOffset());
		updateTxtPmp();
	}

	private final class PlayAction extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			wfSong.ifPresent(Waveform::close);
			btnPlayer.setEnabled(false);
			showSongWaveformStatus("歌声合成中...");
			synthesis();
		}
	}

	private void reloadPmpFileWithNakloid() {
		try {
			coreData.makePmp(tmpVoice.getPronunciationString());
			tmpPmp = new Pmp.Builder(tmpVoice).build();
		} catch (IOException e) {
			MessageDialog.openError(getShell(), "NakloidGUI", "ピッチマークの再読込に失敗しました。\n"+e.getMessage());
		}
	}

	private Composite createParametersText(Composite parent, Voice.ParameterType type) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layContainer = new GridLayout(4, false);
		container.setLayout(layContainer);

		Label labelCircle = new Label(container, SWT.NONE);
		labelCircle.setText("●");
		GridData labelCircleGridData = new GridData();
		labelCircleGridData.verticalAlignment = SWT.CENTER;
		labelCircleGridData.grabExcessVerticalSpace = true;
		labelCircle.setLayoutData(labelCircleGridData);

		Label label = new Label(container, SWT.NONE);
		label.setText(type.getEmString());
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.CENTER;
		labelGridData.grabExcessVerticalSpace = true;
		label.setLayoutData(labelGridData);

		Text tmpText = new Text(container, SWT.BORDER|SWT.RIGHT);
		GridData textGridData = new GridData();
		textGridData.widthHint = 60;
		textGridData.heightHint = 12;
		tmpText.setLayoutData(textGridData);
		if (type == ParameterType.OFFSET) {
			txtOffset = tmpText;
			labelCircle.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			tmpText.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent e) {
					tmpVoice = new Voice.Builder(tmpVoice).offset(Short.valueOf(((Text)e.getSource()).getText())).build();
					overView.redraw();
				}
			});
		} else if (type == ParameterType.OVERLAP) {
			txtOverlap = tmpText;
			labelCircle.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
			tmpText.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e) {
					tmpVoice = new Voice.Builder(tmpVoice).overlap(Short.valueOf(((Text)e.getSource()).getText())).build();
					overView.redraw();
				}
			});
		} else if (type == ParameterType.PREUTTERANCE) {
			txtPreutterance = tmpText;
			labelCircle.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
			tmpText.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent e) {
					tmpVoice = new Voice.Builder(tmpVoice).preutterance(Short.valueOf(((Text)e.getSource()).getText())).build();
					overView.redraw();
				}
			});
		} else if (type == ParameterType.CONSONANT) {
			txtConsonant = tmpText;
			labelCircle.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
			tmpText.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent e) {
					tmpVoice = new Voice.Builder(tmpVoice).consonant(Short.valueOf(((Text)e.getSource()).getText())).build();
					overView.redraw();
				}
			});
		} else if (type == ParameterType.BLANK) {
			txtBlank = tmpText;
			labelCircle.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			tmpText.addKeyListener(new KeyAdapter(){
				public void keyReleased(KeyEvent e) {
					tmpVoice = new Voice.Builder(tmpVoice).blank(Short.valueOf(((Text)e.getSource()).getText())).build();
					overView.redraw();
				}
			});
		}

		Label labelMs = new Label(container, SWT.NONE);
		labelMs.setText("ms");
		GridData labelMsGridData = new GridData();
		labelMsGridData.verticalAlignment = SWT.CENTER;
		labelMsGridData.grabExcessVerticalSpace = true;
		label.setLayoutData(labelMsGridData);

		return container;
	}

	private void updatePmp() {
		if (showAllRangePmp) {
			tmpPmp = new Pmp.Builder(tmpPmp).setPitchmarkPoints(txtPmp.getText()).build();
		} else {
			tmpPmp = new Pmp.Builder(tmpPmp).setPitchmarkPoints(txtPmp.getText(), -voiceView.getOffset(), -voiceView.getOffset()+voiceView.getClientArea().width).build();
		}
		voiceView.redraw(tmpPmp);
	}

	private void updateTxtPmp() {
		if (showAllRangePmp) {
			txtPmp.setText(tmpPmp.getPitchmarkPointsString());
		} else {
			txtPmp.setText(tmpPmp.getPitchmarkPointsStringFromRange(-voiceView.getOffset(), -voiceView.getOffset()+voiceView.getClientArea().width));
		}
	}

	private void showBaseWaveformStatus(String message) {
		overView.redraw(message);
		voiceView.redraw(message);
		overView.update();
	}

	private void showSongWaveformStatus(String message) {
		songView.redraw(message);
		songView.update();
	}

	private void loadBaseWaveform(Path path) {
		showBaseWaveformStatus("音声読込中...");
		wfBase = Optional.of(new Waveform(path));
		if (!Display.getCurrent().isDisposed()) {
			Display.getCurrent().timerExec(50, new Runnable() {
				@Override
				public void run() {
					wfBase.ifPresent(wf->{
						if (wf.getStatus() == WaveformStatus.LOADING) {
							Display.getCurrent().timerExec(50, this);
						} else if (wf.isLoaded()) {
							if (!voiceView.isDisposed()) {
								voiceView.redraw(wf);
							}
							if (!overView.isDisposed()) {
								overView.redraw(wf);
								overView.update();
							}
						}
					});
				}
			});
		}
	}

	private void loadPmpTeacherWaveform(Path pathPmpTeacher, Path pathPmpPrefixTeacher) {
		if (pathPmpTeacher != null) {
			wfPmpTeacher = Optional.of(new Waveform(pathPmpTeacher));
		}
		if (pathPmpPrefixTeacher != null) {
			wfPmpPrefixTeacher = Optional.of(new Waveform(pathPmpPrefixTeacher));
		}
		if ((wfPmpTeacher.isPresent()||wfPmpPrefixTeacher.isPresent()) && !Display.getCurrent().isDisposed()) {
			Display.getCurrent().timerExec(50, new Runnable() {
				@Override
				public void run() {
					boolean wfPmpTeacherLoaded=false, wfPmpPrefixTeacherLoaded=false;
					if (!wfPmpTeacher.isPresent() && !wfPmpPrefixTeacher.isPresent()) {
						voiceView.redraw(null, null);
					}

					if (!wfPmpTeacher.isPresent() || wfPmpTeacher.filter(wf->wf.getStatus().getValue()>0).isPresent()){
						wfPmpTeacherLoaded = true;
					}
					if (!wfPmpPrefixTeacher.isPresent() || wfPmpPrefixTeacher.filter(wf->wf.getStatus().getValue()>0).isPresent()){
						wfPmpPrefixTeacherLoaded = true;
					}
					if (wfPmpTeacherLoaded && wfPmpPrefixTeacherLoaded && !voiceView.isDisposed()) {
						voiceView.redraw(wfPmpTeacher.isPresent()?wfPmpTeacher.get():null, wfPmpPrefixTeacher.isPresent()?wfPmpPrefixTeacher.get():null);
						voiceView.update();
						chkShowPmpTeacher.setEnabled(wfPmpTeacher.isPresent());
						chkShowPmpPrefixTeacher.setEnabled(wfPmpPrefixTeacher.isPresent());
					}
					Display.getCurrent().timerExec(50, this);
					return;
				}
			});
		}
	}

	private void synthesis() {
		tmpCoreData = new CoreData.Builder().build();
		tmpCoreData.setVoice(tmpVoice);
		if (tmpVoice.isVCV()) {
			Voice tmpPreVoice = coreData.getVoice(tmpVoice.getPronunciationAlias().getPrefixPron()+tmpVoice.getPronunciationAlias().getSuffix());
			tmpCoreData.setVoice(tmpPreVoice);
			Note prefix_note = new Note.Builder(1)
					.setPronunciationAlias(tmpPreVoice.getPronunciationString())
					.range(100, 1000)
					.setBasePitch(Short.valueOf(txtMusicalScale.getText()))
					.build();
			tmpCoreData.addNote(prefix_note);
			Note tmp_note = new Note.Builder(2)
					.setPronunciationAlias(tmpVoice.getPronunciationString())
					.range(1000, 3000)
					.setBasePitch(Short.valueOf(txtMusicalScale.getText()))
					.build();
			tmpCoreData.addNote(tmp_note);
		} else {
			Note tmp_note = new Note.Builder(1)
					.setPronunciationAlias(tmpVoice.getPronunciationString())
					.range(100, 3000)
					.setBasePitch(Short.valueOf(txtMusicalScale.getText()))
					.build();
			tmpCoreData.addNote(tmp_note);
		}
		try {
			Files.deleteIfExists(tmpVoice.getUwcPath());
			tmpCoreData.nakloidIni.input.path_input_score = Files.createTempFile(Paths.get("temporary"), "VoiceOption.synthesis.", "");
			temporaryPaths.add(tmpCoreData.nakloidIni.input.path_input_score);
			tmpCoreData.nakloidIni.input.path_singer = Paths.get("temporary");
			temporaryPaths.add(Paths.get("temporary", "oto.ini"));
			tmpCoreData.nakloidIni.input.path_input_pitches = null;
			tmpCoreData.nakloidIni.output.path_song = Files.createTempFile(Paths.get("temporary"), "VoiceOption.synthesis.", "");
			temporaryPaths.add(tmpCoreData.nakloidIni.output.path_song);
			tmpCoreData.nakloidIni.output.ms_margin = 100;
			tmpCoreData.nakloidIni.arrange.vibrato = false;
			tmpCoreData.nakloidIni.arrange.overshoot = false;
			tmpCoreData.nakloidIni.arrange.preparation = false;
			tmpPmp.save(pathPmpTemporary);
			tmpCoreData.saveScore(tmpCoreData.nakloidIni.input.path_input_score);
			tmpCoreData.saveVocal(pathOtoIniTemporary);
			tmpCoreData.synthesize(new CoreDataSynthesisListener() {
				@Override
				public void synthesisFinished() {
					showSongWaveformStatus("歌声読込中...");
					wfSong.ifPresent(Waveform::close);
					wfSong = Optional.of(new Waveform(tmpCoreData.nakloidIni.output.path_song));
					if (!Display.getCurrent().isDisposed()) {
						Display.getCurrent().asyncExec(new Runnable() {
							boolean isSongGenerated = false;
							@Override
							public void run() {
								wfSong.ifPresent(wf -> {
									if (wf.getStatus() == Waveform.WaveformStatus.LOADING) {
										Display.getCurrent().timerExec(50, this);
									} else if (wf.isLoaded() && !isSongGenerated) {
										songView.redraw(wf);
										songView.update();
										wf.setVolume(sclPlayer.getSelection());
										try {
											wf.play();
										} catch (UnsupportedAudioFileException|IOException|LineUnavailableException e) {
											MessageDialog.openError(getShell(), "NakloidGUI", "音声の読込に失敗しました。\n"+e.getMessage());
										}
										isSongGenerated = true;
										Display.getCurrent().timerExec(50, this);
									} else if (wf.getStatus() == Waveform.WaveformStatus.STOPPED) {
										wf.close();
										wfSong = Optional.empty();
										btnPlayer.setEnabled(true);
									}
									if (wf.isPlaying()) {
										Display.getCurrent().timerExec(50, this);
									}
								});
							}
						});
					}
				}
			});
		} catch (IOException|InterruptedException e) {
			MessageDialog.openError(getShell(), "NakloidGUI", "歌声の生成に失敗しました。\n"+e.getMessage());
		}
	}
}
