package nak.nakloidGUI.gui.preferencePages.ini;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import nak.nakloidGUI.NakloidGUI;

public class NotePage extends FieldEditorPreferencePage {
	public NotePage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("音符設定");
		setMessage(getTitle());
		setDescription("音符の初期値に関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			IntegerFieldEditor field = new IntegerFieldEditor("ini.note.ms_front_padding", "フロントパディング", container, 3);
			field.setValidRange(0, 999);
			field.setErrorMessage("正の整数を入力して下さい");
			addField(field);
		}
		{
			IntegerFieldEditor field = new IntegerFieldEditor("ini.note.ms_back_padding", "バックパディング", container, 3);
			field.setValidRange(0, 999);
			field.setErrorMessage("正の整数を入力して下さい");
			addField(field);
		}
	}
}
