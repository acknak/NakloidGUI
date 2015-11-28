package nak.nakloidGUI.gui.preferencePages.ini;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.gui.preferencePages.DoubleFieldEditor;

public class OverlapPage extends FieldEditorPreferencePage {
	public OverlapPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("オーバーラップ");
		setMessage(getTitle());
		setDescription("PSOLA法のOLAに関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			IntegerFieldEditor field = new IntegerFieldEditor("ini.overlap.ms_self_fade", "自己フェード周期(ms)", container, 4);
			field.setValidRange(1, 9999);
			field.setErrorMessage("正の整数を入力して下さい");
			addField(field);
		}
		{
			DoubleFieldEditor field = new DoubleFieldEditor("ini.overlap.self_fade_stretch_scale", "自己フェード時の伸縮処置を有効にする", container);
			field.setValidRange(0.001, 99999);
			field.setErrorMessage("正の実数を入力して下さい");
			addField(field);
		}
		{
			BooleanFieldEditor field = new BooleanFieldEditor("ini.overlap.interpolation", "合成時の補間処理を有効にする", container);
			addField(field);
		}
		{
			BooleanFieldEditor field = new BooleanFieldEditor("ini.overlap.overlap_normalize", "オーバーラップ時のノーマライズを有効にする", container);
			addField(field);
		}
		{
			BooleanFieldEditor field = new BooleanFieldEditor("ini.overlap.window_modification", "合成ピッチに合わせた窓関数変形を有効にする", container);
			addField(field);
		}
	}
}
