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
        //instanciar la interfaz
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
    public void processHttpMessage(int arg0, boolean arg1, IHttpRequestResponse arg2) {
        if (!arg1 && (IBurpExtenderCallbacks.TOOL_PROXY == arg0 
                || IBurpExtenderCallbacks.TOOL_SPIDER == arg0)
                && ibec.isInScope(arg2.getUrl())) {
            LinkedList<IParameter> lista = new LinkedList<>();
            IRequestInfo info = helpers.analyzeRequest(arg2.getRequest());
            byte[] response = arg2.getResponse();
            //byte[] request = arg2.getRequest();
            List<IParameter> parameters = info.getParameters();
            for (IParameter param : parameters) {
                if (param.getValue().length() > 4) {
                    int indexOf = helpers.indexOf(response, helpers.stringToBytes(param.getValue())
                            ,true, 0, response.length - 1);
                    if (indexOf != -1) {
                        lista.add(param);
                    }
                    //test urldecoded too
                    indexOf = helpers.indexOf(response, helpers.stringToBytes(
                            helpers.urlDecode(param.getValue())),true, 0, response.length - 1);
                    if (indexOf != -1 && lista.indexOf(param)==-1) {
                        lista.add(param);
                    }
                }
            }
            if (lista.size() > 0) {
                uInterface.sendToRequestsTable(arg2, lista);
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
