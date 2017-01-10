package com.servicebroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("catalog")
public class CatalogProperties {

	List<ServiceDefinitionProperties> services = new ArrayList<>();
	Map<String, ServiceDefinitionProperties> servicesIndexedByServiceName = new HashMap<>();
	
	public List<ServiceDefinitionProperties> getServices() {
		return services;
	}

	public void setServices(List<ServiceDefinitionProperties> services) {
		this.services = services;
		
	}
	@PostConstruct
	void initializeServicesIndexedByName() {
		servicesIndexedByServiceName = services.stream().collect(Collectors.toMap(ServiceDefinitionProperties::getName, sdp -> sdp));
	}

	public List<ServiceDefinition> getServiceDefinitions() {
		return services.stream().map(sdp -> new ServiceDefinition(newId(), sdp.name, sdp.description, true, true,
				buildPlans(sdp.plans), sdp.tags, null, null, null)).collect(Collectors.toList());
	}

	private List<Plan> buildPlans(List<String> plans) {
		return plans.stream().map(m -> new Plan(newId(), m, m, null, true)).collect(Collectors.toList());
	}

	public Map<String, Object> buildCredentials(String serviceName) {
		ServiceDefinitionProperties sdp = servicesIndexedByServiceName.get(serviceName);
		if (sdp == null) {
			throw new NoSuchElementException(serviceName);
		}
		return sdp.credentials.put(new HashMap<>());
	}
	
	/**
	 * The service ID and plan IDs of each service advertised by the broker must
	 * be unique across Cloud Foundry. GUIDs are recommended for these fields
	 * 
	 * @return
	 */
	private String newId() {
		return UUID.randomUUID().toString();
	}
}
