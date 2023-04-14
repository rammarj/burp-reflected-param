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
        IRequestInfo info = helpers.analyzeRequest(message.getRequest());
        if (!isRequest && (IBurpExtenderCallbacks.TOOL_PROXY == flag 
                || IBurpExtenderCallbacks.TOOL_SPIDER == flag)
                && ibec.isInScope(info.getUrl())) {
            if (uInterface.alreadyExists(message)) {
                return;
            }
            LinkedList<IParameter> reflectedParams = new LinkedList<>();
            byte[] response = message.getResponse();
            //byte[] request = message.getRequest();
            List<IParameter> parameters = info.getParameters();
            for (IParameter param : parameters) {
                if (param.getValue().length() > 4) {
                    int indexOf = helpers.indexOf(response, helpers.stringToBytes(param.getValue())
                            ,true, 0, response.length - 1);
                    if (indexOf != -1) {
                        reflectedParams.add(param);
                    }
                    //test urldecoded too
                    indexOf = helpers.indexOf(response, helpers.stringToBytes(
                            helpers.urlDecode(param.getValue())),true, 0, response.length - 1);
                    if (indexOf != -1 && reflectedParams.indexOf(param)==-1) {
                        reflectedParams.add(param);
                    }
                }
            }
            if (reflectedParams.size() > 0) {
                uInterface.sendToRequestsTable(message, reflectedParams);
            }
        }
    }
  
}
