package nak.nakloidGUI.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import nak.nakloidGUI.NakloidGUI;

public class LoggerWindow extends Dialog {
	Text text;

	public LoggerWindow(Shell parent) {
		super(parent);
		setShellStyle(SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}

	@Override
	protected Point getInitialSize() {
		int defaultSizeX = NakloidGUI.preferenceStore.getInt("gui.mainWindow.logWindowSizeX");
		int defaultSizeY = NakloidGUI.preferenceStore.getInt("gui.mainWindow.logWindowSizeY");
		if (defaultSizeX>0 && defaultSizeY>0) {
			return new Point(defaultSizeX, defaultSizeY);
		}
		return new Point(400, 300);
	}

	@Override
	protected Point getInitialLocation(Point initialSize)  {
		int defaultPositionX = NakloidGUI.preferenceStore.getInt("gui.mainWindow.logWindowPositionX");
		int defaultPositionY = NakloidGUI.preferenceStore.getInt("gui.mainWindow.logWindowPositionY");
		if (defaultPositionX!=0 && defaultPositionY!=0) {
			return new Point(defaultPositionX, defaultPositionY);
		}
		return super.getInitialLocation(getInitialSize());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Nakloid ログ");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().addControlListener(new ControlListener() {
			@Override
			public void controlResized(final ControlEvent e) {
				NakloidGUI.preferenceStore.setValue("gui.mainWindow.logWindowSizeX", getShell().getSize().x);
				NakloidGUI.preferenceStore.setValue("gui.mainWindow.logWindowSizeY", getShell().getSize().y);
			}
			@Override
			public void controlMoved(final ControlEvent e) {
				NakloidGUI.preferenceStore.setValue("gui.mainWindow.logWindowPositionX", getShell().getLocation().x);
				NakloidGUI.preferenceStore.setValue("gui.mainWindow.logWindowPositionY", getShell().getLocation().y);
			}
		});

		Composite composite = (Composite)super.createDialogArea(parent);
		GridLayout layComposite = new GridLayout(1, false);
		layComposite.marginHeight = layComposite.horizontalSpacing = layComposite.marginWidth = layComposite.verticalSpacing = 0;
		composite.setLayout(layComposite);

		text = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		text.setEditable(false);
		OutputStream out = new OutputStream() {
			private ByteBuffer buffer = ByteBuffer.allocate(65535);
			private final Object obj = new Object();
			@Override
			public void write(final int b) throws IOException {
				synchronized (obj) {
					if (text.isDisposed()) {
						return;
					}
					buffer.put((byte)b);
				}
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				super.write(b, off, len);
				flush();
			}
			@Override
			public void flush() throws IOException {
				synchronized (obj) {
					text.append(new String(buffer.array(), Charset.defaultCharset()));
					buffer = ByteBuffer.allocate(65535);
				}
			}
		};
		final PrintStream oldOut = System.out;
		System.setOut(new PrintStream(out));
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				System.setOut(oldOut);
			}
		});
		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	public void flush() {
		if (!text.isDisposed()) {
			text.setText("");
		}
	}

	public String getTextAndFlush() {
		String tmp = "";
		if (!text.isDisposed()) {
			tmp = text.getText();
			text.setText("");
		}
		return tmp;
	}
}
