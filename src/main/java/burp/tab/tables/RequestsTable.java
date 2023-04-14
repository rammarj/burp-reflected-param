package burp.tab.tables;

import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.ITextEditor;

public class RequestsTable extends JTable implements ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private List<List<IParameter>> parametersList;
	private List<IHttpRequestResponse> requestsList;
	private ITextEditor msgeditorRequest, msgeditorResponse;
	private DefaultTableModel parametersTableModel;

	public RequestsTable(List<List<IParameter>> parametersList, List<IHttpRequestResponse> requestsList,
			ITextEditor msgeditorRequest, ITextEditor msgeditorResponse, DefaultTableModel parametersTableModel) {
		this.parametersList = parametersList;
		this.requestsList = requestsList;
		this.msgeditorRequest = msgeditorRequest;
		this.msgeditorResponse = msgeditorResponse;
		this.parametersTableModel = parametersTableModel;
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selectedRow = getSelectedRow();
		if (selectedRow != -1) {
			IHttpRequestResponse http_msg = requestsList.get(selectedRow);
			List<IParameter> tempParamsList = parametersList.get(selectedRow);
			msgeditorRequest.setText(http_msg.getRequest());
			msgeditorResponse.setText(http_msg.getResponse());
			parametersTableModel.setRowCount(0);
			for (IParameter get : tempParamsList) {
				sendToParametersTable(get);
			}
		}
	}

	private void sendToParametersTable(IParameter token) {
		String type = "";
		switch (token.getType()) {
		case IParameter.PARAM_COOKIE:
			type = "COOKIE";
			break;
		case IParameter.PARAM_BODY:
			type = "BODY";
			break;
		case IParameter.PARAM_URL:
			type = "URL";
			break;
		default:
			type = "NOT SUPPORTED YET";
			break;
		}
		this.parametersTableModel.addRow(new String[] { token.getName(), token.getValue(), type });
	}

}
