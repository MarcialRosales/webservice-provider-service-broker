package com.servicebroker;

import java.util.Map;

public class CredentialProperties {

	private String uri;
	private String authentication;
	
	public CredentialProperties() {
		super();
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getAuthentication() {
		return authentication;
	}
	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}
	
	public Map<String, Object> put(Map<String, Object> map) {
		map.put("uri", uri);
		map.put("authentication", authentication);
		return map;
		
	}
	
}
