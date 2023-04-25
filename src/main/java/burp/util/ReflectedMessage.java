package burp.util;

import java.util.List;

import burp.IHttpRequestResponse;
import burp.IParameter;

public class ReflectedMessage {

	private IHttpRequestResponse httpRequestResponse;
	private List<IParameter> parameters;

	public ReflectedMessage(IHttpRequestResponse httpRequestResponse,  List<IParameter> parameters) {
		this.httpRequestResponse = httpRequestResponse;
		this.parameters = parameters;
	}

	public IHttpRequestResponse getHttpRequestResponse() {
		return httpRequestResponse;
	}

	public List<IParameter> getParameters() {
		return parameters;
	}
	
}
