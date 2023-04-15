package burp;

import java.util.LinkedList;
import java.util.List;
import burp.tab.Tab;

/**
 * @author Joaquin R. Martinez
 */
public class BurpExtender implements IBurpExtender, IHttpListener {

	private IBurpExtenderCallbacks ibec;
	private Tab uInterface;
	private IExtensionHelpers helpers;

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks ibec) {
		this.ibec = ibec;
		this.uInterface = new Tab(ibec);
		helpers = ibec.getHelpers();
		ibec.registerHttpListener(this);
		ibec.addSuiteTab(this.uInterface);
	}

	@Override
	public void processHttpMessage(int flag, boolean isRequest, IHttpRequestResponse message) {
		if (isRequest) {
			return; // not interested in requests
		}

		// validate origin of event
		if (IBurpExtenderCallbacks.TOOL_PROXY != flag && IBurpExtenderCallbacks.TOOL_SPIDER != flag) {
			return;
		}

		IRequestInfo info = helpers.analyzeRequest(message);
		if (!ibec.isInScope(info.getUrl())) {
			return; // not process if not in scope
		}

		if (uInterface.alreadyExists(info.getUrl().toString())) {
			return;
		}

		List<IParameter> reflectedParams = new LinkedList<>();
		List<IParameter> parameters = info.getParameters();
		byte[] response = message.getResponse();
		for (IParameter param : parameters) {
			if (param.getValue().length() > 4) {
				int indexOf = helpers.indexOf(response, helpers.stringToBytes(param.getValue()), true, 0,
						response.length - 1);
				if (indexOf != -1) {
					reflectedParams.add(param);
				}
				// test urldecoded too
				indexOf = helpers.indexOf(response, helpers.stringToBytes(helpers.urlDecode(param.getValue())), true, 0,
						response.length - 1);
				if (indexOf != -1 && reflectedParams.indexOf(param) == -1) {
					reflectedParams.add(param);
				}
			}
		}
		if (!reflectedParams.isEmpty()) {
			uInterface.sendToRequestsTable(message, reflectedParams);
		}
	}

}
