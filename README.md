# OSGi Core

OSGi Core is used to setup a framework for running an Java application using OSGi. Currently supports:
- `ssh console`
- `connectiong to ActiveMQ for events`
- `using .env file for variables`
- `json to pojo conversion using annotations`
- `rest services via annotations`

### Build Commands
- `mvn clean verify`
- `mvn clean verify -P index-export`
- `mvn package bnd-indexer:index bnd-resolver:resolve -DskipTests -Pindex-export`
- `mvn package bnd-run:run@core-app -DskipTests`
- `mvn package bnd-run:run@core-app-debug -DskipTests`

### Env Variable
 - `` 
 
### OSGi Console
[General OSGi Console Information](https://enroute.osgi.org/FAQ/500-gogo.html)

#### SSH to Console
`ssh localhost -p 2222 -l <user_id>`

#### Common Commands
- `SCOPE='felix:*'` (set the scope for commands to default to felix)
- `help` (list all commands and help about them)
