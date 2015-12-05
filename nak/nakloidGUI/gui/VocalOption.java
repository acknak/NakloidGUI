package nak.nakloidGUI.gui;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import nak.nakloidGUI.coredata.CoreData;
import nak.nakloidGUI.coredata.CoreData.CoreDataSubscriber;
import nak.nakloidGUI.models.Voice;
import nak.nakloidGUI.models.Voice.ParameterType;

public class VocalOption extends Dialog implements CoreDataSubscriber {
	CoreData coreData;
	TableViewer tableViewer;

	public VocalOption(Shell parentShell, CoreData coreData) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.coreData = coreData;
		coreData.addSubscribers(this);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		control.getShell().setText("ボーカル設定");
		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite)super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		Label lbFilter = new Label(container, SWT.NONE);
		lbFilter.setText("発音フィルター");
		Text txtFilter = new Text(container, SWT.BORDER);
		txtFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tmp_composite = new Composite(container, SWT.FILL);
		tmp_composite.setLayout(new FillLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		tmp_composite.setLayoutData(data);
		tableViewer = new TableViewer(createTable(tmp_composite));
		tableViewer.setLabelProvider(new VoiceLabelProvider());
		tableViewer.setContentProvider(new VoiceContentProvider());
		tableViewer.setInput(coreData.getVoicesArray());

		final PronunciationFilter pronunciationFilter = new PronunciationFilter(txtFilter);
		txtFilter.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e) {
				tableViewer.addFilter(pronunciationFilter);
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				Voice voice = (Voice)sel.getFirstElement();
				try {
					VoiceOption dialog = new VoiceOption(getShell(), coreData, voice);
					dialog.open();
				} catch (IOException e) {
					MessageDialog.openError(getShell(), "NakloidGUI", "音声の読込に失敗しました。\n"+e.toString()+e.getMessage());
				}
			}
		});

		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "完了", true);
	}

	@Override
	public void updateScore() {}

	@Override
	public void updatePitches() {}

	@Override
	public void updateVocal() {
		if (getShell()!=null && !getShell().isDisposed()) {
			tableViewer.setInput(coreData.getVoicesArray());
			tableViewer.refresh();
		}
	}

	@Override
	public void updateSongWaveform() {}

	private Table createTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);

		TableColumn pronunciationColumn = new TableColumn(table, SWT.LEFT);
		pronunciationColumn.setText(ParameterType.PRONUNCIATION.getEnString());
		TableColumn offsetColumn = new TableColumn(table, SWT.RIGHT);
		offsetColumn.setText(ParameterType.OFFSET.getEnString());
		TableColumn overlapColumn = new TableColumn(table, SWT.RIGHT);
		overlapColumn.setText(ParameterType.OVERLAP.getEnString());
		TableColumn preutteranceColumn = new TableColumn(table, SWT.RIGHT);
		preutteranceColumn.setText(ParameterType.PREUTTERANCE.getEnString());
		TableColumn consonantColumn = new TableColumn(table, SWT.RIGHT);
		consonantColumn.setText(ParameterType.CONSONANT.getEnString());
		TableColumn blankColumn = new TableColumn(table, SWT.RIGHT);
		blankColumn.setText(ParameterType.BLANK.getEnString());
		TableColumn fileColumn = new TableColumn(table, SWT.LEFT);
		fileColumn.setText("ファイル名");

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn[] columns = table.getColumns();
		Arrays.stream(columns).forEach(c->c.pack());
		columns[0].setWidth(60);
		columns[columns.length-1].setWidth(400);

		return table;
	}

	private class VoiceContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	private class VoiceLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Voice voice = (Voice)element;
			switch (columnIndex) {
			case 0:
				return voice.getPronunciationString();
			case 1:
				return String.valueOf(voice.getOffset());
			case 2:
				return String.valueOf(voice.getOverlap());
			case 3:
				return String.valueOf(voice.getPreutterance());
			case 4:
				return String.valueOf(voice.getConsonant());
			case 5:
				return String.valueOf(voice.getBlank());
			case 6:
				return voice.getWavPath().toString();
			default:
				return null;
			}
		}
	}

	private class PronunciationFilter extends ViewerFilter {
		private Text text;
		public PronunciationFilter(Text text) {
			this.text = text;
		}
		public boolean select(Viewer viewer, Object parent, Object e) {
			return ((Voice)e).getPronunciationString().contains(text.getText());
		}
	}
}
