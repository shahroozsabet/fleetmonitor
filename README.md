# Fleetmonitor Integration Cloud Native application
Steps for running all services:  

1.  start integration-service-discovery
2.	start integration-gateway
3.	start integration-service

For Simple Functional Testing, You can run integration-service separately too. Just comment those lines related to Discovery service.
Then Give it a port like 9085 instead of 0 otherwise its port would be a random port.

# Index.html
These is an Index.html in the root of project which is written in angular just point the endpoint IP address to where the service is deployed.
var endpoint = "http://localhost:9085/convertFile";

# Gateway
If the project is built on Netflix cloud native stack then all the service endpoints should pass through Zuul gateway routs.
var endpoint = "http://localhost:8082/command/convertFile";

# Kubernetes
There is k8s deployment file in Kubernetes folder of integration-service, and also there are maven and spring profiles to build the project for k8s.
