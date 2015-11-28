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

public class EntireArrangePage extends FieldEditorPreferencePage {
	Group grpAutoVowelCombining=null, grpVibrato=null, grpFinefluctuation=null;
	DoubleFieldEditor dfeAutoVowelCombiningScale=null, dfeVibratoPitch=null, dfeFinefluctuationDeviation=null;
	IntegerFieldEditor ifeVibratoOffset=null, ifeVibratoWidth=null;

	public EntireArrangePage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("自動アレンジ(全体)");
		setMessage(getTitle());
		setDescription("音符全体の自動アレンジに関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			grpAutoVowelCombining = new Group(container, SWT.NONE);
			grpAutoVowelCombining.setText("母音結合");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpAutoVowelCombining.setLayoutData(data);
			BooleanFieldEditor bfeVibrato = new BooleanFieldEditor("ini.arrange.auto_vowel_combining", "母音結合を有効にする", grpAutoVowelCombining);
			addField(bfeVibrato);
			boolean enabled = NakloidGUI.preferenceStore.getBoolean("ini.arrange.auto_vowel_combining");
			{
				dfeAutoVowelCombiningScale = new DoubleFieldEditor("ini.arrange.vowel_combining_volume", "音量比", grpAutoVowelCombining);
				dfeAutoVowelCombiningScale.setValidRange(0.01, 1.0);
				dfeAutoVowelCombiningScale.setErrorMessage("0.01～1.0の実数を入力して下さい");
				dfeAutoVowelCombiningScale.setEnabled(enabled, grpAutoVowelCombining);
				addField(dfeAutoVowelCombiningScale);
			}
		}
		{
			grpVibrato = new Group(container, SWT.NONE);
			grpVibrato.setText("ビブラート");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpVibrato.setLayoutData(data);
			BooleanFieldEditor bfeVibrato = new BooleanFieldEditor("ini.arrange.vibrato", "ビブラートを有効にする", grpVibrato);
			addField(bfeVibrato);
			boolean enabled = NakloidGUI.preferenceStore.getBoolean("ini.arrange.vibrato");
			{
				ifeVibratoOffset = new IntegerFieldEditor("ini.arrange.ms_vibrato_offset", "オフセット(ms)", grpVibrato, 4);
				ifeVibratoOffset.setValidRange(1, 9999);
				ifeVibratoOffset.setErrorMessage("正の整数を入力して下さい");
				ifeVibratoOffset.setEnabled(enabled, grpVibrato);
				addField(ifeVibratoOffset);
			}
			{
				ifeVibratoWidth = new IntegerFieldEditor("ini.arrange.ms_vibrato_width", "周期(ms)", grpVibrato, 4);
				ifeVibratoWidth.setValidRange(1, 9999);
				ifeVibratoWidth.setErrorMessage("正の整数を入力して下さい");
				ifeVibratoWidth.setEnabled(enabled, grpVibrato);
				addField(ifeVibratoWidth);
			}
			{
				dfeVibratoPitch = new DoubleFieldEditor("ini.arrange.pitch_vibrato", "変動幅(Hz)", grpVibrato);
				dfeVibratoPitch.setValidRange(0.01, 30);
				dfeVibratoPitch.setErrorMessage("0.01～30の実数を入力して下さい");
				dfeVibratoPitch.setEnabled(enabled, grpVibrato);
				addField(dfeVibratoPitch);
			}
		}
		{
			grpFinefluctuation = new Group(container, SWT.NONE);
			grpFinefluctuation.setText("微細変動");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpFinefluctuation.setLayoutData(data);
			BooleanFieldEditor bfeVibrato = new BooleanFieldEditor("ini.arrange.finefluctuation", "微細変動を有効にする", grpFinefluctuation);
			addField(bfeVibrato);
			boolean enabled = NakloidGUI.preferenceStore.getBoolean("ini.arrange.finefluctuation");
			{
				dfeFinefluctuationDeviation = new DoubleFieldEditor("ini.arrange.finefluctuation_deviation", "標準偏差", grpFinefluctuation);
				dfeFinefluctuationDeviation.setValidRange(0.01, 10.0);
				dfeFinefluctuationDeviation.setErrorMessage("0.01～10.0の実数を入力して下さい");
				dfeFinefluctuationDeviation.setEnabled(enabled, grpFinefluctuation);
				addField(dfeFinefluctuationDeviation);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.arrange.auto_vowel_combining")) {
			if (dfeAutoVowelCombiningScale != null) {
				dfeAutoVowelCombiningScale.setEnabled((boolean)event.getNewValue(), grpAutoVowelCombining);
			}
		}
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.arrange.vibrato")) {
			if (ifeVibratoOffset != null) {
				ifeVibratoOffset.setEnabled((boolean)event.getNewValue(), grpVibrato);
			}
			if (ifeVibratoWidth != null) {
				ifeVibratoWidth.setEnabled((boolean)event.getNewValue(), grpVibrato);
			}
			if (dfeVibratoPitch != null) {
				dfeVibratoPitch.setEnabled((boolean)event.getNewValue(), grpVibrato);
			}
		}
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.arrange.finefluctuation")) {
			if (dfeFinefluctuationDeviation != null) {
				dfeFinefluctuationDeviation.setEnabled((boolean)event.getNewValue(), grpFinefluctuation);
			}
		}
	}
}
