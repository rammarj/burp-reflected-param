package burp;

import java.util.List;
import burp.tab.Tab;

/**
 * @author Joaquin R. Martinez
 */
public class BurpExtender implements IBurpExtender, IHttpListener {

	private IBurpExtenderCallbacks ibec;
	private Tab reflectedParametersTab;
	private IExtensionHelpers helpers;

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks ibec) {
		this.ibec = ibec;
		this.reflectedParametersTab = new Tab(ibec);
		this.helpers = ibec.getHelpers();
		this.ibec.registerHttpListener(this);
		this.ibec.addSuiteTab(this.reflectedParametersTab);
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
		// validate if in scope check is selected and if domain is in scope
		if (this.reflectedParametersTab.isInScope() && !ibec.isInScope(info.getUrl())) {
			return;
		}

		if (this.reflectedParametersTab.alreadyExists(info.getUrl().toString())) {
			return;
		}

		List<IParameter> parameters = info.getParameters();
		byte[] response = message.getResponse();

		List<IParameter> reflectedParams = parameters.stream()
				.filter(e -> e.getValue().length() > 4 && validateIfReflectedParameter(response, e))
				.toList();
		if (!reflectedParams.isEmpty()) {
			this.reflectedParametersTab.sendToRequestsTable(message, reflectedParams);
		}
	}

	private boolean validateIfReflectedParameter(byte[] response, IParameter param) {
		int indexOf = helpers.indexOf(response, helpers.stringToBytes(helpers.urlDecode(param.getValue())), true, 0,
				response.length - 1);
		return indexOf != -1;
	}

}
