package nak.nakloidGUI.gui.preferencePages.gui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import nak.nakloidGUI.NakloidGUI;

public class NoteOptionPage extends FieldEditorPreferencePage {
	public NoteOptionPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("音符設定画面");
		setMessage(getTitle());
		setDescription("音符設定画面の外観に関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			IntegerFieldEditor field = new IntegerFieldEditor("gui.noteOption.volumeViewHeight", "ボリューム画面の高さ", container, 3);
			field.setValidRange(50, 200);
			field.setErrorMessage("50～200の整数を入力して下さい");
			addField(field);
		}
	}
}
