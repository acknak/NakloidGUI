package nak.nakloidGUI.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.coredata.CoreData;

public class SpeechSynthesisForm extends Dialog {
	final String[] ext = {"*.wav"};
	final String [] filterNames = {"RIFF waveform Audio Format (*.wav)"};
	private CoreData coreData;
	private Text txtInputWavFile, txtF0Scale, txtLeftMargin, txtLyrics;

	public SpeechSynthesisForm(Shell shell, CoreData coreData) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.coreData = coreData;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 380);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("話声合成用ファイルをインポート");
	}

	@Override
	protected void buttonPressed(int buttonId) {
		try {
			if (buttonId == IDialogConstants.OK_ID) {
				System.out.println(CoreData.sdf.format(System.currentTimeMillis()));
				try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(NakloidGUI.preferenceStore.getString("workspace.path_speech_ini")), "SJIS"))) {
					bw.write("input_wav_path="+txtInputWavFile.getText());
					bw.newLine();
					bw.write("output_score_path="+NakloidGUI.preferenceStore.getString("ini.input.path_input_score"));
					bw.newLine();
					bw.write("output_pitches_path="+NakloidGUI.preferenceStore.getString("ini.input.path_input_pitches"));
					bw.newLine();
					bw.write("pronunciation="+txtLyrics.getText().replaceAll("\n", ""));
					bw.newLine();
					bw.write("f0_scaling="+String.valueOf(Double.parseDouble(txtF0Scale.getText())/100.0));
					bw.newLine();
					bw.write("ms_margin="+txtLeftMargin.getText());
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ProcessBuilder pb = new ProcessBuilder("WORLDforNakloid.exe", NakloidGUI.preferenceStore.getString("workspace.path_speech_ini"));
				pb.redirectErrorStream(true);
				Process process = pb.start();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "Shift_JIS"))) {
					String str;
					while((str = br.readLine()) != null) {
						System.out.println(str);
					}
				}
				coreData.reloadScoreAndPitches();
				coreData.saveScore();
				try {
					coreData.synthesize();
				} catch (InterruptedException e) {
					ErrorDialog.openError(getShell(), "NakloidGUI",
							"話声合成中にスレッドが中断されました。",
							new MultiStatus(".", IStatus.ERROR,
									Stream.of(e.getStackTrace())
											.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
											.collect(Collectors.toList()).toArray(new Status[]{}),
									e.getLocalizedMessage(), e));
				}
			}
		} catch (IOException e) {
			ErrorDialog.openError(getShell(), "NakloidGUI",
					"楽譜保存時のファイルの入出力時にエラーが発生しました。\ntemporaryフォルダに書き込み権限があるか確認してください。",
					new MultiStatus(".", IStatus.ERROR,
							Stream.of(e.getStackTrace())
									.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
									.collect(Collectors.toList()).toArray(new Status[]{}),
							e.getLocalizedMessage(), e));
		} finally {
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void handleShellCloseEvent() {
		buttonPressed(IDialogConstants.CANCEL_ID);
		super.handleShellCloseEvent();
	};

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "作成", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "キャンセル", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		{
			Composite cmpInputWavFile = new Composite(container, SWT.NONE);
			cmpInputWavFile.setLayoutData(new GridData(GridData.FILL_BOTH));
			GridLayout layCmpInputWavFile = new GridLayout(4, false);
			layCmpInputWavFile.marginRight = 5;
			cmpInputWavFile.setLayout(layCmpInputWavFile);
			{
				Label lblInputWavFile = new Label(cmpInputWavFile, SWT.RIGHT);
				lblInputWavFile.setText("ガイドWav");
			}
			{
				txtInputWavFile = new Text(cmpInputWavFile, SWT.BORDER|SWT.LEFT);
				txtInputWavFile.setEditable(false);
				GridData layTxtInputWavFile = new GridData(GridData.FILL_HORIZONTAL);
				layTxtInputWavFile.horizontalSpan = 2;
				txtInputWavFile.setLayoutData(layTxtInputWavFile);
			}
			{
				Button btnAlias = new Button(cmpInputWavFile, SWT.NULL);
				btnAlias.setText("選択");
				btnAlias.addSelectionListener(new SelectionAdapter(){
					public void widgetSelected(SelectionEvent e){
						FileDialog openDialog = new FileDialog(getShell(), SWT.OPEN);
						openDialog.setFilterExtensions(ext);
						openDialog.setFilterNames(filterNames);
						String strPath = openDialog.open();
						if (strPath==null || strPath.isEmpty()) {
							return;
						}
						txtInputWavFile.setText(strPath);
					}
				});
			}
			{
				Label lblF0Scale = new Label(cmpInputWavFile, SWT.RIGHT);
				lblF0Scale.setText("F0変換率");
			}
			{
				txtF0Scale = new Text(cmpInputWavFile, SWT.BORDER|SWT.RIGHT);
				txtF0Scale.setText("100");
				GridData layTxtF0Scale = new GridData(SWT.RIGHT);
				layTxtF0Scale.widthHint = 40;
				txtF0Scale.setLayoutData(layTxtF0Scale);
			}
			{
				Label lblF0ScaleUnit = new Label(cmpInputWavFile, SWT.LEFT);
				lblF0ScaleUnit.setText("%");
				GridData layTxtF0ScaleUnit = new GridData(SWT.RIGHT);
				layTxtF0ScaleUnit.horizontalSpan = 2;
				lblF0ScaleUnit.setLayoutData(layTxtF0ScaleUnit);
			}
			{
				Label lblLeftMargin = new Label(cmpInputWavFile, SWT.RIGHT);
				lblLeftMargin.setText("発声マージン");
			}
			{
				txtLeftMargin = new Text(cmpInputWavFile, SWT.BORDER|SWT.RIGHT);
				txtLeftMargin.setText("100");
				GridData layLeftMargin = new GridData(SWT.RIGHT);
				layLeftMargin.widthHint = 40;
				txtLeftMargin.setLayoutData(layLeftMargin);
			}
			{
				Label lblLeftMarginUnit = new Label(cmpInputWavFile, SWT.LEFT);
				lblLeftMarginUnit.setText("ms");
				GridData layLeftMarginUnit = new GridData(SWT.RIGHT);
				layLeftMarginUnit.horizontalSpan = 2;
				lblLeftMarginUnit.setLayoutData(layLeftMarginUnit);
			}
			{
				Group grpLyrics = new Group(cmpInputWavFile, SWT.NONE);
				grpLyrics.setText("発音");
				GridData layGrpLyrics = new GridData(GridData.FILL_BOTH);
				layGrpLyrics.horizontalSpan = 4;
				grpLyrics.setLayoutData(layGrpLyrics);
				grpLyrics.setLayout(new GridLayout(1, false));
				{
					Label lblLyricsHint = new Label(grpLyrics, SWT.LEFT);
					lblLyricsHint.setText("カンマ区切りで入力して下さい\n例：こ,ん,に,ち,わ");
				}
				{
					txtLyrics = new Text(grpLyrics, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
					txtLyrics.setLayoutData(new GridData(GridData.FILL_BOTH));
				}
			}
		}
		return parent;
	}
}
