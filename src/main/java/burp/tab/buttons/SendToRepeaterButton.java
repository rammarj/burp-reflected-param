package burp.tab.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;

public class SendToRepeaterButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTable requestsTable;
	private List<IHttpRequestResponse> requestsList;
	private IBurpExtenderCallbacks callbacks;

	public SendToRepeaterButton(JTable requestsTable, List<IHttpRequestResponse> requestsList,
			IBurpExtenderCallbacks callbacks) {
		super("Send to repeater");
		this.requestsTable = requestsTable;
		this.requestsList = requestsList;
		this.callbacks = callbacks;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int selected = requestsTable.getSelectedRow();
		if (selected != -1) {
			IHttpRequestResponse msgHTTP = requestsList.get(selected);
			IRequestInfo request = this.callbacks.getHelpers().analyzeRequest(msgHTTP);
			URL url = request.getUrl();
			this.callbacks.sendToRepeater(url.getHost(), url.getPort(), url.getProtocol().equalsIgnoreCase("https"),
					msgHTTP.getRequest(), null);
		}
	}

}
