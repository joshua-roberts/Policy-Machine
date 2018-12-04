# Policy Machine Web Services
NGAC reference implementation using web services.

### Deployment
#### Docker
*Note:* Neo4j only
To run the Policy Machine in a Docker container using Docker Compose, using the Docker Quickstart Terminal:
1. Download and install Docker Toolbox:
    - For mac: https://docs.docker.com/toolbox/toolbox_install_mac/
    - For windows: https://docs.docker.com/toolbox/toolbox_install_windows/
2. Run the Docker Quickstart Terminal
3. Run 'mvn clean package install' from the project root.  This will create the pm.war file in the 'target' folder.
4. In the docker terminal, navigate to the project root and run 'docker-compose up'.  This will deploy the pm.war file on a tomcat server, and start a neo4j instance.

#### Tomcat
1. Make sure Tomcat is installed.
2. run `$CATALINA_HOME\bin\startup`
3. add the following lines to $CATALINA_HOME\conf\tomcat-users.xml
    ```xml
    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <user username="admin" password="password" roles="manager-gui, manager-script"/>
    ```
4. Add a server to maven in ${MAVEN_HOME}/conf/settings.xml
    ```xml
    <server>
     <id>TomcatServer</id>
     <username>admin</username>
     <password>password</password>
    </server>
    ```
4. This block in the pom.xml file tells tomcat where to deploy the war
    ```xml
    <plugin>
        <groupId>org.apache.tomcat.maven</groupId>
        <artifactId>tomcat7-maven-plugin</artifactId>
        <version>2.2</version>
        <configuration>
            <url>http://localhost:8080/manager/text</url>
            <server>TomcatServer</server>
            <path>/pm</path>
            <username>admin</username>
            <password>password</password>
        </configuration>
    </plugin>
    ```
 5. go to the project's root directory, and run `mvn install`
 6. run `mvn tomcat7:deploy`
 7. The application will be available at `localhost:8080/pm`
 8. To access the APIs the url is `localhost:8080/pm/api`.

### Policy Machine Configuration
Once the server is running, navigate to `../pm/config.jsp` to configure the PM.
#### Connecting to a database
- Neo4j: Bolt protocol is not yet supported
- MySQL: The schema script which can be found [here](../sql/pmsql.sql). 
#### Configurations
1.	Load a configuration.  Configuration files are in a JSON format and have the extension .pm
    a.	Here is the format expected
    b.	Here is a few examples
2.	Save a configuration
    a.	save the current state of the policy machine, can then be loaded

### Super User
When developing this reference implementation, two questions arose: Who can create Policy Classes? And who can assign to Policy Classes? Since these questions are not addressed in the NGAC standard, we decided to implement a super user who by default as the ability to carry out these actions.  Our approach, explained below, is specific to this implementation, and is not required in all implementations.

There are 7 nodes that make up the super user configuration. The super User is assigned to 2 User Attributes super_ua1 and super_ua2. These two attributes are assigned to a Policy Class also called super.  Super_ua1 is associated with an Object Attribute, super_oa1, which is also assigned to the Policy Class super, with * permissions.  This gives any user in super_ua1 all permissions on objects in super_oa1. There is one Object called super assigned to super_oa1. Super_ua2 is associated with super_ua1 with * permissions.  This allows the super user to have all permissions on itself. 

This approach gives us one answer to the questions above. When creating a Policy Class we check if the requesting user has the permission to create a Policy Class on the super Object.  Similarly, when any user attempts to assign an Object or User Attribute to a Policy Class, we first check that the user has Permissions do so on the super Object.  The super Object can be viewed as a represesntative of the Policy Classes.  This is because the NGAC standard does not allows permissions on Policy Classes. 

This idea can be expanded upon to allow objects to represent entire sub graphs. However, for now, this implementation supports just one super Object and a super User.

### API Requests
The exposed web services also act as a Policy Enforcement Point (PEP).  When a request comes in the PEP forwards the request to the PDP, which ensures the requesting user is allowed to carry out the action. This means the PEP always needs to know which user is sending a request.  This is tracked using session IDs that represent users who have authenticated successfully with the server.  Every endpoint requires this session ID to determine the user.  The only API that does not require a session ID is the Sessions API.  This API is used to authenticate users and provides the session IDs that will be used in subsequent requests to the Policy Machine.

For example, to sign in as the super user:
```
POST /pm/api/sessions
Content-Type:application/json
{
  "username": "super",
  "password": "super"
}
```
The response from the server will look like:
```json
{
    "code": 9000,
    "message": "success",
    "entity": "NEW_SESSION_ID"
}
```
Now the user can pass this session ID to the server for any other calls to the API.

### API Responses
A response from the Policy Machine will always return an HTTP status code of 200, with more details provided in the body of the response.  


### Technology Used
- JAVA 8
- JAX-RS
- Neo4j
- MySQL
- Tomcat
- Docker
