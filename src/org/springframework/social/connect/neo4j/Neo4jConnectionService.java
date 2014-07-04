package org.springframework.social.connect.neo4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.util.MultiValueMap;

public class Neo4jConnectionService implements ConnectionService {

	private String userLabel = "User";
	private String userIdProperty = "id";
	
	private final Neo4jTemplate neo4jTemplate;
	private final ConnectionConverter converter;
	
	public Neo4jConnectionService(Neo4jTemplate neo4jTemplate, ConnectionConverter converter) {
		this.converter = converter;
		this.neo4jTemplate = neo4jTemplate;
	}
	
	private boolean connectionExists(String userId, String providerId, String providerUserId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s', providerUserId: '%s'}) RETURN connection", userId, providerId, providerUserId);
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s', providerUserId: '%s'})<-[:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) RETURN connection", providerId, providerUserId, userLabel, userIdProperty, userId);
		
		return neo4jTemplate.query(q, null).to(SocialConnection.class).singleOrNull() == null ? false : true;
	}
	
	@Override
	public int getMaxRank(String userId, String providerId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s'}) RETURN max(connection.rank)", userId, providerId);
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s'})<-[:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) RETURN max(connection.rank)", providerId, userLabel, userIdProperty, userId);
		
		return neo4jTemplate.query(q, null).to(Integer.class).singleOrNull();
	}

	@Override
	public void create(String userId, Connection<?> userConnection, int rank) {
		SocialConnection connection = converter.convert(userConnection);
		connection.setRank(rank);
		
		if(connectionExists(userId, connection.getProviderId(), connection.getProviderUserId())) {
			throw new DuplicateKeyException("Connection already exists");
		}

		String q = String.format("MATCH (user:%s {%s: '%s'}) CREATE (connection:SocialConnection {providerId: '%s', providerUserId: '%s', rank: %d, displayName: '%s', profileUrl: '%s', imageUrl: '%s', accessToken: '%s', secret: '%s', refreshToken: '%s', expireTime: %d}) CREATE (user)-[:SOCIAL_CONNECTION]->(connection)", userLabel, userIdProperty, userId, connection.getProviderId(), connection.getProviderUserId(), connection.getRank(), connection.getDisplayName(), connection.getProfileUrl(), connection.getImageUrl(), connection.getAccessToken(), connection.getSecret(), connection.getRefreshToken(), connection.getExpireTime());
		neo4jTemplate.query(q, null);
	}

	@Override
	public void update(String userId, Connection<?> userConn) {
		SocialConnection connection = converter.convert(userConn);
		//connection.setUserId(userId);
		
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s', providerUserId: '%s'}) SET connection.expireTime = %d SET connection.accessToken = '%s' SET connection.profileUrl = '%s' SET connection.imageUrl = '%s' SET connection.displayName = '%s'", userId, connection.getProviderId(), connection.getProviderUserId(), connection.getExpireTime(), connection.getAccessToken(), connection.getProfileUrl(), connection.getImageUrl(), connection.getDisplayName());
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s', providerUserId: '%s'})<-[:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) SET connection.expireTime = %d SET connection.accessToken = '%s' SET connection.profileUrl = '%s' SET connection.imageUrl = '%s' SET connection.displayName = '%s'", connection.getProviderId(), connection.getProviderUserId(), userLabel, userIdProperty, userId, connection.getExpireTime(), connection.getAccessToken(), connection.getProfileUrl(), connection.getImageUrl(), connection.getDisplayName());
		
		neo4jTemplate.query(q, null);
	}

	@Override
	public void remove(String userId, ConnectionKey connectionKey) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s', providerUserId: '%s'}), (connection)<-[r:SOCIAL]-() DELETE r,connection", userId, connectionKey.getProviderId(), connectionKey.getProviderUserId());
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s', providerUserId: '%s'})<-[r:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) DELTE r,connection", connectionKey.getProviderId(), connectionKey.getProviderUserId(), userLabel, userIdProperty, userId);
		
		neo4jTemplate.query(q, null);
	}

	@Override
	public void remove(String userId, String providerId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s'}), (connection)<-[r:SOCIAL]-() DELETE r,connection", userId, providerId);
		
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s'})<-[r:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) DELETE r,connection", providerId, userLabel, userIdProperty, userId);
		neo4jTemplate.query(q, null);
	}

	@Override
	public Connection<?> getPrimaryConnection(String userId, String providerId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s', rank: 1}) RETURN connection", userId, providerId);
		
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s', rank: 1})<-[:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) RETURN connection", providerId, userLabel, userIdProperty, userId);
		SocialConnection connection = neo4jTemplate.query(q, null).to(SocialConnection.class).singleOrNull();
		
		return connection != null ? converter.convert(connection) : null;
	}

	@Override
	public Connection<?> getConnection(String userId, String providerId, String providerUserId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s', providerUserId: '%s'}) RETURN connection", userId, providerId, providerUserId);
		
		String q = String.format("Match (connection:SocialConnection {providerId: '%s', providerUserId: '%s'})<-[:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) RETURN connection", providerId, providerUserId, userLabel, userIdProperty, userId);
		SocialConnection connection = neo4jTemplate.query(q, null).to(SocialConnection.class).singleOrNull();
		
		return connection != null ? converter.convert(connection) : null;
	}

	@Override
	public List<Connection<?>> getConnections(String userId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s'}) RETURN connection ORDER BY connection.providerId, connection.rank", userId);
		
		String q = String.format("MATCH (connection:SocialConnection)<-[:SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) RETURN connection ORDER BY connection.providerId, connection.rank", userLabel, userIdProperty, userId);
		Result<SocialConnection> results = neo4jTemplate.query(q, null).to(SocialConnection.class);
		
		List<Connection<?>> connections = new ArrayList<>();
		for(SocialConnection connection : results) {
			connections.add(converter.convert(connection));
		}
		
		return connections;
	}

	@Override
	public List<Connection<?>> getConnections(String userId, String providerId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {userId: '%s', providerId: '%s'}) RETURN connection ORDER BY connection.rank", userId, providerId);
		
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s'})<-[SOCIAL_CONNECTION]-(user:%s {%s: '%s'}) RETURN connection ORDER BY connection.rank ", providerId, userLabel, userIdProperty, userId);
		Result<SocialConnection> results = neo4jTemplate.query(q, null).to(SocialConnection.class);
		
		List<Connection<?>> connections = new ArrayList<>();
		for(SocialConnection connection : results) {
			connections.add(converter.convert(connection));
		}
		
		return connections;
	}

	//NOT YET IMPLEMENTED!!!
	@Override
	public List<Connection<?>> getConnections(String userId, MultiValueMap<String, String> providerUsers) {
		if (providerUsers == null || providerUsers.isEmpty()) {
			throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
		}
		
		return null;
	}

	
	@Override
	public Set<String> getUserIds(String providerId, Set<String> providerUserIds) {
		String providerIds = "";
		for(String providerUserId : providerUserIds) {
			providerIds += String.format("'%s',", providerUserId);
		}
		
		//String q = String.format("MATCH (connection:Neo4jConnection {providerId: '%s'}) WHERE connection.providerUserId IN [%s] RETURN connection.userId", providerId, providerIds.substring(0, providerIds.length() - 1));
		
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s'})<-[:SOCIAL_CONNECTION]-(user:%s) WHERE connection.providerUserId IN [%s] RETURN user.%s", providerId, userLabel, providerIds.substring(0, providerIds.length() - 1), userIdProperty);
		Result<String> results = neo4jTemplate.query(q, null).to(String.class);
		
		Set<String> userIds = new HashSet<>();
		for(String userId : userIds) {
			userIds.add(userId);
		}
		
		return userIds;
	}

	
	@Override
	public List<String> getUserIds(String providerId, String providerUserId) {
		//String q = String.format("MATCH (connection:Neo4jConnection {providerId: '%s', providerUserId: '%s'}) RETURN connection.userId", providerId, providerUserId);
		
		String q = String.format("MATCH (connection:SocialConnection {providerId: '%s', providerUserId: '%s'})<-[:SOCIAL_CONNECTION]-(user:%s) RETURN user.%s", providerId, providerUserId, userLabel, userIdProperty);
		Result<String> results = neo4jTemplate.query(q, null).to(String.class);
		
		
		List<String> userIds = new ArrayList<>();
		for(String userId : results) {
			userIds.add(userId);
		}
		
		return userIds;
	}

}
