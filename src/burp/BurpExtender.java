/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        if (IBurpExtenderCallbacks.TOOL_PROXY == arg0 && arg2.getHttpService().getHost().equals(this.uInterface.getHost())) {
            if (arg1 == true) {
                if (IBurpExtenderCallbacks.TOOL_PROXY == arg0) {
                    IRequestInfo req = helpers.analyzeRequest(arg2);
                    List<String> headers = req.getHeaders();
                    for (int i = 0; i < headers.size(); i++) {
                        String get = headers.get(i);
                        if (get.indexOf("Host") != -1) {
                            get = "Host: "+uInterface.miServer()+"/burp-header-injector-HOST"; //inject this string 
                            headers.remove(i);
                            headers.add(i, get);
                        }else if (get.indexOf("Accept-Language") != -1) {
                            get = "Accept-Language: burp-header-injector-ACCEPT"; //inject this string 
                            headers.remove(i);
                            headers.add(i, get);
                        }else if (get.indexOf("User-Agent") != -1) {                            
                            get = "User-Agent: burp-header-injector-UA"; //inject this string 
                            headers.remove(i);
                            headers.add(i, get);
                        }else if (get.indexOf("Referer") != -1) {                            
                            get += ".burp-header-injector-REFERER"; //inject this string 
                            headers.remove(i);
                            headers.add(i, get);
                        }else if (get.indexOf("Origin") != -1) {                            
                            get = "Origin: "+uInterface.miServer()+".burp-header-injector-ORIGIN"; //inject this string 
                            headers.remove(i);
                            headers.add(i, get);
                        }
                    }
                    headers.add("X-Forwarded-For: "+uInterface.miServer()+".burp-header-injector-XFF");
                    headers.add("X-Forwarded-Host: "+uInterface.miServer()+".burp-header-injector-XFH");
                    headers.add("X-Forwarded-Proto: "+uInterface.miServer()+".burp-header-injector-XFP");
            //he
                    //headers.add("X-Forwarded-For: burp-header-injector-HOST");
                    byte[] new_msg = helpers.buildHttpMessage(headers,
                            helpers.stringToBytes(helpers.bytesToString(arg2.getRequest()).substring(req.getBodyOffset())));
                    //original.setRequest(new_msg);
                    IHttpRequestResponse nuevo_response = ibec.makeHttpRequest(arg2.getHttpService(), new_msg);
                    byte[] response = nuevo_response.getResponse();
                    if (helpers.indexOf(response, "burp-header-injector-".getBytes(), true, 0, response.length - 1) != -1) {
                        URL url = req.getUrl();
                        ibec.sendToRepeater(url.getHost(), url.getPort(), true, new_msg, "H.I.");
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
