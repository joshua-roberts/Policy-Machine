# Policy Machine
The Policy Machine is a reference implementation of the Next Generation Access Control (NGAC) standard. 
Provided is a Java library to model and query NGAC graphs and a RESTful API for an administrative Policy 
Enforcement Point (PEP). The API can be used to create web-based NGAC aware applications.

# Full Documentation
View the Policy Machine documentation [here](https://joshua-roberts.github.io/Policy-Machine-Dev/)

# Getting Started

## Standalone Java Library
The REST API provides tools to create NGAC aware applications but requires a certain level of already developed infrastructure to really be useful.  If you are looking to just experiment with NGAC graphs and use cases, you can use the Policy Decision Point (PDP), Event Processing Point (EPP), and the Policy Access Point (PAP) packages to test different NGAC policy configurations in your own environments.

### Build Policy Machine JAR Library

1. In **pom.xml** change `<packaging>war</packaging>` to `<packaging>jar</packaging>`.
2. Run `mvn package` from the project root.
3. `pm.jar` will be created in /target/.  
4. Add `pm.jar` to your classpath.
