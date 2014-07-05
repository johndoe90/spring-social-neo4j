package org.springframework.social.connect.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.test.FakeConnectionFactoryLocator;

@Configuration
@ComponentScan(basePackages = {"org.springframework.social.connect.neo4j"})
public class ApplicationConfig extends Neo4jConfiguration {

	public ApplicationConfig() {
		setBasePackage("org.springframework.social.connect.neo4j");
	}
	
	@Bean
	public Neo4jTemplate neo4jTemplate() {
		return new Neo4jTemplate(graphDatabaseService());
	}
	
	@Bean
	public GraphDatabaseService graphDatabaseService() {
		return new TestGraphDatabaseFactory().newImpermanentDatabase();
	}
	
	@Bean
	public TextEncryptor textEncryptor() {
		return Encryptors.noOpText();
	}
	
	@Bean
	public ConnectionFactoryLocator connectionFactoryLocator() {
		return new FakeConnectionFactoryLocator();
	}
}
