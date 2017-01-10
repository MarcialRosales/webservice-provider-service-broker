package com.servicebroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ServiceBrokerSampleApplication {

	
	@Bean
	Catalog catalog(CatalogProperties properties) {
		return new Catalog(properties.getServiceDefinitions());
	}
	@Bean
	ServiceInstanceService serviceInstanceService() {
		return new StaticServiceInstanceService();
	}
	@Bean
	StaticServiceInstanceBindingService serviceInstanceBindingService(CatalogProperties properties) {
		return new StaticServiceInstanceBindingService(properties);
	}
	public static void main(String[] args) {
		SpringApplication.run(ServiceBrokerSampleApplication.class, args);
	}
}

class StaticServiceInstanceService implements ServiceInstanceService {

	private static final Logger log = LoggerFactory.getLogger(StaticServiceInstanceService.class);

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		String orgGuid = request.getOrganizationGuid();
		String spaceGuid = request.getSpaceGuid();
		String planId = request.getPlanId();
		String serviceDefinitionId = request.getServiceDefinitionId();
		
		log.info(String.format("CreateServiceInstance for %s, %s, %s, %s", orgGuid, spaceGuid, planId, serviceDefinitionId));
		
		return new CreateServiceInstanceResponse();
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		
		return new DeleteServiceInstanceResponse();
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		
		return null;
	}
	
}
class StaticServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger log = LoggerFactory.getLogger(StaticServiceInstanceBindingService.class);

	CatalogProperties catalogProperties;
	
	public StaticServiceInstanceBindingService(CatalogProperties catalogProperties) {
		super();
		this.catalogProperties = catalogProperties;
	}

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(
			CreateServiceInstanceBindingRequest request) {
		
		CreateServiceInstanceAppBindingResponse response = new CreateServiceInstanceAppBindingResponse();
		response.withCredentials(catalogProperties.buildCredentials(request.getServiceDefinition().getName()));
		
		log.info(String.format("createServiceInstanceBinding for %s with credentials %s", request.getServiceDefinition().getName(), response.getCredentials()));
		
		return response;
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		// TODO Auto-generated method stub
		
	}
	
}

class NoOpServiceInstanceService implements ServiceInstanceService {

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
}