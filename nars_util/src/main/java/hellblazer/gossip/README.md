Chinese-Whispers
================

A generic gossip based state replication and failure detection service.  The network transport is UDP unicast.

This project is licensed under the [Apache license, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Read the [project wiki](https://github.com/Hellblazer/Chinese-Whispers/wiki) for more information on design, configuration and usage.

This project requires Maven version 3.x to build.

To build this project, cd to the root directory and do:

    mvn clean install

See the [project wiki](https://github.com/Hellblazer/Chinese-Whispers/wiki) for design and usage.

### Maven configuration

For releases, include the hellblazer release repository:

    <repository>
        <id>hellblazer-release</id>
        <url>https://repository-hal900000.forge.cloudbees.com/release/</url>
    </repository>
    
add as dependency:

    <dependency>
        <groupId>com.hellblazer</groupId>
        <artifactId>chinese-whispers</artifactId>
        <version>1.0.0</version>
    </dependency>

For snapshots, include the hellblazer snapshot repository:

    <repository>
        <id>hellblazer-snapshots</id>
        <url>https://repository-hal900000.forge.cloudbees.com/snapshot/</url>
    </repository>
    
add as dependency:

    <dependency>
        <groupId>com.hellblazer</groupId>
        <artifactId>chinese-whispers</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>


<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.hellblazer</groupId>
	<artifactId>chinese-whispers</artifactId>
	<version>1.0.3-SNAPSHOT</version>
	<name>Chinese Whispers</name>
	<description>A UDP based state replication service</description>

	<licenses>
		<license>
			<name>Apache License, Version 2.0</name>
			<url>http://www.apache.org/licenses/LICENSE-2.0</url>
		</license>
	</licenses>

	<developers>
		<developer>
			<name>Hal Hildebrand</name>
			<email>hal.hildebrand@me.com</email>
			<organization>Chiral Behaviors</organization>
		</developer>
	</developers>

	<scm>
		<connection>git://github.com/ChiralBehaviors/Chinese-Whispers.git</connection>
		<url>https://github.com/ChiralBehaviors/Chinese-Whispers</url>
	</scm>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.resources.sourceEncoding>UTF-8</project.resources.sourceEncoding>
	</properties>

    <repositories>
        <repository>
            <id>ChiralBehaviors-Snapshots</id>
            <url>http://repository-chiralbehaviors.forge.cloudbees.com/snapshot/</url>
        </repository>
        <repository>
            <id>ChiralBehaviors-release</id>
            <url>http://repository-chiralbehaviors.forge.cloudbees.com/release/</url>
        </repository>
    </repositories>

	<dependencies>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.10</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<version>1.7.7</version>
		</dependency>
		<dependency>
			<groupId>org.mockito</groupId>
			<artifactId>mockito-all</artifactId>
			<version>1.9.0</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-classic</artifactId>
			<version>1.1.1</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.uuid</groupId>
			<artifactId>java-uuid-generator</artifactId>
			<version>3.1.3</version>
			<exclusions>
				<exclusion>
					<artifactId>log4j</artifactId>
					<groupId>log4j</groupId>
				</exclusion>
			</exclusions>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-annotations</artifactId>
			<version>2.4.1</version>
            <optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.dataformat</groupId>
			<artifactId>jackson-dataformat-yaml</artifactId>
			<version>2.4.1</version>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-databind</artifactId>
			<version>2.4.1</version>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.chiralbehaviors</groupId>
			<artifactId>failure-detectors</artifactId>
			<version>0.0.1</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>2.3.2</version>
				<configuration>
					<source>1.7</source>
					<target>1.7</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-source-plugin</artifactId>
				<version>2.2.1</version>
				<executions>
					<execution>
						<id>attach-sources</id>
						<phase>verify</phase>
						<goals>
							<goal>jar-no-fork</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</project>