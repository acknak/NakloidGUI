package nak.nakloidGUI.gui.preferencePages;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class DoubleFieldEditor extends StringFieldEditor {
	private double min=0.001, max=99999;

	public DoubleFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		setTextLimit(5);
		setEmptyStringAllowed(false);
		createControl(parent);
	}

	@Override
	protected boolean checkState() {
		try {
			double doubleTmp = Double.valueOf(getTextControl().getText());
			if (min<=doubleTmp && doubleTmp<=max) {
				clearErrorMessage();
				return true;
			}
		} catch (NumberFormatException e) {}
		showErrorMessage();
		return false;
	}

	@Override
	protected void doLoad() {
		getTextControl().setText(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		getTextControl().setText(getPreferenceStore().getDefaultString(getPreferenceName()));
		valueChanged();
	}

	@Override
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(), Double.valueOf(getTextControl().getText()));
	}

	public double getDoubleValue() throws NumberFormatException {
		return Double.valueOf(getStringValue());
	}

	public void setValidRange(double min, double max) {
		this.min = min;
		this.max = max;
	}
}
