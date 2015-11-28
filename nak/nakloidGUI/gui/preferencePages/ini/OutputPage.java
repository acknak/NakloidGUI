package nak.nakloidGUI.gui.preferencePages.ini;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.gui.preferencePages.DoubleFieldEditor;

public class OutputPage extends FieldEditorPreferencePage {
	Group grpCompressor = null;
	DoubleFieldEditor dfeCompressorThreshold=null, dfeCompressorRatio=null;

	public OutputPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("出力");
		setMessage(getTitle());
		setDescription("出力に関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			Composite cmpGeneral = new Composite(container, SWT.LEFT);
			cmpGeneral.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			cmpGeneral.setLayout(new GridLayout());
			{
				IntegerFieldEditor field = new IntegerFieldEditor("ini.output.ms_margin", "曲頭マージン", cmpGeneral, 5);
				field.setValidRange(0, 99999);
				field.setErrorMessage("０以上の整数を入力して下さい");
				addField(field);
			}
			{
				DoubleFieldEditor field = new DoubleFieldEditor("ini.output.max_volume", "最大ボリューム比", cmpGeneral);
				field.setValidRange(0.01, 1.0);
				field.setErrorMessage("0.01～1.0の実数を入力して下さい");
				addField(field);
			}
		}
		{
			grpCompressor = new Group(container, SWT.NONE);
			grpCompressor.setText("簡易コンプレッサー");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpCompressor.setLayoutData(data);
			BooleanFieldEditor bfeCompressor = new BooleanFieldEditor("ini.output.compressor", "簡易コンプレッサーを有効にする", grpCompressor);
			addField(bfeCompressor);
			boolean enabled = NakloidGUI.preferenceStore.getBoolean("ini.output.compressor");
			{
				dfeCompressorThreshold = new DoubleFieldEditor("ini.output.compressor_threshold", "スレッショルド", grpCompressor);
				dfeCompressorThreshold.setValidRange(-9999, -0.01);
				dfeCompressorThreshold.setErrorMessage("-0.01以下の実数を入力して下さい");
				dfeCompressorThreshold.setEnabled(enabled, grpCompressor);
				addField(dfeCompressorThreshold);
			}
			{
				dfeCompressorRatio = new DoubleFieldEditor("ini.output.compressor_ratio", "レシオ", grpCompressor);
				dfeCompressorRatio.setValidRange(0.01, 99999);
				dfeCompressorRatio.setErrorMessage("0.01以上の実数を入力して下さい");
				dfeCompressorRatio.setEnabled(enabled, grpCompressor);
				addField(dfeCompressorRatio);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.output.compressor")) {
			if (dfeCompressorThreshold != null) {
				dfeCompressorThreshold.setEnabled((boolean)event.getNewValue(), grpCompressor);
			}
			if (dfeCompressorRatio != null) {
				dfeCompressorRatio.setEnabled((boolean)event.getNewValue(), grpCompressor);
			}
		}
	}
}
