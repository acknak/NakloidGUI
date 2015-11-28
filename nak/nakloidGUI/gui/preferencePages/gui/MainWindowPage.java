package nak.nakloidGUI.gui.preferencePages.gui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import nak.nakloidGUI.NakloidGUI;

public class MainWindowPage extends FieldEditorPreferencePage {
	public MainWindowPage() {
		super(FieldEditorPreferencePage.GRID);
		setTitle("メインウィンドウ");
		setMessage(getTitle());
		setDescription("メインウィンドウの外観に関する設定項目です");
		setPreferenceStore(NakloidGUI.preferenceStore);
	}

	@Override
	protected void createFieldEditors() {
		Composite container = new Composite(getFieldEditorParent(), SWT.LEFT);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		container.setLayout(new GridLayout());
		{
			Group group = new Group(container, SWT.NONE);
			group.setText("入力可能なMIDIノート番号の範囲");
			GridData data = new GridData(GridData.FILL_BOTH);
			group.setLayoutData(data);
			{
				IntegerFieldEditor field = new IntegerFieldEditor("gui.mainWindow.numMidiNoteUpperLimit", "上限", group, 3);
				field.setValidRange(66, 110);
				field.setErrorMessage("66～110の整数を入力して下さい");
				addField(field);
			}
			{
				IntegerFieldEditor field = new IntegerFieldEditor("gui.mainWindow.numMidiNoteLowerLimit", "下限", group, 2);
				field.setValidRange(20, 65);
				field.setErrorMessage("20～65の整数を入力して下さい");
				addField(field);
			}
		}
		{
			Group group = new Group(container, SWT.NONE);
			group.setText("表示する音符の長さ");
			GridData data = new GridData(GridData.FILL_BOTH);
			group.setLayoutData(data);
			{
				IntegerFieldEditor field = new IntegerFieldEditor("gui.mainWindow.baseNoteHeight", "基本縦長(px)", group, 2);
				field.setValidRange(1, 99);
				field.setErrorMessage("1～99の整数を入力して下さい");
				addField(field);
			}
			{
				IntegerFieldEditor field = new IntegerFieldEditor("gui.mainWindow.baseMsByPixel", "基本幅比(ms/px)", group, 2);
				field.setValidRange(1, 99);
				field.setErrorMessage("1～99の整数を入力して下さい");
				addField(field);
			}
		}
		{
			RadioGroupFieldEditor field = new RadioGroupFieldEditor(
					"gui.mainWindow.vocalInfoDisplayMode",
					"ボーカル情報の表示方法",
					2,
					new String[][]{{"ツールチップ","tooltip"},{"ライナー","liner"}},
					container,
					true);
			addField(field);
		}
	}
}
