package nak.nakloidGUI.gui;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
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
import nak.nakloidGUI.models.Note;

public class NotesWindow extends Dialog implements CoreDataSubscriber {
	CoreData coreData;
	TableViewer tableViewer;

	public NotesWindow(Shell parentShell, CoreData coreData) {
		super(parentShell);
		setShellStyle(SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
		this.coreData = coreData;
		coreData.addSubscribers(this);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 360);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		control.getShell().setText("音符一覧");
		return control;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
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
		tableViewer.setLabelProvider(new NoteLabelProvider());
		tableViewer.setContentProvider(new NoteContentProvider());
		tableViewer.setInput(coreData.getNotes().stream().toArray());

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
				Note note = (Note)sel.getFirstElement();
				NoteOption dialog = new NoteOption(getShell(), coreData, note);
				dialog.open();
			}
		});

		return parent;
	}

	@Override
	public void updateScore() {
		if (getShell()!=null && !getShell().isDisposed()) {
			tableViewer.setInput(coreData.getNotes().stream().toArray());
			tableViewer.refresh();
		}
	}

	@Override
	public void updatePitches() {}

	@Override
	public void updateVocal() {}

	@Override
	public void updateSongWaveform() {}

	@Override
	public void updateSaveState() {}

	private Table createTable(Composite parent) {
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);

		TableColumn pronunciationColumn = new TableColumn(table, SWT.LEFT);
		pronunciationColumn.setText("発音");
		TableColumn offsetColumn = new TableColumn(table, SWT.RIGHT);
		offsetColumn.setText("MIDIノート番号");
		TableColumn overlapColumn = new TableColumn(table, SWT.RIGHT);
		overlapColumn.setText("開始時間(ms)");
		TableColumn preutteranceColumn = new TableColumn(table, SWT.RIGHT);
		preutteranceColumn.setText("終了時間(ms)");
		TableColumn consonantColumn = new TableColumn(table, SWT.RIGHT);
		consonantColumn.setText("基準音量");

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn[] columns = table.getColumns();
		Arrays.stream(columns).forEach(c->c.pack());
		columns[0].setWidth(60);

		return table;
	}

	private class NoteContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[])inputElement;
		}
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}

	private class NoteLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Note note = (Note)element;
			switch (columnIndex) {
			case 0:
				return note.getPronunciationAliasString();
			case 1:
				return String.valueOf(note.getBasePitch());
			case 2:
				return String.valueOf(note.getStart());
			case 3:
				return String.valueOf(note.getEnd());
			case 4:
				return String.valueOf(note.getBaseVelocity());
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
			return ((Note)e).getPronunciationAliasString().contains(text.getText());
		}
	}
}
