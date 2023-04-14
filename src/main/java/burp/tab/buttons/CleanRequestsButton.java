package burp.tab.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.table.DefaultTableModel;
import burp.IHttpRequestResponse;
import burp.IParameter;

public class CleanRequestsButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 1L;
	private List<IHttpRequestResponse> requestsList;
	private List<List<IParameter>> parametersList;
	private DefaultTableModel requestTableModel, parametersTableModel;
	
	public CleanRequestsButton(List<IHttpRequestResponse> requestsList, List<List<IParameter>> parametersList, DefaultTableModel requestTableModel, DefaultTableModel parametersTableModel) {
		super("Clear requests table");
		this.requestsList = requestsList;
		this.parametersList = parametersList;
		this.requestTableModel = requestTableModel;
		this.parametersTableModel = parametersTableModel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.requestsList.clear();
        this.parametersList.clear();
        this.requestTableModel.setRowCount(0);
        this.parametersTableModel.setRowCount(0);
	}

}
