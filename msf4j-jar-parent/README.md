# WSO2 Microservices Server - Parent POM for Microservices

This parent POM file makes life easy for developers who write microservices using WSO2 MSS. The following example
from the [stockquote-mss-deployable-jar](../samples/stockquote-mss-deployable-jar) microservice example, demonstrates how to quickly write a POM for your microservice using the
msf4j-jar-parent POM.

```xml
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.wso2.msf4j</groupId>
        <artifactId>msf4j-jar-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../msf4j-jar-parent/pom.xml</relativePath>
    </parent>

    <groupId>org.wso2.msf4j.example</groupId>
    <artifactId>stockquote-mss-deployable-jar</artifactId>
    <packaging>jar</packaging>

    <name>StockQuote MSS Deployable Jar</name>

    <properties>
        <microservice.resourceClasses>org.wso2.msf4j.example.StockQuoteService</microservice.resourceClasses>
    </properties>
    
</project>
```

The microservice.resourceClasses Maven property should contain the comma separated list of fully qualified resource class names.
