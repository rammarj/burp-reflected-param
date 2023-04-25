package burp.tab.tables;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.util.ReflectedMessage;

public abstract class RequestsTable extends JTable implements ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private final Map<String, ReflectedMessage> messages;
	private final DefaultTableModel model;
	private int contRequests;

	public RequestsTable() {
		this.messages = new HashMap<>();
		this.contRequests = 1;
		this.model = new DefaultTableModel(new String[] { "#id", "method", "url" }, 0);
		setModel(model);
		setRowSelectionAllowed(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selectedRow = getSelectedRow();
		if (selectedRow != -1) {
			String url = this.model.getValueAt(selectedRow, 2).toString();
			ReflectedMessage message = messages.get(url);
			IHttpRequestResponse requestResponse = message.getHttpRequestResponse();
			selectionChanged(message);
		}
	}

	public abstract void selectionChanged(ReflectedMessage message);
	
	public boolean alreadyExists(String url) {
		return this.messages.containsKey(url);
	}

	public void addRequest(ReflectedMessage rm, IRequestInfo requestInfo) {
		if (alreadyExists(requestInfo.getUrl().toString())) {
			return;
		}

		this.model.addRow(new String[] { String.valueOf(contRequests++), requestInfo.getMethod(),
				requestInfo.getUrl().toString() });
		this.messages.put(requestInfo.getUrl().toString(), rm);
	}

	public void clearTable() {
		this.model.setRowCount(0);
		this.messages.clear();
	}
	
	public ReflectedMessage getSelectedMessage() {
		int selectedRow = getSelectedRow();
		if (selectedRow != -1) {
			String url = this.model.getValueAt(selectedRow, 2).toString();
			ReflectedMessage message = messages.get(url);
			return message;
		}
		
		return null;
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false; // make cells not editable
	}
}
