package org.springframework.social.connect.neo4j;

import javax.transaction.TransactionManager;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.aspects.config.Neo4jAspectConfiguration;
import org.springframework.data.neo4j.config.JtaTransactionManagerFactoryBean;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.test.FakeConnectionFactoryLocator;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@ComponentScan(basePackages = {"org.springframework.social.connect.neo4j"})
public class ApplicationConfig extends Neo4jAspectConfiguration {

	public ApplicationConfig() {
		setBasePackage("org.springframework.social.connect.neo4j");
	}
	
	@Bean
	public JtaTransactionManager neo4jTransactionManager() throws Exception {
		return new JtaTransactionManagerFactoryBean(getGraphDatabaseService()).getObject();
	}
	
	@Bean
	public Neo4jTemplate neo4jTemplate() {
		return new Neo4jTemplate(graphDatabaseService());
	}
	
	@Bean
	public GraphDatabaseService graphDatabaseService() {
		return new TestGraphDatabaseFactory().newImpermanentDatabase();
		//return new SpringRestGraphDatabase("http://localhost:7474/db/data/");
	}
	
	@Bean
	public TextEncryptor textEncryptor() {
		return Encryptors.noOpText();
	}
	
	@Bean
	public ConnectionFactoryLocator connectionFactoryLocator() {
		return new FakeConnectionFactoryLocator();
	}
	
	@Bean
	public ConnectionConverter connectionConverter() {
		return new ConnectionConverter(connectionFactoryLocator(), textEncryptor());
	}
	
	@Bean
	public ConnectionService neo4jConnectionService() {
		return new Neo4jConnectionService(neo4jTemplate(), connectionConverter());
	}
}
