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

public class OverlapPage extends FieldEditorPreferencePage {
	Group grpSelfFadeStretch=null;
	IntegerFieldEditor ifeMsSelfFade=null;

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
			grpSelfFadeStretch = new Group(container, SWT.NONE);
			grpSelfFadeStretch.setText("自己フェード伸縮");
			GridData data = new GridData(GridData.FILL_BOTH);
			grpSelfFadeStretch.setLayoutData(data);
			BooleanFieldEditor field = new BooleanFieldEditor("ini.overlap.stretch_self_fade", "自己フェード時の伸縮処理を有効にする", grpSelfFadeStretch);
			addField(field);
			{
				ifeMsSelfFade = new IntegerFieldEditor("ini.overlap.ms_self_fade", "自己フェード周期(ms)", grpSelfFadeStretch, 4);
				ifeMsSelfFade.setValidRange(1, 9999);
				ifeMsSelfFade.setErrorMessage("正の整数を入力して下さい");
				ifeMsSelfFade.setEnabled(NakloidGUI.preferenceStore.getBoolean("ini.overlap.stretch_self_fade"), grpSelfFadeStretch);
				addField(ifeMsSelfFade);
			}
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

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (((FieldEditor)event.getSource()).getPreferenceName().equals("ini.overlap.stretch_self_fade")) {
			if (ifeMsSelfFade != null) {
				ifeMsSelfFade.setEnabled((boolean)event.getNewValue(), grpSelfFadeStretch);
			}
		}
	}
}
