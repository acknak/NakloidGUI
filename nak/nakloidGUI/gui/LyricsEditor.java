package nak.nakloidGUI.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.models.PronunciationAlias;

public class LyricsEditor extends Dialog {
	private Text text;
	private String lyrics;
	private CoreData coreData;

	public LyricsEditor(Shell parent, CoreData coreData) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.coreData = coreData;
		lyrics = coreData.getAllLyrics();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("歌詞エディター");
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			coreData.setLyrics(Arrays.stream(text.getText().replaceAll("\n", "").split(",")).map(PronunciationAlias::new).collect(Collectors.toList()));
			try {
				coreData.saveScore();
			} catch (IOException e) {
				ErrorDialog.openError(getShell(), "NakloidGUI",
						"歌詞保存のファイル入出力時にエラーが発生しました。\ntemporaryフォルダに書き込み権限があるか確認してください。",
						new MultiStatus(".", IStatus.ERROR,
								Stream.of(e.getStackTrace())
										.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
										.collect(Collectors.toList()).toArray(new Status[]{}),
								e.getLocalizedMessage(), e));
			}
			try {
				coreData.synthesize();
			} catch (IOException e) {
				ErrorDialog.openError(getShell(), "NakloidGUI",
						"歌声合成のファイルの入出力時にエラーが発生しました。\ntemporaryフォルダ及びNakloid.iniに書き込み権限があるか確認してください。",
						new MultiStatus(".", IStatus.ERROR,
								Stream.of(e.getStackTrace())
										.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
										.collect(Collectors.toList()).toArray(new Status[]{}),
								e.getLocalizedMessage(), e));
			} catch (InterruptedException e) {
				ErrorDialog.openError(getShell(), "NakloidGUI",
						"歌声合成中にスレッドが中断されました。",
						new MultiStatus(".", IStatus.ERROR,
								Stream.of(e.getStackTrace())
										.map(s->new Status(IStatus.ERROR, ".", "at "+s.getClassName()+": "+s.getMethodName()))
										.collect(Collectors.toList()).toArray(new Status[]{}),
								e.getLocalizedMessage(), e));
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
		GridLayout layComposite = new GridLayout(1, false);
		layComposite.marginHeight = layComposite.horizontalSpacing = layComposite.marginWidth = layComposite.verticalSpacing = 0;
		container.setLayout(layComposite);
		text = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setText(lyrics);
		Font initialFont = text.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (FontData tmpFontData : fontData) {
			tmpFontData.setHeight(20);
		}
		Font newFont = new Font(Display.getCurrent(), fontData);
		text.setFont(newFont);
		return container;
	}
}
