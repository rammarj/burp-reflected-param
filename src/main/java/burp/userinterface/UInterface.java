package burp.userinterface;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.IRequestInfo;
import burp.ITextEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Joaquin R. Martinez
 */
public class UInterface extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final DefaultTableModel requestTableModel, parametersTableModel;
    private final JButton cleanRequestsButton, sendToRepeaterButton;
    private ITextEditor msgeditorRequest, msgeditorResponse;
    private List<IHttpRequestResponse> requestsList;
    private List<List<IParameter>> parametersList;
    private List<IParameter> tempParamsList;
    private final IBurpExtenderCallbacks ibec;
    private int contRequests;
    private JTable requestsTable, parametersTable;
    private final IExtensionHelpers helpers;

    public UInterface(IBurpExtenderCallbacks ibec) {
        super(new GridLayout());
        this.ibec = ibec;
        this.helpers = ibec.getHelpers();
        this.requestsList = new LinkedList<>();
        this.parametersList = new LinkedList<>();
        contRequests = 1;
        this.cleanRequestsButton = new JButton("Clear requests table");
        this.cleanRequestsButton.addActionListener(this);

        this.sendToRepeaterButton = new JButton("Send to repeater");
        this.sendToRepeaterButton.addActionListener(this);

        this.requestTableModel = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        tempParamsList = null;
        this.parametersTableModel = new DefaultTableModel(new String[]{"name", "value", "type"}, 0);

        //crear los httpMessageEditors para presentar los requests/responses de los usuarios 1 y 2 y el de CSRF
        this.msgeditorRequest = ibec.createTextEditor();
        msgeditorRequest.getComponent().add(new PopupMenu());
        this.msgeditorRequest.setEditable(false);
        this.msgeditorResponse = ibec.createTextEditor();
        this.msgeditorResponse.setEditable(false);
        //crear panel de requests
        JPanel pnlRequests = new JPanel();
        Border brdRequestList = new TitledBorder(new LineBorder(Color.BLACK), "Requests list");
        pnlRequests.setBorder(brdRequestList);
        BoxLayout bxl_proyecto = new BoxLayout(pnlRequests, BoxLayout.Y_AXIS);
        pnlRequests.setLayout(bxl_proyecto);
        //eleccion de proyecto
        //crear tabla requests

        requestsTable = new JTable();
        //tbl_requests.setEnabled(false);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = requestsTable.getSelectedRow();
                if (selectedRow != -1) {
                    IHttpRequestResponse http_msg = requestsList.get(selectedRow);
                    tempParamsList = parametersList.get(selectedRow);
                    //LinkedList<ParameterWithMarkers> params = parametersList.get(selectedRow);
                    msgeditorRequest.setText(http_msg.getRequest());
                    msgeditorResponse.setText(http_msg.getResponse());
                    parametersTableModel.setRowCount(0);
                    for (IParameter get : tempParamsList) {
                        sendToParametersTable(get);
                    }
                }
            }
        });
        requestsTable.setModel(this.requestTableModel);
        JScrollPane sclTblRequests = new JScrollPane();
        sclTblRequests.setPreferredSize(new Dimension(500, 220));
        sclTblRequests.setViewportView(requestsTable);
        pnlRequests.add(sclTblRequests);
        //crear panel preview HTTP
        //crear panel request preview
        JTabbedPane tabRequests = new JTabbedPane();
        //agregar al tab 2 los requestst/responeses del usuario 2
        tabRequests.add("Request", this.msgeditorRequest.getComponent());
        tabRequests.add("Response", this.msgeditorResponse.getComponent());
        //agregar al tab 2 los requestst/responeses del usuario 2
        //agragar los tabs del usuario 1 y 2 y el de CSRF al tab principal

        parametersTable = new JTable();
        parametersTable.setModel(this.parametersTableModel);
        parametersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        parametersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        	
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = parametersTable.getSelectedRow();
                if (selected != -1) {
                    IParameter parametro = tempParamsList.get(selected);
                    msgeditorRequest.setSearchExpression(parametro.getValue());
                    msgeditorResponse.setSearchExpression(parametro.getValue());
                }
            }
        });
        JScrollPane sclTblTokens = new JScrollPane();
        sclTblTokens.setPreferredSize(new Dimension(400, 120));
        sclTblTokens.setViewportView(parametersTable);

        JPanel pnlReflectedParams = new JPanel(new GridLayout());
        pnlReflectedParams.setBorder(new TitledBorder(
                new LineBorder(Color.BLACK), "Reflected parameters"));
        pnlReflectedParams.add(sclTblTokens);

        JPanel pnlClearRrequests = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlClearRrequests.add(this.cleanRequestsButton);
        pnlReflectedParams.add(pnlClearRrequests);

        JSplitPane splpnIzquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splpnIzquierdo.add(pnlRequests);
        splpnIzquierdo.add(pnlReflectedParams);

        JPanel pnlIzquierdo = new JPanel(new BorderLayout());

        pnlIzquierdo.add(splpnIzquierdo, "Center");
        pnlIzquierdo.add(pnlClearRrequests, "South");

        JPanel pnlSendRepeater = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSendRepeater.add(this.sendToRepeaterButton);

        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.add(tabRequests, "Center");
        pnlRight.add(pnlSendRepeater, "South");

        JSplitPane contenedorPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contenedorPrincipal.add(pnlIzquierdo);
        contenedorPrincipal.add(pnlRight);

        contenedorPrincipal.setAutoscrolls(true);
        add(contenedorPrincipal);
        ibec.customizeUiComponent(this);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.cleanRequestsButton) {
            this.requestsList.clear();
            this.parametersList.clear();
            this.requestTableModel.setRowCount(0);
            this.parametersTableModel.setRowCount(0);
        } else if (source == this.sendToRepeaterButton) {
            int selected = requestsTable.getSelectedRow();
            if (selected != -1) {
                IHttpRequestResponse msgHTTP = requestsList.get(selected);
                IRequestInfo request = helpers.analyzeRequest(msgHTTP);
                URL url = request.getUrl();
                ibec.sendToRepeater(url.getHost(), url.getPort(), url.getProtocol().equalsIgnoreCase("https"),
                         msgHTTP.getRequest(), null);
            }
        }
    }

    public void sendToRequestsTable(IHttpRequestResponse rq, List<IParameter> pwm) {
        if (!alreadyExists(rq)) {
            this.requestsList.add(rq);
            this.parametersList.add(pwm);
            IRequestInfo requestInfo = this.ibec.getHelpers().analyzeRequest(rq);
            this.requestTableModel.addRow(new String[]{String.valueOf(contRequests++),
                requestInfo.getMethod(), requestInfo.getUrl().toString()});
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
        this.parametersTableModel.addRow(new String[]{token.getName(), token.getValue(), type});
    }

}
