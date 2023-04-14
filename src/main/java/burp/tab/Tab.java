package burp.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.IRequestInfo;
import burp.ITab;
import burp.ITextEditor;
import burp.tab.buttons.CleanRequestsButton;
import burp.tab.buttons.SendToRepeaterButton;
import burp.tab.tables.ParametersTable;
import burp.tab.tables.RequestsTable;

public class Tab extends JPanel implements ITab {

	private static final long serialVersionUID = 1L;
	private final DefaultTableModel requestTableModel, parametersTableModel;
    private ITextEditor msgeditorRequest, msgeditorResponse;
    private List<IHttpRequestResponse> requestsList;
    private List<List<IParameter>> parametersList;
    private int contRequests;
    private final IExtensionHelpers helpers;
    
	public Tab(IBurpExtenderCallbacks ibec) {
		super(new GridLayout());
        this.helpers = ibec.getHelpers();
        this.requestsList = new LinkedList<>();
        this.parametersList = new LinkedList<>();
        contRequests = 1;
        this.requestTableModel = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        this.parametersTableModel = new DefaultTableModel(new String[]{"name", "value", "type"}, 0);

        this.msgeditorRequest = ibec.createTextEditor();
        msgeditorRequest.getComponent().add(new PopupMenu());
        this.msgeditorRequest.setEditable(false);
        this.msgeditorResponse = ibec.createTextEditor();
        this.msgeditorResponse.setEditable(false);

        JPanel pnlRequests = new JPanel();
        Border brdRequestList = new TitledBorder(new LineBorder(Color.BLACK), "Requests list");
        pnlRequests.setBorder(brdRequestList);
        BoxLayout bxl_proyecto = new BoxLayout(pnlRequests, BoxLayout.Y_AXIS);
        pnlRequests.setLayout(bxl_proyecto);

        JTable requestsTable = new RequestsTable(parametersList, requestsList, msgeditorRequest, msgeditorResponse, parametersTableModel);
        requestsTable.setModel(this.requestTableModel);
        
        JScrollPane sclTblRequests = new JScrollPane();
        sclTblRequests.setPreferredSize(new Dimension(500, 220));
        sclTblRequests.setViewportView(requestsTable);
        pnlRequests.add(sclTblRequests);

        JTabbedPane tabRequests = new JTabbedPane();
        tabRequests.add("Request", this.msgeditorRequest.getComponent());
        tabRequests.add("Response", this.msgeditorResponse.getComponent());
        JTable parametersTable = new ParametersTable(requestsTable, msgeditorRequest, msgeditorResponse);
        parametersTable.setModel(this.parametersTableModel);
        JScrollPane sclTblTokens = new JScrollPane();
        sclTblTokens.setPreferredSize(new Dimension(400, 120));
        sclTblTokens.setViewportView(parametersTable);

        JPanel pnlReflectedParams = new JPanel(new GridLayout());
        pnlReflectedParams.setBorder(new TitledBorder(
                new LineBorder(Color.BLACK), "Reflected parameters"));
        pnlReflectedParams.add(sclTblTokens);


        JButton cleanRequestsButton = new CleanRequestsButton(requestsList, parametersList, requestTableModel, parametersTableModel);
        JButton sendToRepeaterButton = new SendToRepeaterButton(requestsTable, requestsList, ibec);

        JPanel pnlClearRrequests = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlClearRrequests.add(cleanRequestsButton);
        pnlReflectedParams.add(pnlClearRrequests);

        JSplitPane splpnIzquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splpnIzquierdo.add(pnlRequests);
        splpnIzquierdo.add(pnlReflectedParams);

        JPanel pnlIzquierdo = new JPanel(new BorderLayout());

        pnlIzquierdo.add(splpnIzquierdo, BorderLayout.CENTER);
        pnlIzquierdo.add(pnlClearRrequests, BorderLayout.SOUTH);

        JPanel pnlSendRepeater = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSendRepeater.add(sendToRepeaterButton);

        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.add(tabRequests, BorderLayout.CENTER);
        pnlRight.add(pnlSendRepeater, BorderLayout.SOUTH);

        JSplitPane contenedorPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contenedorPrincipal.add(pnlIzquierdo);
        contenedorPrincipal.add(pnlRight);

        contenedorPrincipal.setAutoscrolls(true);
        add(contenedorPrincipal);
        ibec.customizeUiComponent(this);
	}
	
	@Override
	public String getTabCaption() {
        return "Reflected Parameter";
	}

	@Override
	public Component getUiComponent() {
		return this;
	}


    public boolean alreadyExists(IHttpRequestResponse original) {
        URL url = helpers.analyzeRequest(original).getUrl();
        for (IHttpRequestResponse iHttpRequestResponse : requestsList) {
            URL u = helpers.analyzeRequest(iHttpRequestResponse).getUrl();
            if (u.toString().equals(url.toString())) {
                return true;
            }
        }
        return false;
    }

    public void sendToRequestsTable(IHttpRequestResponse rq, List<IParameter> pwm) {
        if (!alreadyExists(rq)) {
            this.requestsList.add(rq);
            this.parametersList.add(pwm);
            IRequestInfo requestInfo = this.helpers.analyzeRequest(rq);
            this.requestTableModel.addRow(new String[]{String.valueOf(contRequests++),
                requestInfo.getMethod(), requestInfo.getUrl().toString()});
        }
    }
    
}
