package burp;

import burp.userinterface.UInterface;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Joaquin R. Martinez
 */
public class BurpExtender implements IBurpExtender, IHttpListener {

    private IBurpExtenderCallbacks ibec;
    private UInterface uInterface;
    private IExtensionHelpers helpers;
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks ibec) {
        this.ibec = ibec;//guardar
        helpers = ibec.getHelpers();
        uInterface = new UInterface(ibec);
        ibec.registerHttpListener(this);
        /*agregar el nuevo tab a burp*/
        ibec.addSuiteTab(new ITab() {
            @Override
            public String getTabCaption() {
                return "Reflected Parameter";
            }
            @Override
            public Component getUiComponent() {
                return uInterface;
            }
        });
    }
    @Override
    public void processHttpMessage(int flag, boolean isRequest, IHttpRequestResponse message) {
        if (!isRequest && (IBurpExtenderCallbacks.TOOL_PROXY == flag 
                || IBurpExtenderCallbacks.TOOL_SPIDER == flag)
                && ibec.isInScope(message.getUrl())) {
            if (uInterface.alreadyExists(message)) {
                return;
            }
            LinkedList<IParameter> reflectedParams = new LinkedList<>();
            IRequestInfo info = helpers.analyzeRequest(message.getRequest());
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
    /**
     * Busca una cadena en bytes y devuelve un par (comienzo y fin de la
     * cadena). Si no encuentra nada retorna NULL
     */
    /*
    private int[] indexOf(byte[] data, byte[] search, int start, int end) {
        int start_ = helpers.indexOf(data, search, true, start, end);
        if (start_ != -1) {
            int end_ = start_ + search.length;
            return new int[]{start_, end_};
        }
        return null;
    }
*/
}
