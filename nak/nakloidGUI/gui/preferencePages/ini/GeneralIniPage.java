package nak.nakloidGUI.gui.preferencePages.ini;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import nak.nakloidGUI.NakloidGUI;

public class GeneralIniPage extends FieldEditorPreferencePage {
	public GeneralIniPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("Nakloid");
		setMessage(getTitle());
		setDescription("Nakloidに関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			BooleanFieldEditor field = new BooleanFieldEditor("ini.output.print_debug", "ログ表示をデバッグモードにする", container);
			addField(field);
		}
		{
			BooleanFieldEditor field = new BooleanFieldEditor("ini.vocal_library.use_uwc_cache", "UWCキャッシュを使用する", container);
			addField(field);
		}
		{
			IntegerFieldEditor ifeVibratoOffset = new IntegerFieldEditor("ini.input.track", "使用するMIDIトラック", container, 2);
			ifeVibratoOffset.setValidRange(1, 99);
			ifeVibratoOffset.setErrorMessage("正の整数を入力して下さい");
			addField(ifeVibratoOffset);
		}
	}
}
