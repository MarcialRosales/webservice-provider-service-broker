package com.servicebroker;

import java.util.ArrayList;
import java.util.List;

public class ServiceDefinitionProperties {
	String name;
	String description;
	List<String> plans = new ArrayList<>();
	List<String> tags = new ArrayList<>();
	CredentialProperties credentials;
	
	

	public ServiceDefinitionProperties() {
		super();
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ServiceDefinitionProperties(String id, String description) {
		super();
		this.name = id;
		this.description = description;
	}

	public List<String> getPlans() {
		return plans;
	}

	public void setPlans(List<String> plans) {
		this.plans = plans;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public CredentialProperties getCredentials() {
		return credentials;
	}

	public void setCredentials(CredentialProperties credentials) {
		this.credentials = credentials;
	}	
	
}