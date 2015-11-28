package nak.nakloidGUI.gui;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
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
			coreData.setLyrics(Arrays.stream(text.getText().split(",")).map(PronunciationAlias::new).collect(Collectors.toList()));
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
