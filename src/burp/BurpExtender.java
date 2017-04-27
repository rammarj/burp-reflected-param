
package burp;

import burp.userinterface.Tab;
import burp.userinterface.UInterface;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Joaquin R. Martinez
 */
public class BurpExtender implements IBurpExtender, IHttpListener {

    public static IBurpExtenderCallbacks ibec;
    private UInterface uInterface;
    private IExtensionHelpers helpers;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks ibec) {
        BurpExtender.ibec = ibec;//guardar
        helpers = ibec.getHelpers();
        uInterface = new UInterface(ibec);
        ibec.registerHttpListener(this);
        /*agregar el nuevo tab a burp*/
        //instanciar la interfaz
        ibec.addSuiteTab(new Tab("Reflected Parameter", uInterface));
    }

    @Override
    public void processHttpMessage(int arg0, boolean arg1, IHttpRequestResponse arg2) {
        if ((IBurpExtenderCallbacks.TOOL_PROXY == arg0 || IBurpExtenderCallbacks.TOOL_SPIDER == arg0) &&                 
                ibec.isInScope(arg2.getUrl())) {
            if (arg1 == true) {
                if (IBurpExtenderCallbacks.TOOL_PROXY == arg0) {
                    IRequestInfo requestInfo = helpers.analyzeRequest(arg2);
                    List<String> headers = requestInfo.getHeaders();
                    
                    byte[] newMessage = helpers.buildHttpMessage(headers,
                            helpers.stringToBytes(helpers.bytesToString(arg2.getRequest()).substring(requestInfo.getBodyOffset())));
                    //original.setRequest(newMessage);
                    IHttpRequestResponse newResponse = ibec.makeHttpRequest(arg2.getHttpService(), newMessage);
                    byte[] response = newResponse.getResponse();
                    if (helpers.indexOf(response, "burp-header-injector-".getBytes(), true, 0, response.length - 1) != -1) {
                        URL url = requestInfo.getUrl();
                        ibec.sendToRepeater(url.getHost(), url.getPort(), true, newMessage, "H.I.");
                    }
                }
            } else {
                LinkedList<IParameter> lista = new LinkedList<>();
                IRequestInfo info = helpers.analyzeRequest(arg2.getRequest());
                byte[] response = arg2.getResponse();
                //byte[] request = arg2.getRequest();
                List<IParameter> parameters = info.getParameters();
                for (IParameter param : parameters) {
                    if (param.getValue().length() > 4) {
                        int[] indexOf = indexOf(response, helpers.stringToBytes(param.getValue()), 0, response.length - 1);
                        if (indexOf != null) {
                            lista.add(param);
                        }
                    }
                }
                if (lista.size() > 0) {
                    uInterface.sendToRequestsTable(arg2, lista);
                }
            }
        }
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

}
