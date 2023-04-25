package burp.tab.tables;

import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import burp.IParameter;

public class ParametersTable extends JTable implements ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private final DefaultTableModel model;
	
	public ParametersTable() {
		this.model = new DefaultTableModel(new String[]{"name", "value", "type"}, 0);
		setModel(model);
		setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selected = getSelectedRow();
        if (selected != -1) {
            String parameterValue = this.model.getValueAt(selected, 0).toString();
            selectedValueChanged(parameterValue);
        }
	}
	
	public void selectedValueChanged(String value) {}
	
	public void clearTable() {
		model.setRowCount(0);
	}
	
	private String getParameterType(byte type) {
		switch (type) {
		case IParameter.PARAM_COOKIE:
			return "COOKIE";
		case IParameter.PARAM_BODY:
			return "BODY";
		case IParameter.PARAM_URL:
			return "URL";
		default:
			return "NOT SUPPORTED YET";
		}
	}
	
	public void setParameters(List<IParameter> p) {
		this.model.setRowCount(0);
		for (IParameter iParameter : p) {
			String[] data = new String[] {iParameter.getName(), iParameter.getValue(), getParameterType(iParameter.getType())};
			this.model.addRow(data);
		}
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false; // make cell not editable when user double clicks in.
	}
}
