/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp.userinterface;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.IRequestInfo;
import burp.ITextEditor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Joaquin R. Martinez
 */
public class UInterface extends JPanel implements ActionListener {

    private DefaultTableModel mdl_tblRequests, mdl_tblParameters;
    private JButton btn_limpiarRequests, btn_bdTestConexion;
    private ITextEditor msgeditor_request, msgeditor_response;
    private JCheckBox chb_automaticAddToList;
    private LinkedList<IHttpRequestResponse> lst_request;
    private LinkedList<LinkedList<IParameter>> lst_parameters;
    private LinkedList<IParameter> lst_temp_params;
    private IBurpExtenderCallbacks ibec;
    private int contRequests;
    private JTable tbl_requests, tbl_parameters;
    private IExtensionHelpers helpers;
    private int selectedRow;
    private JTextField txt_host, txt_server;

    public UInterface(IBurpExtenderCallbacks ibec) {
        //super(new BorderLayout(10,10));
        this.setBackground(Color.WHITE);
        setLayout(new GridLayout());
        this.ibec = ibec;
        selectedRow = -1;
        this.txt_host = new JTextField(20);
        this.txt_server = new JTextField(20);
        this.helpers = ibec.getHelpers();
        this.lst_request = new LinkedList<>();
        this.lst_parameters = new LinkedList<>();
        contRequests = 1;
        chb_automaticAddToList = new JCheckBox("Add request to list (If sends CSRF Tokens)");
        this.btn_limpiarRequests = new JButton("Clear requests table");
        this.btn_limpiarRequests.addActionListener(this);
        this.mdl_tblRequests = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        lst_temp_params = null;
        this.mdl_tblParameters = new DefaultTableModel(new String[]{"name", "type"}, 0);
        //fields de conexion a datos
        this.btn_bdTestConexion = new JButton("Connect to database");
        this.btn_bdTestConexion.addActionListener(this);
        
        //crear los httpMessageEditors para presentar los requests/responses de los usuarios 1 y 2 y el de CSRF
        this.msgeditor_request = ibec.createTextEditor();
        this.msgeditor_request.setEditable(false);
        this.msgeditor_response = ibec.createTextEditor();
        this.msgeditor_response.setEditable(false);
        //this.mdl_tblRequests.
        //crear panel de requests
        JPanel pnl_requests = new JPanel();
        Border brd_pnlIdors = new TitledBorder(new LineBorder(Color.BLACK), "Requests list");
        pnl_requests.setBorder(brd_pnlIdors);
        BoxLayout bxl_proyecto = new BoxLayout(pnl_requests, BoxLayout.Y_AXIS);
        pnl_requests.setLayout(bxl_proyecto);
        //eleccion de proyecto
        //crear tabla requests

        tbl_requests = new JTable();
        //tbl_requests.setEnabled(false);
        tbl_requests.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_requests.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = tbl_requests.getSelectedRow();
                if (selectedRow != -1) {
                    IHttpRequestResponse http_msg = lst_request.get(selectedRow);
                    try {
                        lst_temp_params = lst_parameters.get(selectedRow);
                        //LinkedList<ParameterWithMarkers> params = lst_parameters.get(selectedRow);
                        msgeditor_request.setText(http_msg.getRequest());
                        msgeditor_response.setText(http_msg.getResponse());
                        mdl_tblParameters.setRowCount(0);
                        for (IParameter get : lst_temp_params) {
                            sendToParametersTable(get);
                        }
                    } catch (Exception ex) {
                        try {
                            ibec.getStderr().write(ex.getMessage().getBytes());
                        } catch (IOException ex1) { }
                    }
                }
            }
        });
        tbl_requests.setModel(this.mdl_tblRequests);
        JScrollPane scl_tblRequests = new JScrollPane();
        scl_tblRequests.setPreferredSize(new Dimension(500, 220));
        scl_tblRequests.setViewportView(tbl_requests);
        JPanel pnl_host = new JPanel(); 
        pnl_host.add(new JLabel("Host:"));
        pnl_host.add(this.txt_host);
        pnl_host.add(new JLabel("My Server:"));
        pnl_host.add(this.txt_server);
        pnl_host.add(this.btn_limpiarRequests);
        pnl_requests.add(pnl_host);
        pnl_requests.add(scl_tblRequests);
        //crear panel preview HTTP
        //crear panel request preview
        JTabbedPane tab_requests = new JTabbedPane();
        //agregar al tab 2 los requestst/responeses del usuario 2
        tab_requests.add("Request", this.msgeditor_request.getComponent());
        tab_requests.add("Response", this.msgeditor_response.getComponent());
        //agregar al tab 2 los requestst/responeses del usuario 2
        //agragar los tabs del usuario 1 y 2 y el de CSRF al tab principal

        tbl_parameters = new JTable();
        tbl_parameters.setModel(this.mdl_tblParameters);
        tbl_parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_parameters.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selected = tbl_parameters.getSelectedRow();
                if (selected != -1 && selectedRow != -1) {
                    try {
                        IParameter parametro = lst_temp_params.get(selected);
                        msgeditor_request.setSearchExpression(parametro.getValue());
                        msgeditor_response.setSearchExpression(parametro.getValue());
                    } catch (Exception ex) {
                        try {
                            ibec.getStderr().write(ex.getMessage().getBytes());
                        } catch (IOException ex1) { }
                    }
                }
            }
        });
        JScrollPane scl_tblTokens = new JScrollPane();
        scl_tblTokens.setPreferredSize(new Dimension(400, 120));
        scl_tblTokens.setViewportView(tbl_parameters);

        JPanel pnl_btnsTablaTokens = new JPanel();
        BoxLayout bxl_pnlBtnTokens = new BoxLayout(pnl_btnsTablaTokens, BoxLayout.Y_AXIS);
        pnl_btnsTablaTokens.setLayout(bxl_pnlBtnTokens);
        //jPanel.setPreferredSize(new Dimension(100,200));
        pnl_btnsTablaTokens.add(this.chb_automaticAddToList);

        JPanel pnl_csrf = new JPanel(new GridLayout());
        pnl_csrf.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Reflected parameters"));
        pnl_csrf.add(scl_tblTokens);
        JSplitPane pnl_izquierdo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pnl_izquierdo.add(pnl_requests);
        pnl_izquierdo.add(pnl_csrf);

        JSplitPane contenedorPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contenedorPrincipal.add(pnl_izquierdo);
        contenedorPrincipal.add(tab_requests);
        
        //this.tbl_requests.set
        
        contenedorPrincipal.setAutoscrolls(true);
        add(contenedorPrincipal);
        ibec.customizeUiComponent(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == this.btn_bdTestConexion) {
            //this.db_conexion.createStatement();
        } else if (s == this.btn_limpiarRequests) {
            this.lst_request.clear();
            this.lst_parameters.clear();
            //this.lst_temp_params.clear();
            this.mdl_tblRequests.setRowCount(0);
            this.mdl_tblParameters.setRowCount(0);
        }
    }

    public void sendToRequestsTable(IHttpRequestResponse rq, LinkedList<IParameter> pwm) {
        this.lst_request.add(rq);
        this.lst_parameters.add(pwm);
        IRequestInfo requestInfo = this.ibec.getHelpers().analyzeRequest(rq);
        //sendToParametersTable(pwm);
        this.mdl_tblRequests.addRow(new String[]{"" + contRequests++, requestInfo.getMethod(), requestInfo.getUrl().toString()});
    }

    private void sendToParametersTable(IParameter token) {
        //IParameter token = pwm.getParameter();
        String name = token.getName();
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
        this.mdl_tblParameters.addRow(new String[]{name, type});
    }

    public boolean automaticAdd() {
        return this.chb_automaticAddToList.isSelected();
    }
    
    /**
     * Busca una cadena en bytes y devuelve un par (comienzo y fin de la
     * cadena). Si no encuentra nada retorna NULL
     */
    private int[] indexOf(byte[] data, byte[] search, int start, int end) {
        int start_ = helpers.indexOf(data, search, true, start, end);
        if (start_ != -1) {
            int end_ = start_ + search.length;
            return new int[]{start_, end_};
        }
        return null;
    }

    /**
     * Devuelve todas las coincidencias 'search' en 'data'.
     */
    private LinkedList<int[]> recursiveIndexOf(byte[] data, byte[] search) {
        LinkedList<int[]> ret = new LinkedList<>();
        int start = 0, end = data.length - 1;
        int[] index = null;
        index = indexOf(data, search, start, end);
        while (index != null) {
            ret.add(index);
            start = index[1]; //ahora empezara buscando desde donde termino el anterior
            index = indexOf(data, search, start, end);
        }
        return ret;
    }

    public String getHost(){
        return this.txt_host.getText().trim();
    }
    
    public String miServer(){
        return this.txt_server.getText().trim();
    }
    
}
