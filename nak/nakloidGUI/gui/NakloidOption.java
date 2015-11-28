package nak.nakloidGUI.gui;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import nak.nakloidGUI.NakloidGUI;
import nak.nakloidGUI.gui.preferencePages.gui.GeneralGuiPage;
import nak.nakloidGUI.gui.preferencePages.gui.MainWindowPage;
import nak.nakloidGUI.gui.preferencePages.gui.NoteOptionPage;
import nak.nakloidGUI.gui.preferencePages.gui.VoiceOptionPage;
import nak.nakloidGUI.gui.preferencePages.ini.AdvancedSettingPage;
import nak.nakloidGUI.gui.preferencePages.ini.EdgeArrangePage;
import nak.nakloidGUI.gui.preferencePages.ini.EntireArrangePage;
import nak.nakloidGUI.gui.preferencePages.ini.GeneralIniPage;
import nak.nakloidGUI.gui.preferencePages.ini.NotePage;
import nak.nakloidGUI.gui.preferencePages.ini.OutputPage;
import nak.nakloidGUI.gui.preferencePages.ini.OverlapPage;

public class NakloidOption extends PreferenceDialog {
	public NakloidOption(Shell parentShell, PreferenceManager preferenceManager) {
		super(parentShell, preferenceManager);
		{
			PreferenceNode nakloidNode = new PreferenceNode("Nakloid", new GeneralIniPage());
			preferenceManager.addToRoot(nakloidNode);
			PreferenceNode nakloidOutputNode = new PreferenceNode("NakloidOutput", new OutputPage());
			preferenceManager.addTo("Nakloid", nakloidOutputNode);
			PreferenceNode nakloidNoteNode = new PreferenceNode("NakloidNote", new NotePage());
			preferenceManager.addTo("Nakloid", nakloidNoteNode);
			PreferenceNode nakloidEntireArrangeNode = new PreferenceNode("NakloidEntireArrange", new EntireArrangePage());
			preferenceManager.addTo("Nakloid", nakloidEntireArrangeNode);
			PreferenceNode nakloidEdgeArrangeNode = new PreferenceNode("NakloidEdgeArrange", new EdgeArrangePage());
			preferenceManager.addTo("Nakloid", nakloidEdgeArrangeNode);
			PreferenceNode nakloidOverlapNode = new PreferenceNode("NakloidOverlap", new OverlapPage());
			preferenceManager.addTo("Nakloid", nakloidOverlapNode);
			PreferenceNode nakloidAdvancedSettingNode = new PreferenceNode("NakloidAdvancedSetting", new AdvancedSettingPage());
			preferenceManager.addTo("Nakloid", nakloidAdvancedSettingNode);
		}
		{
			PreferenceNode nakloidGuiNode = new PreferenceNode("NakloidGui", new GeneralGuiPage());
			preferenceManager.addToRoot(nakloidGuiNode);
			PreferenceNode nakloidGuiMainWindowNode = new PreferenceNode("NakloidGuiMainWindow", new MainWindowPage());
			preferenceManager.addTo("NakloidGui", nakloidGuiMainWindowNode);
			PreferenceNode nakloidGuiNoteOptionNode = new PreferenceNode("NakloidGuiNoteOption", new NoteOptionPage());
			preferenceManager.addTo("NakloidGui", nakloidGuiNoteOptionNode);
			PreferenceNode nakloidGuiVoiceOptionNode = new PreferenceNode("NakloidGuiVoiceOption", new VoiceOptionPage());
			preferenceManager.addTo("NakloidGui", nakloidGuiVoiceOptionNode);
		}
		setPreferenceStore(NakloidGUI.preferenceStore);
	}


	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		control.getShell().setText("設定");
		getTreeViewer().collapseAll();
		getTreeViewer().expandAll();
		return control;
	}
}
