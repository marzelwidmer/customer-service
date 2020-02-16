
# Skaffold Run Pipeline on OKD
## Login Internal Docker Registry
```bash
docker login -u developer -p `oc whoami -t` registry.apps.c3smonkey.ch

WARNING! Using --password via the CLI is insecure. Use --password-stdin.
Login Succeeded
```

# Kustomize 
```bash
kustomize build k8s/overlays/dev > k8s-deployment.yaml
```

## Run Skaffold Pipeline
```bash
skaffold run -p monkey

Generating tags...
 - registry.apps.c3smonkey.ch/dev/customer-service -> registry.apps.c3smonkey.ch/dev/customer-service:latest
Checking cache...
 - registry.apps.c3smonkey.ch/dev/customer-service: Found. Pushing
The push refers to repository [registry.apps.c3smonkey.ch/dev/customer-service]
f730a95aa030: Pushed
2a474dc063a9: Pushed
567cd6e70f55: Pushed
3ed672ad8207: Pushed
6189abe095d5: Pushed
f47163e8de57: Pushed
0d1435bd79e4: Pushed
latest: digest: sha256:24e73fe1263cd9222cb4b3d9c17cd4e8fe47f4b710d1478bd8ab50d16dc5cee2 size: 1788
Tags used in deployment:
 - registry.apps.c3smonkey.ch/dev/customer-service -> registry.apps.c3smonkey.ch/dev/customer-service:latest@sha256:24e73fe1263cd9222cb4b3d9c17cd4e8fe47f4b710d1478bd8ab50d16dc5cee2
Starting deploy...
 - configmap/customer-service-configmap-th4dkd9k8h configured
 - service/customer-service configured
 - deployment.apps/customer-service configured
You can also run [skaffold run --tail] to get the logs
```

See also : 

https://registry-console-default.apps.c3smonkey.ch/registry#/images/dev/customer-service 
http://customer-service-dev.apps.c3smonkey.ch/swagger-ui.html














# Docker Build

```
mvn clean install jib:dockerBuild
```
```
docker run --name customer-service -p 8080:8080 -d marzelwidmer/customer-service:latest
```

# API 
``` 
http :8080/api/v1/scientists/random
```

# Jager

[Jeager UI ](http://localhost:16686/search)

## Initial run
```
docker run -d --name jaeger \                                                                                                                                
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.8
```

## Stop Jaeger
```
docker stop jaeger 
```
## Start Jaeger
```
docker start jaeger 
```

## Hit the Service
``` 
for x in (seq 20); http ":8080/api/v1/scientists/random"; end
```


# Kubernetes ConfigMap Support

## Add Maven `spring-cloud-dependencies` dependency management  
```xml
<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

## Add `spring-cloud.version` properties.
```xml
<properties>
		...
		<spring-cloud.version>Greenwich.SR3</spring-cloud.version>
	</properties>
```

## Add Maven dependency `spring-cloud-starter-kubernetes-config` 
```xml
		<!-- Kubernetes -->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-kubernetes-config</artifactId>
		</dependency>
```

## Update RBAC policy
```bash
$ oc policy add-role-to-user view system:serviceaccount:development:default
```

To avoid the following exception.
```bash
.fabric8.kubernetes.client.KubernetesClientException: 
Failure executing: GET at: https://172.30.0.1/api/v1/namespaces/development/pods/customer-service-35-wj25f. 
    Message: Forbidden!Configured service account doesn't have access. 
    Service account may have been revoked. pods "order-service-35-wj25f" is 
        forbidden: User "system:serviceaccount:development:default" cannot get pods in the namespace "development": no RBAC policy matched.
```


# ConfigMap
Apply `ConfigMap`
```bash
$ oc apply -f deployments/configmap.yaml
```

## Additional ConfigMap Commands
Create `ConfigMap` from file.
```bash
$ oc create configmap customer-service --from-file=src/main/resources/application.yaml
```

Get all `ConfigMaps`
```bash
$ oc get configmaps
```

Get `ConfigMap` as `yaml`
```bash
$ oc get configmap customer-service -o yaml
```

Describe `ConfigMap`
```bash
$ oc describe configmap customer-service
```

Delete `ConfigMap`
```bash
$ oc delete configmap customer-service
```

# Watch running POD
```bash
$ watch oc get pods --field-selector=status.phase=Running                                                                         28.6m  Thu Sep 19 16:14:40 2019
```

# Tail logfile
```bash
$ oc logs -f customer-service-37-hh2tb
```
