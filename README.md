# 'Web-Service Provider' Service Broker (Demonstration Project)

## Motivation
The main goal of this demo project is to show how we can build a *Service Broker* for *Cloud Foundry*. In this demonstration project, we are building a *Service Broker* that provides a set of web-service end-points along with the credentials required to access them.

Why do we need to build a *Service Broker* if we could simply assign some environment variables (`cf set-env myapp URL "http://someur.com"`) to our applications or create a user provided service (`cf cups myservice ....`)? Because *Service Brokers* provide a far more user-friendly mechanism to expose services to our applications. If use `cf set-env` we would have to configure each application with all the web-services they required. Similarly, if we used `cf cups` we would have to create one per each web-service and it will only be applicable to the space where we create the user provided service. Furthermore, the developer who creates the user provided service must know the url and credentials of the web-service.

In the contrary, *Service Broker* frees up developers from the burden of having to know the url(s) and credential(s) of all the web-services they need access to. Our simple *Service Broker* has hard-coded credentials but in rather real scenario, the *Service Broker* would have to dynamically obtain those credentials from somewhere else. The *Service Broker* frees our developers from those arduous tasks.
    
  
## What is a *Service Broker* then?  
Services are integrated with Cloud Foundry ([v1.9](https://docs.pivotal.io/pivotalcf/1-9/services/overview.html)) by implementing an API that we call Service Broker API. We have to distinguish the actual *Service* from the *Service Broker*. A *Service Broker* is nothing more than an application that exposes a *Service Broker API* which allows *Cloud Foundry* to provision/deprovision instances of that service in an automated fashion, i..e without human intervention. 

The *Service Broker* application can run anywhere, but in our case, it is a standard Spring Boot application deployed to our space in *Cloud Foundry*. Once we have our application deployed, we can register it with *Cloud Foundry* as a *Service Broker* and *Cloud Foundry* will automatically interact with our application using the so called *Service Broker API*. 

Once our application is deployed and registered as a *Service Broker* in *Cloud Foundry*, we can see it listed in the market place by invoking the following command: `cf marketplace`. 

The picture below shows on the left hand-side all the *cf* commands that deal with *Service Broker(s)* and the arrows in the middle are the interaction flows between *Cloud Foundry* and the *Service Broker*. In this picture, the *Service Broker* is **MySQL Broker** and the service is **MySQL**. 
 
![Cloud Foundry Service Broker Integration](http://docs.cloudfoundry.org/services/images/v2services-new.png)

## What does our *Service Broker* do? 
Imagine we are building a reseller e-commerce portal which sells products. The products we offer are obtained via an external web-service (**catalogService**) call. Likewise, to know the amount of products available (**availableService**) and their price (**pricingService**) we call another web-service. Our Service Broker gives us the uri and credentials to access to these 3 web-services. 

For brevity sake, we have decided to declare the web-services in a properties file (`application.yml`) but we could have chosen Redis or an RDBMS to store them. 

These are the services our service broker exposes right now:
```
catalog.services:
 - name: catalogService
   description: Service that returns a catalog of products
   plans: basic
   tags: webservice, rest
   credentials:
     uri: http://catalog.com
     authentication: bearer 343435353
     

 - name: availabilityService
   description: Service that returns availability of products in stock
   plans: default
   tags: webservice, soap
   credentials:
     uri: http://available.com
     authentication: bearer 343435353
     

 - name: pricingService
   description: Service that returns pricing of products 
   plans: default
   tags: webservice, protobuf
   credentials:
     uri: http://pricing.com
     authentication: bearer 343435353
``` 

In the next sections, we will show step by step how to create our *Service Broker*.            
	
## Create Service Broker compliant with Service Broker API [2.11](http://docs.cloudfoundry.org/services/api.html) (for v1.9 of PCF)

A Service Broker API handles 3 areas: advertise a catalog of services, provisioning of a service instance from the available services, and binding a service instance to an application. 

### Create skeleton of the Service Broker

Create a Spring Boot application with the following 2 dependencies: [spring-cloud-cloudfoundry-service-broker](https://github.com/spring-cloud/spring-cloud-cloudfoundry-service-broker) and spring-boot-starter-web dependency (remember that this is REST API):
```
<properties>
	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
	<java.version>1.8</java.version>
	<springCloudServiceBrokerVersion>1.0.0.RELEASE</springCloudServiceBrokerVersion>
</properties>

<dependencies>
	<!-- other dependencies >
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-web</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-cloudfoundry-service-broker</artifactId>
		<version>${springCloudServiceBrokerVersion}</version>
	</dependency>
</dependencies>
```

### Catalog Management
The first endpoint that a broker must implement is the service catalog. If the catalog fails to load or it returns invalid data, the *Cloud Controller* will not allow us to register the broker and will give a meaningful error message. 

If we run the spring boot as it is (`mvn spring-boot:run`), it will complain that it needs an instance of `org.springframework.cloud.servicebroker.model.Catalog`. We are going to use the `application.yml` file to declare the services and we are going to create a `@Bean` of type `org.springframework.cloud.servicebroker.model.Catalog`:
```
@SpringBootApplication
public class ServiceBrokerSampleApplication {

	@Bean
	Catalog catalog(CatalogProperties properties) {
		return new Catalog(properties.getServiceDefinitions());
	}
	...
}
```

If we run our application again it will fail because it needs an instance of `org.springframework.cloud.servicebroker.service.ServiceInstanceService`.

For now, we are going to provide a No-Op one so that we can verify the catalog Rest endpoint works.
```

@SpringBootApplication
public class ServiceBrokerSampleApplication {

    ...
	@Bean
	ServiceInstanceService serviceInstanceService() {
		return new NoOpServiceInstanceService();
	}
	...
```

```
class NoOpServiceInstanceService implements ServiceInstanceService {
...
```

Let's check out the catalog endpoint. The Service Broker API is protected by basic authentication. In our case, we are using `broker:broker` configured in the `application.yml`.

`curl -u broker:broker localhost:8080/v2/catalog | jq .` produces this output:
```
{
  "services": [
    {
      "id": "a0857b9a-cabd-4235-ab6d-324b38acbd9f",
      "name": "catalogService",
      "description": "Service that returns a catalog of products",
      "bindable": true,
      "plan_updateable": true,
      "plans": [
        {
          "id": "2c438cc8-10d8-412f-8cac-3fc738714c37",
          "name": "basic",
          "description": "basic",
          "metadata": {},
          "free": true
        }
      ],
      "tags": [
        "webservice",
        "rest"
      ],
      "metadata": {},
      "requires": [],
      "dashboard_client": null
    },
    {
      "id": "2ac2448a-b957-4c10-91ec-269f3a413505",
      "name": "availabilityService",
      "description": "Service that returns availability of products in stock",
      "bindable": true,
      "plan_updateable": true,
      "plans": [
        {
          "id": "12419180-5421-4e3b-901c-295078adcd77",
          "name": "default",
          "description": "default",
          "metadata": {},
          "free": true
        }
      ],
      "tags": [
        "webservice",
        "soap"
      ],
      "metadata": {},
      "requires": [],
      "dashboard_client": null
    },
    {
      "id": "10be10a3-aa00-441c-bb7e-53baba0e964f",
      "name": "pricingService",
      "description": "Service that returns pricing of products",
      "bindable": true,
      "plan_updateable": true,
      "plans": [
        {
          "id": "a4c9bb9c-4e0b-43fd-9f87-5d89fe9d6114",
          "name": "default",
          "description": "default",
          "metadata": {},
          "free": true
        }
      ],
      "tags": [
        "webservice",
        "protobuf"
      ],
      "metadata": {},
      "requires": [],
      "dashboard_client": null
    }
  ]
}
```

 
### Adding our Broker to Cloud Foundry

We are not going to fully complete our broker before we register it with *Cloud Foundry*. We are going to build the application with `mvn install` and push it with `cf push -f target/manifest.yml`. Our broker is now listening at `broker-service.cfapps.iobroker-service.cfapps.io`.

We create the service broker scoped to our space executing this command `cf create-service-broker mybroker broker broker http://broker-service.cfapps.io --space-scoped`. We can list our service broker:
```
cf service-brokers
Getting service brokers as mrosales@pivotal.io...

name       url
mybroker   http://broker-service.cfapps.io
``` 

And ultimatley, we can list our services via the `cf marketplace` command:

``` 
service               plans                                     description
...
availabilityService   default                                   Service that returns availability of products in stock
catalogService   	  basic                                     Service that returns a catalog of products
pricingService        default                                   Service that returns pricing of products
...

```

**Note**: *The service ID and plan IDs of each service advertised by the broker must be unique across Cloud Foundry. GUIDs are recommended for these fields*. Check out the code to see that we are not using `UUID.randomUUID().toString()`.
 
If your `/v2/catalog` end-point is not fully compliant, the registration will fail. Here is a list of [catalog validation rules](http://docs.cloudfoundry.org/services/managing-service-brokers.html#catalog-validation).

  
### Creating a service instance 
  
Once we can see our services listed in the market place we can proceed with the next step. We need to implement a class that implements the interface `org.springframework.cloud.servicebroker.service.ServiceInstanceService`.

```
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
   ....
}   
```
And we also modify the SpringBoot configuration to create an instance of this class instead of the NoOp we did in the previous section.
```
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
	....
```

Once we have the code ready, we only need to push the new broker to *Cloud Foundry* and we delete the previously registered broker and re-create it again.

```
mvn install
cf push -f target/manifest.yml
cf delete-service-broker mybroker
cf create-service-broker mybroker broker broker http://broker-service.cfapps.io --space-scoped
```
And finally we create an instance of our `catalogService`.
```
cf create-service catalogService basic catalogService
```

And `cf services` will list our service instance:
```
cf services
Getting services in org pivotal-emea-cso / space mrosales as mrosales@pivotal.io...
OK

name             service         plan    bound apps                                   last operation
catalogService   catalogService  basic                                                create succeeded
```
 
We can actually check the details of the service instance `catalogService`.
```
cf service catalogService

Service instance: catalogService
Service: catalogService
Bound apps:
Tags:
Plan: basic
Description: Service that returns a catalog of producgts
Documentation url:
Dashboard:

Last Operation
Status: create succeeded
Message:
Started: 2017-01-10T13:35:49Z
Updated: 2017-01-10T13:35:49Z
```

### Binding our service instance to an application

We saw earlier that `catalogService` does not show any credentials and/or urls. This is because the credentials are assigned when we bind the application to the service instance. We need to implement another interface `ServiceInstanceBindingService` which will bind a service instance to an application and provide the credentials. 

Our class is very simple as it will return the credentials we configured in the `application.yml` for each service.
```
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

``` 
As we have done with the previous classes, we need to instantiate it:
```
@SpringBootApplication
public class ServiceBrokerSampleApplication {

	...
	@Bean
	StaticServiceInstanceBindingService serviceInstanceBindingService(CatalogProperties properties) {
		return new StaticServiceInstanceBindingService(properties);
	}
	...

```

And finally, we bind the service instance to an application. 
```
cf bind-service catalogService myApp 
```
And via the `cf env myApp` we can check out the injected credentials.
```
System-Provided:
{
 "VCAP_SERVICES": {
  "catalogService": [
   {
    "credentials": {
     "authentication": "bearer 343435353",
     "uri": "http://catalog.com"
    },
    "label": "catalogService",
    "name": "catalogService",
    "plan": "basic",
    "provider": null,
    "syslog_drain_url": null,
    "tags": [
     "webservice",
     "rest"
    ],
    "volume_mounts": []
   }
  ],
  ....
```

We can see that the credentails are there and also the tags `webservice`, and `rest`.
 