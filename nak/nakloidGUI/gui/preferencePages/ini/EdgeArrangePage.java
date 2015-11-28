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

public class EdgeArrangePage extends FieldEditorPreferencePage {
	Group grpOvershoot=null, grpPreparation=null;
	DoubleFieldEditor dfeOvershootPitch=null, dfePreparationPitch=null;
	IntegerFieldEditor ifeOvershootWidth=null, ifePreparationWidth=null;

	public EdgeArrangePage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("自動アレンジ(音符端)");
		setMessage(getTitle());
		setDescription("音符端の自動アレンジに関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			grpOvershoot = new Group(container, SWT.NONE);
			grpOvershoot.setText("オーバーシュート");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpOvershoot.setLayoutData(data);
			BooleanFieldEditor bfeOvershoot = new BooleanFieldEditor("ini.arrange.overshoot", "オーバーシュートを有効にする", grpOvershoot);
			addField(bfeOvershoot);
			boolean enabled = NakloidGUI.preferenceStore.getBoolean("ini.arrange.overshoot");
			{
				ifeOvershootWidth = new IntegerFieldEditor("ini.arrange.ms_overshoot", "変動時間(ms)", grpOvershoot, 4);
				ifeOvershootWidth.setValidRange(1, 9999);
				ifeOvershootWidth.setErrorMessage("正の整数を入力して下さい");
				ifeOvershootWidth.setEnabled(enabled, grpOvershoot);
				addField(ifeOvershootWidth);
			}
			{
				dfeOvershootPitch = new DoubleFieldEditor("ini.arrange.pitch_overshoot", "変動幅(Hz)", grpOvershoot);
				dfeOvershootPitch.setValidRange(0.01, 30);
				dfeOvershootPitch.setErrorMessage("0.01～30の実数を入力して下さい");
				dfeOvershootPitch.setEnabled(enabled, grpOvershoot);
				addField(dfeOvershootPitch);
			}
		}
		{
			grpPreparation = new Group(container, SWT.NONE);
			grpPreparation.setText("プレパレーション");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpPreparation.setLayoutData(data);
			BooleanFieldEditor bfePreparation = new BooleanFieldEditor("ini.arrange.preparation", "プレパレーションを有効にする", grpPreparation);
			addField(bfePreparation);
			boolean enabled = NakloidGUI.preferenceStore.getBoolean("ini.arrange.preparation");
			{
				ifePreparationWidth = new IntegerFieldEditor("ini.arrange.ms_preparation", "変動時間(ms)", grpPreparation, 4);
				ifePreparationWidth.setValidRange(1, 9999);
				ifePreparationWidth.setErrorMessage("正の整数を入力して下さい");
				ifePreparationWidth.setEnabled(enabled, grpPreparation);
				addField(ifePreparationWidth);
			}
			{
				dfePreparationPitch = new DoubleFieldEditor("ini.arrange.pitch_preparation", "変動幅(Hz)", grpPreparation);
				dfePreparationPitch.setValidRange(0.01, 30);
				dfePreparationPitch.setErrorMessage("0.01～30の実数を入力して下さい");
				dfePreparationPitch.setEnabled(enabled, grpPreparation);
				addField(dfePreparationPitch);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.arrange.overshoot")) {
			if (ifeOvershootWidth != null) {
				ifeOvershootWidth.setEnabled((boolean)event.getNewValue(), grpOvershoot);
			}
			if (dfeOvershootPitch != null) {
				dfeOvershootPitch.setEnabled((boolean)event.getNewValue(), grpOvershoot);
			}
		}
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.arrange.preparation")) {
			if (ifePreparationWidth != null) {
				ifePreparationWidth.setEnabled((boolean)event.getNewValue(), grpOvershoot);
			}
			if (dfePreparationPitch != null) {
				dfePreparationPitch.setEnabled((boolean)event.getNewValue(), grpOvershoot);
			}
		}
	}
}
