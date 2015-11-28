package nak.nakloidGUI.gui.preferencePages.gui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import nak.nakloidGUI.NakloidGUI;

public class GeneralGuiPage extends FieldEditorPreferencePage {
	public GeneralGuiPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("外観設定");
		setMessage(getTitle());
		setDescription("GUIの外観に関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {}
}
