package burp.tab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private boolean isInScope;

	public Tab(IBurpExtenderCallbacks ibec) {
		super(new GridLayout());
		this.helpers = ibec.getHelpers();

		this.msgeditorRequest = ibec.createTextEditor();
		msgeditorRequest.getComponent().add(new PopupMenu());
		this.msgeditorRequest.setEditable(false);
		this.msgeditorResponse = ibec.createTextEditor();
		this.msgeditorResponse.setEditable(false);
		this.isInScope = false;

		// create tables
		ParametersTable parametersTable = new ParametersTable() {
			@Override
			public void selectedValueChanged(String value) {
				msgeditorRequest.setSearchExpression(value);
				msgeditorResponse.setSearchExpression(value);
			}
		};
		JScrollPane sclTblTokens = new JScrollPane();
		sclTblTokens.setPreferredSize(new Dimension(400, 120));
		sclTblTokens.setViewportView(parametersTable);
		JPanel pnlReflectedParams = new JPanel(new GridLayout());
		pnlReflectedParams.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Reflected parameters"));
		pnlReflectedParams.add(sclTblTokens);

		requestsTable = new RequestsTable() {
			private static final long serialVersionUID = 1L;
			@Override
			public void selectionChanged(ReflectedMessage message) {
				IHttpRequestResponse requestResponse = message.getHttpRequestResponse();
				parametersTable.setParameters(message.getParameters());
				msgeditorRequest.setText(requestResponse.getRequest());
				msgeditorResponse.setText(requestResponse.getResponse());
			}
		};
		JPanel leftSidePanel = new JPanel(new BorderLayout());
		leftSidePanel.add(createIsInScopeCheckBox(), BorderLayout.NORTH);
		JSplitPane tablesPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		tablesPanel.add(createRequestsPanel());
		tablesPanel.add(pnlReflectedParams);
		leftSidePanel.add(tablesPanel, BorderLayout.CENTER);
		leftSidePanel.add(createCleanRequestsButtonPanel(e -> {
			requestsTable.clearTable();
			parametersTable.clearTable();
		}), BorderLayout.SOUTH);

		JPanel rightSidePanel = new JPanel(new BorderLayout());
		JTabbedPane tabRequests = new JTabbedPane();
		tabRequests.add("Request", this.msgeditorRequest.getComponent());
		tabRequests.add("Response", this.msgeditorResponse.getComponent());
		rightSidePanel.add(tabRequests, BorderLayout.CENTER);
		rightSidePanel.add(createSendToRepeaterButtonPanel(ibec, e -> {
			ReflectedMessage message = requestsTable.getSelectedMessage();
			if (message != null) {
				sendToRepeater(ibec, message.getHttpRequestResponse());
			}
		}), BorderLayout.SOUTH);

		JSplitPane mainContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainContainer.add(leftSidePanel);
		mainContainer.add(rightSidePanel);

		mainContainer.setAutoscrolls(true);
		add(mainContainer);
		ibec.customizeUiComponent(this);
	}

	private JCheckBox createIsInScopeCheckBox() {
		JCheckBox isInScopeCheckBox = new JCheckBox("Validate only in scope domains");
		isInScopeCheckBox.addChangeListener(e -> {
			this.isInScope = isInScopeCheckBox.isSelected();
		});
		return isInScopeCheckBox;
	}

	private JPanel createCleanRequestsButtonPanel(ActionListener l) {
		JButton cleanRequestsButton = new JButton("clear requests");
		cleanRequestsButton.addActionListener(l);
		JPanel pnlClearRrequests = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlClearRrequests.add(cleanRequestsButton);
		return pnlClearRrequests;
	}

	private JPanel createRequestsPanel() {
		JPanel pnlRequests = new JPanel();
		Border brdRequestList = new TitledBorder(new LineBorder(Color.BLACK), "Requests list");
		pnlRequests.setBorder(brdRequestList);
		BoxLayout bxl_proyecto = new BoxLayout(pnlRequests, BoxLayout.Y_AXIS);
		pnlRequests.setLayout(bxl_proyecto);

		// create panel
		JScrollPane sclTblRequests = new JScrollPane();
		sclTblRequests.setPreferredSize(new Dimension(500, 220));
		sclTblRequests.setViewportView(requestsTable);
		pnlRequests.add(sclTblRequests);
		return pnlRequests;
	}

	private JPanel createSendToRepeaterButtonPanel(IBurpExtenderCallbacks ibec, ActionListener l) {
		JPanel pnlSendRepeater = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton sendToRepeaterButton = new JButton("Send to repeater");
		sendToRepeaterButton.addActionListener(l);
		pnlSendRepeater.add(sendToRepeaterButton);
		return pnlSendRepeater;
	}

	private void sendToRepeater(IBurpExtenderCallbacks ibec, IHttpRequestResponse rq) {
		IRequestInfo request = ibec.getHelpers().analyzeRequest(rq);
		URL url = request.getUrl();
		ibec.sendToRepeater(url.getHost(), url.getPort(), url.getProtocol().equalsIgnoreCase("https"), rq.getRequest(),
				null);
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
		IRequestInfo ri = this.helpers.analyzeRequest(rq.getRequest());
		requestsTable.addRequest(new ReflectedMessage(rq, pwm), ri);
	}

	public boolean alreadyExists(String url) {
		return requestsTable.alreadyExists(url);
	}

	public boolean isInScope() {
		return isInScope;
	}
}
