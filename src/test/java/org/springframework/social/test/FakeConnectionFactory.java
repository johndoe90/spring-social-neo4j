package org.springframework.social.test;

import org.springframework.social.ServiceProvider;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;

public class FakeConnectionFactory<A> extends ConnectionFactory<A> {

	private String providerId;
	
	public FakeConnectionFactory(String providerId, ServiceProvider<A> serviceProvider, ApiAdapter<A> apiAdapter) {
		super(providerId, serviceProvider, apiAdapter);
		this.providerId = providerId;
	}

	@Override
	public Connection<A> createConnection(ConnectionData data) {
		return createConnection(data.getProviderId(), data.getProviderUserId(), data.getDisplayName());
	}
	
	public Connection<A> createConnection(String providerUserId, String displayName) {
		return createConnection(providerId, providerUserId, displayName);
	}

	public Connection<A> createConnection(String providerId, String providerUserId, String displayName) {
		ConnectionData data = new ConnectionData(providerId,
				providerUserId, 
				displayName,
				String.format("http://profile/%s", providerUserId),
				String.format("http://image/%s", providerUserId),
				"accessToken", 
				"secret", "", 0L);
		
		return new FakeConnection<A>(data);
	}

}
