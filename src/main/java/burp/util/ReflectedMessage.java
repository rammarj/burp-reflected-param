package burp.util;

import java.util.List;

import burp.IHttpRequestResponse;
import burp.IParameter;

public class ReflectedMessage {

	private IHttpRequestResponse iHttpRequestResponse;
	private List<IParameter> pwm;

	public ReflectedMessage(IHttpRequestResponse iHttpRequestResponse,  List<IParameter> pwm) {
		this.iHttpRequestResponse = iHttpRequestResponse;
		this.pwm = pwm;
	}

	public IHttpRequestResponse getiHttpRequestResponse() {
		return iHttpRequestResponse;
	}

	public List<IParameter> getPwm() {
		return pwm;
	}
	
}
