package org.springframework.social.connect.neo4j;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

public class Neo4jUsersConnectionRepository implements UsersConnectionRepository {

	private final TextEncryptor textEncryptor;
	private final ConnectionService neo4jService;
	private final ConnectionFactoryLocator connectionFactoryLocator;
	
	public Neo4jUsersConnectionRepository(TextEncryptor textEncryptor, ConnectionService neo4jService, ConnectionFactoryLocator connectionFactoryLocator) {
		this.neo4jService = neo4jService;
		this.textEncryptor = textEncryptor;
		this.connectionFactoryLocator = connectionFactoryLocator;
	}
	
	@Override
	public List<String> findUserIdsWithConnection(Connection<?> connection) {
		ConnectionKey key = connection.getKey();
		return neo4jService.getUserIds(key.getProviderId(), key.getProviderUserId());
	}

	@Override
	public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
		return neo4jService.getUserIds(providerId, providerUserIds);
	}

	@Override
	public ConnectionRepository createConnectionRepository(String userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null");
		}
		
		return new Neo4jConnectionRepository(userId, neo4jService, connectionFactoryLocator, textEncryptor);
	}

}
