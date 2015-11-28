package nak.nakloidGUI.gui.preferencePages.ini;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.gui.preferencePages.DoubleFieldEditor;

public class AdvancedSettingPage extends FieldEditorPreferencePage {
	public AdvancedSettingPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("詳細設定");
		setMessage(getTitle());
		setDescription("詳細設定です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			Group group = new Group(container, SWT.NONE);
			group.setText("UWCファイル作成時の設定");
			GridData data = new GridData(GridData.FILL_BOTH);
			group.setLayoutData(data);
			{
				DoubleFieldEditor field = new DoubleFieldEditor("ini.unit_waveform_container.target_rms", "ターゲットRMS", group);
				field.setValidRange(0.001, 1.0);
				field.setErrorMessage("0.001～1.0の実数を入力して下さい");
				addField(field);
			}
			{
				IntegerFieldEditor field = new IntegerFieldEditor("ini.unit_waveform_container.num_lobes", "ローブ幅", group, 1);
				field.setValidRange(1, 5);
				field.setErrorMessage("１～５の整数を入力して下さい");
				addField(field);
			}
			{
				BooleanFieldEditor field = new BooleanFieldEditor("ini.unit_waveform_container.uwc_normalize", "UWC作成時のノーマライズを有効にする", group);
				addField(field);
			}
		}
		{
			Group group = new Group(container, SWT.NONE);
			group.setText("ピッチマークの設定");
			GridData data = new GridData(GridData.FILL_BOTH);
			group.setLayoutData(data);
			{
				IntegerFieldEditor field = new IntegerFieldEditor("ini.pitchmark.default_pitch", ".frq不在時の基準周波数(Hz)", group, 3);
				field.setValidRange(1, 999);
				field.setErrorMessage("正の整数を入力して下さい");
				addField(field);
			}
			{
				IntegerFieldEditor field = new IntegerFieldEditor("ini.pitchmark.pitch_margin", "ピッチマーク時のマージン", group, 2);
				field.setValidRange(1, 99);
				field.setErrorMessage("正の整数を入力して下さい");
				addField(field);
			}
			{
				DoubleFieldEditor field = new DoubleFieldEditor("ini.pitchmark.xcorr_threshold", "探索打切の相関閾値", group);
				field.setValidRange(0.1, 0.999);
				field.setErrorMessage("0.1～0.999の整数を入力して下さい");
				addField(field);
			}
		}
	}
}
