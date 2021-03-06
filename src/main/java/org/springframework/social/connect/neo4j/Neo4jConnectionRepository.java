package org.springframework.social.connect.neo4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.connect.NoSuchConnectionException;
import org.springframework.social.connect.NotConnectedException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class Neo4jConnectionRepository implements ConnectionRepository {

	private final String userId;
	private final ConnectionService connectionService;
	private final ConnectionFactoryLocator connectionFactoryLocator;

	public Neo4jConnectionRepository(String userId,
			ConnectionService connectionService,
			ConnectionFactoryLocator connectionFactoryLocator) {
		this.userId = userId;
		this.connectionService = connectionService;
		this.connectionFactoryLocator = connectionFactoryLocator;
	}

	@Override
	public MultiValueMap<String, Connection<?>> findAllConnections() {
		List<Connection<?>> results = connectionService.getConnections(this.userId);
		MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<>();
		Set<String> providerIds = this.connectionFactoryLocator.registeredProviderIds();

		for (String providerId : providerIds) {
			connections
					.put(providerId, Collections.<Connection<?>> emptyList());
		}

		for (Connection<?> connection : results) {
			String providerId = connection.getKey().getProviderId();
			if (connections.get(providerId).size() == 0) {
				connections.put(providerId, new LinkedList<Connection<?>>());
			}

			connections.add(providerId, connection);
		}

		return connections;
	}

	@Override
	public List<Connection<?>> findConnections(String providerId) {
		return connectionService.getConnections(this.userId, providerId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> List<Connection<A>> findConnections(Class<A> apiType) {
		List<?> connections = findConnections(getProviderId(apiType));
		return (List<Connection<A>>) connections;
	}

	@Override
	public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUserIds) {
		if (providerUserIds == null || providerUserIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Unable to execute find: no providerUsers provided");
		}

		List<Connection<?>> resultList = connectionService.getConnections(this.userId, providerUserIds);
		MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<String, Connection<?>>();
		for (Connection<?> connection : resultList) {
			String providerId = connection.getKey().getProviderId();
			List<String> userIds = providerUserIds.get(providerId);
			List<Connection<?>> connections = connectionsForUsers.get(providerId);
			if (connections == null) {
				connections = new ArrayList<Connection<?>>(userIds.size());
				for (int i = 0; i < userIds.size(); i++) {
					connections.add(null);
				}
				connectionsForUsers.put(providerId, connections);
			}
			String providerUserId = connection.getKey().getProviderUserId();
			int connectionIndex = userIds.indexOf(providerUserId);
			connections.set(connectionIndex, connection);
		}
		
		return connectionsForUsers;
	}

	@Override
	public Connection<?> getConnection(ConnectionKey connectionKey) {
		Connection<?> connection = connectionService.getConnection(this.userId, connectionKey.getProviderId(), connectionKey.getProviderUserId());
		if (connection == null) {
			throw new NoSuchConnectionException(connectionKey);
		}

		return connection;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
		if (connection == null) {
			throw new NotConnectedException(providerId);
		}

		return connection;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
		String providerId = getProviderId(apiType);
		return (Connection<A>) findPrimaryConnection(providerId);
	}

	@Override
	public void addConnection(Connection<?> connection) {
		try {
			ConnectionData data = connection.createData();
			Integer rank = connectionService.getMaxRank(userId, data.getProviderId());
			connectionService.create(userId, connection, rank);
		} catch (DuplicateKeyException e) {
			throw new DuplicateConnectionException(connection.getKey());
		}
	}

	@Override
	public void updateConnection(Connection<?> connection) {
		connectionService.update(this.userId, connection);
	}

	@Override
	public void removeConnections(String providerId) {
		connectionService.remove(this.userId, providerId);
	}

	@Override
	public void removeConnection(ConnectionKey connectionKey) {
		connectionService.remove(this.userId, connectionKey);
	}

	private <A> String getProviderId(Class<A> apiType) {
		return this.connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
	}

	private Connection<?> findPrimaryConnection(String providerId) {
		return connectionService.getPrimaryConnection(userId, providerId);
	}
}
