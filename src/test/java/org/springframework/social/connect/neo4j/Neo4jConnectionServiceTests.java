package org.springframework.social.connect.neo4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.test.FakeConnection;
import org.springframework.social.test.FakeConnectionFactory;
import org.springframework.social.test.FakeProvider;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ApplicationConfig.class})
public class Neo4jConnectionServiceTests {

	@Autowired
	private Neo4jTemplate neo4jTemplate;
	
	@Autowired
	private Neo4jConnectionService service;
	
	private final FakeConnectionFactory<FakeProvider> factory = new FakeConnectionFactory<FakeProvider>("fake", null, null);
	
	@Before
	public void setup() {
		String q = "CREATE (joey:User:_User {id: 'joey'}) "
					+ "CREATE (joeyFacebook:SocialConnection:_SocialConnection {providerId: 'facebook', providerUserId: 'joey.ramones', displayName: 'joey r.', rank: 1}) "
					+ "CREATE (joeyTwitterA:SocialConnection:_SocialConnection {providerId: 'twitter', providerUserId: '@JeffreyHyman', displayName: 'joey r.', rank: 2}) "
					+ "CREATE (joeyTwitterB:SocialConnection:_SocialConnection {providerId: 'twitter', providerUserId: '@joey_ramones', displayName: 'joey r.', rank: 1}) "
					+ "CREATE (joey)-[:SOCIAL_CONNECTION]->(joeyFacebook), (joey)-[:SOCIAL_CONNECTION]->(joeyTwitterA), (joey)-[:SOCIAL_CONNECTION]->(joeyTwitterB) "
				 + "CREATE (johnny:User:_User {id: 'johnny'}) "
					+ "CREATE (johnnyFacebook:SocialConnection:_SocialConnection {providerId: 'facebook', providerUserId: 'JohnnyRamones', displayName: 'johnny r.', rank: 1}) "
					+ "CREATE (johnny)-[:SOCIAL_CONNECTION]->(johnnyFacebook) "
				 + "CREATE (tommy:User:_User {id: 'tommy'}) "
					+ "CREATE (tommyTwitter:SocialConnection:_SocialConnection {providerId: 'twitter', providerUserId: '@joey_ramones', displayName: 'joey r.', rank: 1}) "
					+ "CREATE (tommy)-[:SOCIAL_CONNECTION]->(tommyTwitter) "
				 + "CREATE (cj:User:_User {id: 'cj'}) "
					+ "CREATE (cjFake:SocialConnection:_SocialConnection {providerId: 'fake', providerUserId: 'c-j', displayName: 'cj', rank: 1}) "
					+ "CREATE (cj)-[:SOCIAL_CONNECTION]->(cjFake)";
		neo4jTemplate.query(q, null);
	}
	
	@After
	public void tearDown() {
		String q = "START n=node(*) OPTIONAL MATCH (n)-[r]-() DELETE r,n";
		neo4jTemplate.query(q, null);
	}
	
	@Test
	@Transactional
	public void shouldReturnMultipleConnections() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.put("twitter", Arrays.asList("@JeffreyHyman", "@joey_ramones"));
		map.put("facebook", Arrays.asList("joey.ramones"));

		List<Connection<?>> connections = service.getConnections("joey", map);
		assertEquals(3, connections.size());
		assertEquals("[{facebook, joey.ramones, joey r.}, {twitter, @joey_ramones, joey r.}, {twitter, @JeffreyHyman, joey r.}]", connections.toString());
	}
	
	@Test
	@Transactional
	public void shouldReturnTheUserIds() {
		List<String> userIds = service.getUserIds("twitter", "@joey_ramones");
		assertNotNull(userIds);
		assertEquals(2, userIds.size());
		
		assertTrue(userIds.contains("joey"));
		assertTrue(userIds.contains("tommy"));
	}

	@Test
	@Transactional
	public void shouldReturnTheSetOfUserIds() {
		Set<String> providedIds = new HashSet<String>();
		providedIds.add("joey.ramones");
		providedIds.add("JohnnyRamones");

		Set<String> userIds = service.getUserIds("facebook", providedIds);
		assertNotNull(userIds);
		assertEquals(2, userIds.size());
		
		assertTrue(userIds.contains("joey"));
		assertTrue(userIds.contains("johnny"));
	}

	@Test
	@Transactional
	public void shouldReturnTheDefaultRank() {
		int rank = service.getMaxRank("deedee", "twitter");
		assertEquals(1, rank);
	}

	@Test
	@Transactional
	public void shouldReturnTheMaxRankForAProvider() {
		int rank = service.getMaxRank("joey", "twitter");
		assertEquals(3, rank);
	}

	@Test
	@Transactional
	public void shouldReturnNullIfTheConnectionIsNotFound() {
		Connection<?> conn = service.getConnection("a", "b", "c");
		assertNull(conn);
	}

	@Test
	@Transactional
	public void shouldFindPrimaryConnection() {
		Connection<?> conn = service.getPrimaryConnection("joey", "twitter");
		assertNotNull("Connection not found", conn);
		assertEquals("twitter", conn.getKey().getProviderId());
		assertEquals("@joey_ramones", conn.getKey().getProviderUserId());
	}

	@Test
	@Transactional
	public void shouldFindConnection() {
		Connection<?> conn = service.getConnection("joey", "facebook", "joey.ramones");
		assertNotNull("Connection not found", conn);
		assertEquals("facebook", conn.getKey().getProviderId());
		assertEquals("joey.ramones", conn.getKey().getProviderUserId());
	}

	@Test
	@Transactional
	public void shouldListTheConnectionsForUserAndProviderSortByRank() {
		List<Connection<?>> connections = service.getConnections("joey", "twitter");
		assertEquals(2, connections.size());
		assertEquals("[{twitter, @joey_ramones, joey r.}, {twitter, @JeffreyHyman, joey r.}]", connections.toString());
	}

	@Test
	@Transactional
	public void shouldListTheConnectionsForUserSortByProviderAndRank() {
		List<Connection<?>> connections = service.getConnections("joey");
		assertEquals(3, connections.size());
		assertEquals("[{facebook, joey.ramones, joey r.}, {twitter, @joey_ramones, joey r.}, {twitter, @JeffreyHyman, joey r.}]", connections.toString());
	}

	@Test
	@Transactional
	public void shouldCreateNewConnection() {
		String q = "CREATE (user:User:_User {id: 'UserId'})";
		neo4jTemplate.query(q, null);
				
		Connection<?> userConn = factory.createConnection("userName", "user name");
		service.create("UserId", userConn, 5);
		
		
		List<Connection<?>> connections = service.getConnections("UserId", "fake");
		assertEquals(1, connections.size());

		FakeConnection<?> conn = (FakeConnection<?>) connections.get(0);
		assertEquals("fake", conn.getData().getProviderId());
		assertEquals("userName", conn.getData().getProviderUserId());
		assertEquals("user name", conn.getData().getDisplayName());
	}

	@Transactional
	@Test(expected = DuplicateKeyException.class)
	public void shouldThrowExceptionIfConnectionExists() {
		Connection<?> userConn = factory.createConnection("c-j", "cj");
		service.create("cj", userConn, 2);
	}

	@Test
	@Transactional
	public void shouldUpdateTheConnection() {
		Connection<?> conn = service.getConnection("joey", "twitter", "@JeffreyHyman");
		assertEquals("joey r.", conn.getDisplayName());

		service.update("joey", conn);

		Connection<?> conn2 = service.getConnection("joey", "twitter", "@JeffreyHyman");
		assertEquals("joey r.", conn2.getDisplayName());
	}

	@Test
	@Transactional
	public void shouldRemoveTheConnection() {
		service.remove("joey", new ConnectionKey("twitter", "@JeffreyHyman"));

		Connection<?> conn = service.getConnection("joey", "twitter", "@JeffreyHyman");
		assertNull("Connection not removed", conn);
	}

	@Test
	@Transactional
	public void shouldRemoveTheConnectionForAProvider() {
		service.remove("joey", "twitter");

		List<Connection<?>> conn = service.getConnections("joey", "twitter");
		assertEquals(0, conn.size());
	}
}
