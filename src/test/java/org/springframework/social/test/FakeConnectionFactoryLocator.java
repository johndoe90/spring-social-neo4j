package org.springframework.social.test;

import java.util.Set;

import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;

public class FakeConnectionFactoryLocator implements ConnectionFactoryLocator {

	@Override
	public ConnectionFactory<?> getConnectionFactory(String providerId) {
		return new FakeConnectionFactory<Object>("fake", null, null);
	}

	@Override
	public <A> ConnectionFactory<A> getConnectionFactory(Class<A> apiType) {
		return null;
	}

	@Override
	public Set<String> registeredProviderIds() {
		return null;
	}

}
