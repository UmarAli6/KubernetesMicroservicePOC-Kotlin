# KubernetesMicroservicePOC-Kotlin
Project in collaboration with Gisecke+Devrient (G&D). The aim of the project was to make a proof of concept for adapting Kubernetes as a container orchestrating platform. The existing architecture Docker Swarm was to be replaced with Kubernetes and the database was to be moved externally. The project team created microservices in Ktor framework with an SQL database and used Docker Containers for the dockerization as Docker images. The Docker images were then deployed in Kubernetes Cluster. The microservices were lastly distributed in the form of Helm Charts. Development was done in CentOS and used Oracle DB as the database. Weekly sprints using Scrum framework, JIRA for planning and tracking and Git for version control.

## How G&D shall reuse the outcome of KTH project
G&D shall get the Docker image produced by KTH
G&D shall receive the Kubernetes deployment and Service definition files
G&D shall receive the Database connectivity model used by KTH
G&D shall receive the packaging structure created by KTH (In the form of helm charts) , so that it can be deployed in G&D 's Kubernetes cluster

### Old Architecture
![Old](https://user-images.githubusercontent.com/62612527/156083468-2635d730-9a14-46b0-9b6d-cfada8ab06ce.png)
### New Architecture
![New](https://user-images.githubusercontent.com/62612527/156083355-bf6e3071-2d30-4d46-b83b-6d041b35064e.png)
