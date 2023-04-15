package burp.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.net.URL;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.IRequestInfo;
import burp.ITab;
import burp.ITextEditor;
import burp.tab.tables.ParametersTable;
import burp.tab.tables.RequestsTable;
import burp.util.ReflectedMessage;

public class Tab extends JPanel implements ITab {

	private static final long serialVersionUID = 1L;
	private ITextEditor msgeditorRequest, msgeditorResponse;
	private final IExtensionHelpers helpers;
	private final RequestsTable requestsTable;

	public Tab(IBurpExtenderCallbacks ibec) {
		super(new GridLayout());
		this.helpers = ibec.getHelpers();

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

		ParametersTable parametersTable = new ParametersTable(msgeditorRequest, msgeditorResponse);
		requestsTable = new RequestsTable(msgeditorRequest, msgeditorResponse, helpers) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectionChanged(ReflectedMessage message) {
				parametersTable.setParameters(message.getPwm());
			}
		};

		JScrollPane sclTblRequests = new JScrollPane();
		sclTblRequests.setPreferredSize(new Dimension(500, 220));
		sclTblRequests.setViewportView(requestsTable);
		pnlRequests.add(sclTblRequests);

		JTabbedPane tabRequests = new JTabbedPane();
		tabRequests.add("Request", this.msgeditorRequest.getComponent());
		tabRequests.add("Response", this.msgeditorResponse.getComponent());
		
		JScrollPane sclTblTokens = new JScrollPane();
		sclTblTokens.setPreferredSize(new Dimension(400, 120));
		sclTblTokens.setViewportView(parametersTable);

		JPanel pnlReflectedParams = new JPanel(new GridLayout());
		pnlReflectedParams.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Reflected parameters"));
		pnlReflectedParams.add(sclTblTokens);

		JButton cleanRequestsButton = new JButton("clear requests");
		cleanRequestsButton.addActionListener(e -> {
			requestsTable.clearTable();
			parametersTable.clearTable();
		});

		JButton sendToRepeaterButton = new JButton("Send to repeater");
		sendToRepeaterButton.addActionListener(e -> {
			ReflectedMessage message = requestsTable.getSelectedMessage();
			if (message != null) {
				IHttpRequestResponse rq = message.getiHttpRequestResponse();
				IRequestInfo request = ibec.getHelpers().analyzeRequest(rq);
				URL url = request.getUrl();
				ibec.sendToRepeater(url.getHost(), url.getPort(), url.getProtocol().equalsIgnoreCase("https"),
						rq.getRequest(), null);
			}
		});

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

	public void sendToRequestsTable(IHttpRequestResponse rq, List<IParameter> pwm) {
		requestsTable.addRequest(new ReflectedMessage(rq, pwm));
	}

	public boolean alreadyExists(String url) {
		return requestsTable.alreadyExists(url);
	}

}
