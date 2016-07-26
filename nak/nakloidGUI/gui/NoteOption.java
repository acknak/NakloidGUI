package nak.nakloidGUI.gui;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.gui.noteOptionViews.VolumeView;
import nak.nakloidGUI.gui.noteOptionViews.VolumeView.VolumeViewListener;
import nak.nakloidGUI.models.Note;
import nak.nakloidGUI.models.Voice;

public class NoteOption extends Dialog implements VolumeViewListener {
	private CoreData coreData;
	private final Note note;
	private Note tmpNote;
	private Text txtAlias, txtPitch, txtStart, txtEnd, txtBaseVelocity, txtFrontMargin, txtFrontPadding, txtBackPadding, txtBackMargin;
	private VolumeView volumeView;

	public NoteOption(Shell shell, CoreData coreData, Note note) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.coreData = coreData;
		this.note = tmpNote = note;
		if (coreData.getVoice(note.getPronunciationAlias())==null) {
			MessageDialog.openInformation(getShell(), "NakloidGUI", "「"+note.getPronunciationAliasString()+"」は存在しない発音です。");
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 380);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("\""+note.getPronunciationAliasString()+"\""+"の音符設定");
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			coreData.setNote(tmpNote);
		} else if (buttonId == IDialogConstants.ABORT_ID) {
			if (MessageDialog.openQuestion(getShell(), "NakloidGUI", "本当にこの音符を削除しますか？")) {
				coreData.removeNote(tmpNote);
				buttonId = IDialogConstants.OK_ID;
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
		if (coreData.getScoreLength() > 0) {
			createButton(parent, IDialogConstants.OK_ID, "完了", true);
			createButton(parent, IDialogConstants.ABORT_ID, "削除", coreData.getScoreLength()>0);
		} else {
			createButton(parent, IDialogConstants.OK_ID, "追加", true);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, "キャンセル", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		{
			Composite cmpNoteParameters = new Composite(container, SWT.NONE);
			cmpNoteParameters.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layCntNoteParameters = new GridLayout(5, false);
			layCntNoteParameters.marginRight = 5;
			cmpNoteParameters.setLayout(layCntNoteParameters);
			{
				Composite cmpAlias = new Composite(cmpNoteParameters, SWT.NONE);
				cmpAlias.setLayout(new GridLayout(3, false));
				{
					Label lblAlias = new Label(cmpAlias, SWT.RIGHT);
					lblAlias.setText("発音");
				}
				{
					txtAlias = new Text(cmpAlias, SWT.BORDER|SWT.RIGHT);
					txtAlias.setText(note.getPronunciationAliasString());
					GridData layTxtAlias = new GridData(SWT.LEFT);
					layTxtAlias.widthHint = 40;
					txtAlias.setLayoutData(layTxtAlias);
					txtAlias.addKeyListener(new KeyAdapter(){
						public void keyReleased(KeyEvent e) {
							tmpNote = new Note.Builder(tmpNote).setPronunciationAlias(((Text)e.getSource()).getText()).build();
						}
					});
				}
				{
					Button btnAlias = new Button(cmpAlias, SWT.NULL);
					btnAlias.setText("設定");
					btnAlias.addSelectionListener(new SelectionAdapter(){
						public void widgetSelected(SelectionEvent e){
							openVoiceDialog();
						}
					});
				}
			}
			{
				Composite cmpPitch = new Composite(cmpNoteParameters, SWT.NONE);
				cmpPitch.setLayout(new GridLayout(2, false));
				{
					Label lblPitch = new Label(cmpPitch, SWT.RIGHT);
					lblPitch.setText("MIDIノート番号");
				}
				{
					txtPitch = new Text(cmpPitch, SWT.BORDER|SWT.RIGHT);
					txtPitch.setText(String.valueOf(note.getBasePitch()));
					GridData layTxtPitch = new GridData(SWT.LEFT);
					layTxtPitch.widthHint = 40;
					txtPitch.setLayoutData(layTxtPitch);
					txtPitch.addKeyListener(new KeyAdapter(){
						public void keyReleased(KeyEvent e) {
							tmpNote = new Note.Builder(tmpNote).setBasePitch(Integer.valueOf(((Text)e.getSource()).getText())).build();
						}
					});
				}
			}
			{
				Composite cmpStart = new Composite(cmpNoteParameters, SWT.NONE);
				cmpStart.setLayout(new GridLayout(3, false));
				{
					Label lblStart = new Label(cmpStart, SWT.RIGHT);
					lblStart.setText("開始時間");
				}
				{
					txtStart = new Text(cmpStart, SWT.BORDER|SWT.RIGHT);
					txtStart.setText(String.valueOf(note.getStart()));
					GridData layTxtStart = new GridData(SWT.LEFT);
					layTxtStart.widthHint = 40;
					txtStart.setLayoutData(layTxtStart);
					txtStart.addKeyListener(new KeyAdapter(){
						public void keyReleased(KeyEvent e) {
							tmpNote = new Note.Builder(tmpNote).setStart(Integer.valueOf(((Text)e.getSource()).getText())).build();
							volumeView.redraw(tmpNote, coreData.getVoice(tmpNote.getPronunciationAlias()));
						}
					});
				}
				{
					Label lblStartMs = new Label(cmpStart, SWT.LEFT);
					lblStartMs.setText("ms");
				}
			}
			{
				Composite cmpEnd = new Composite(cmpNoteParameters, SWT.NONE);
				cmpEnd.setLayout(new GridLayout(3, false));
				{
					Label lblEnd = new Label(cmpEnd, SWT.RIGHT);
					lblEnd.setText("終了時間");
				}
				{
					txtEnd = new Text(cmpEnd, SWT.BORDER|SWT.RIGHT);
					txtEnd.setText(String.valueOf(note.getEnd()));
					GridData layTxtEnd = new GridData(SWT.LEFT);
					layTxtEnd.widthHint = 40;
					txtEnd.setLayoutData(layTxtEnd);
					txtEnd.addKeyListener(new KeyAdapter(){
						public void keyReleased(KeyEvent e) {
							tmpNote = new Note.Builder(tmpNote).setEnd(Integer.valueOf(((Text)e.getSource()).getText())).build();
							volumeView.redraw(tmpNote, coreData.getVoice(tmpNote.getPronunciationAlias()));
						}
					});
				}
				{
					Label lblEndMs = new Label(cmpEnd, SWT.LEFT);
					lblEndMs.setText("ms");
				}
			}
			{
				Composite cmpBaseVelocity = new Composite(cmpNoteParameters, SWT.NONE);
				cmpBaseVelocity.setLayout(new GridLayout(3, false));
				{
					Label lblBaseVelocity = new Label(cmpBaseVelocity, SWT.RIGHT);
					lblBaseVelocity.setText("基準音量");
				}
				{
					txtBaseVelocity = new Text(cmpBaseVelocity, SWT.BORDER|SWT.RIGHT);
					txtBaseVelocity.setText(String.valueOf(note.getBaseVelocity()));
					GridData layTxtBaseVelocity = new GridData(SWT.LEFT);
					layTxtBaseVelocity.widthHint = 40;
					txtBaseVelocity.setLayoutData(layTxtBaseVelocity);
					txtBaseVelocity.addKeyListener(new KeyAdapter(){
						public void keyReleased(KeyEvent e) {
							try {
								tmpNote = new Note.Builder(tmpNote).setBaseVelocity(Integer.valueOf(((Text)e.getSource()).getText())).build();
							} catch (NumberFormatException e1) {
								return;
							}
							volumeView.redraw(tmpNote, coreData.getVoice(tmpNote.getPronunciationAlias()));
						}
					});
					txtBaseVelocity.setEnabled(tmpNote.getVelPointsSize()==0);
				}
				{
					Label lblBaseVelocityRange = new Label(cmpBaseVelocity, SWT.LEFT);
					lblBaseVelocityRange.setText("(0-127)");
				}
			}
		}
		{
			Group grpVolumeParameters = new Group(container, SWT.NONE);
			grpVolumeParameters.setText("\""+note.getPronunciationAliasString()+"\"の音量");
			grpVolumeParameters.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layGrpVolumeParameters = new GridLayout(1, false);
			layGrpVolumeParameters.marginHeight = 0;
			grpVolumeParameters.setLayout(layGrpVolumeParameters);
			{
				volumeView = new VolumeView(grpVolumeParameters, note, coreData.getVoice(tmpNote.getPronunciationAlias()));
				volumeView.setVolumeViewListener(this);
			}
			{
				Composite cmpBorderParameters = new Composite(grpVolumeParameters, SWT.NONE);
				GridLayout layBorderParameters = new GridLayout(4, false);
				layBorderParameters.marginRight = 5;
				layBorderParameters.marginHeight = 0;
				cmpBorderParameters.setLayout(layBorderParameters);
				{
					Composite cmpFrontMargin = new Composite(cmpBorderParameters, SWT.NONE);
					cmpFrontMargin.setLayout(new GridLayout(3, false));
					{
						Label lblFrontMargin = new Label(cmpFrontMargin, SWT.RIGHT);
						lblFrontMargin.setText("フロントマージン");
					}
					{
						txtFrontMargin = new Text(cmpFrontMargin, SWT.BORDER|SWT.RIGHT);
						txtFrontMargin.setText(String.valueOf(note.getFrontMargin()));
						GridData layTxtFrontMargin = new GridData(SWT.LEFT);
						layTxtFrontMargin.widthHint = 40;
						txtFrontMargin.setLayoutData(layTxtFrontMargin);
						txtFrontMargin.setEnabled(false);
					}
					{
						Label lblFrontMarginMs = new Label(cmpFrontMargin, SWT.LEFT);
						lblFrontMarginMs.setText("ms");
					}
				}
				{
					Composite cmpFrontPadding = new Composite(cmpBorderParameters, SWT.NONE);
					cmpFrontPadding.setLayout(new GridLayout(3, false));
					{
						Label lblFrontPadding = new Label(cmpFrontPadding, SWT.RIGHT);
						lblFrontPadding.setText("フロントパディング");
					}
					{
						txtFrontPadding = new Text(cmpFrontPadding, SWT.BORDER|SWT.RIGHT);
						txtFrontPadding.setText(String.valueOf(note.getFrontPadding()));
						GridData layTxtFrontPadding = new GridData(SWT.LEFT);
						layTxtFrontPadding.widthHint = 40;
						txtFrontPadding.setLayoutData(layTxtFrontPadding);
						txtFrontPadding.setEnabled(false);
					}
					{
						Label lblFrontPaddingMs = new Label(cmpFrontPadding, SWT.LEFT);
						lblFrontPaddingMs.setText("ms");
					}
				}
				{
					Composite cmpBackPadding = new Composite(cmpBorderParameters, SWT.NONE);
					cmpBackPadding.setLayout(new GridLayout(3, false));
					{
						Label lblBackPadding = new Label(cmpBackPadding, SWT.RIGHT);
						lblBackPadding.setText("バックパディング");
					}
					{
						txtBackPadding = new Text(cmpBackPadding, SWT.BORDER|SWT.RIGHT);
						txtBackPadding.setText(String.valueOf(note.getBackPadding()));
						GridData layTxtBackPadding = new GridData(SWT.LEFT);
						layTxtBackPadding.widthHint = 40;
						txtBackPadding.setLayoutData(layTxtBackPadding);
						txtBackPadding.setEnabled(false);
					}
					{
						Label lblBackPaddingMs = new Label(cmpBackPadding, SWT.LEFT);
						lblBackPaddingMs.setText("ms");
					}
				}
				{
					Composite cmpBackMargin = new Composite(cmpBorderParameters, SWT.NONE);
					cmpBackMargin.setLayout(new GridLayout(3, false));
					{
						Label lblBackMargin = new Label(cmpBackMargin, SWT.RIGHT);
						lblBackMargin.setText("バックマージン");
					}
					{
						txtBackMargin = new Text(cmpBackMargin, SWT.BORDER|SWT.RIGHT);
						txtBackMargin.setText(String.valueOf(note.getBackMargin()));
						GridData layTxtBackMargin = new GridData(SWT.LEFT);
						layTxtBackMargin.widthHint = 40;
						txtBackMargin.setLayoutData(layTxtBackMargin);
						txtBackMargin.setEnabled(false);
					}
					{
						Label lblBackMarginMs = new Label(cmpBackMargin, SWT.LEFT);
						lblBackMarginMs.setText("ms");
					}
				}
			}
		}
		return parent;
	}

	@Override
	public void volumeViewAddVelPoint(int ms, int size) {
		tmpNote = tmpNote.addVelPoint(ms, size);
		txtBaseVelocity.setEnabled(false);
		volumeView.redraw(tmpNote, coreData.getVoice(tmpNote.getPronunciationAlias()));
	}

	@Override
	public void volumeViewDeleteVelPoint(int ms) {
		tmpNote = tmpNote.deleteVelPoint(ms);
		if (tmpNote.getVelPointsSize() == 0) {
			txtBaseVelocity.setEnabled(true);
		}
		volumeView.redraw(tmpNote, coreData.getVoice(tmpNote.getPronunciationAlias()));
	}

	@Override
	public void volumeViewBarUpdated(SelectionEvent e) {
		volumeView.redraw();
	}

	private void openVoiceDialog() {
		Voice voice = coreData.getVoice(txtAlias.getText());
		if (voice == null) {
			MessageDialog.openError(getShell(), "NakloidGUI", "oto.iniに存在しない発音が選択されました。");
			return;
		}
		try {
			VoiceOption dialog = new VoiceOption(getShell(), coreData, voice);
			dialog.open();
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), "NakloidGUI",
					"oto.iniに存在しない発音が選択されました。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		}
	}
}
