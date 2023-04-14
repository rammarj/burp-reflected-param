package burp.tab.tables;

import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import burp.IParameter;
import burp.ITextEditor;

public class ParametersTable extends JTable implements ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private JTable parametersTable;
	private List<IParameter> tempParamsList;
	private ITextEditor msgeditorRequest, msgeditorResponse;
	
	public ParametersTable(JTable parametersTable, ITextEditor msgeditorRequest, ITextEditor msgeditorResponse) {
		this.parametersTable = parametersTable;
		this.msgeditorRequest = msgeditorRequest;
		this.msgeditorResponse = msgeditorResponse;
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selected = parametersTable.getSelectedRow();
        if (selected != -1) {
            IParameter parametro = tempParamsList.get(selected);
            msgeditorRequest.setSearchExpression(parametro.getValue());
            msgeditorResponse.setSearchExpression(parametro.getValue());
        }
	}
}
