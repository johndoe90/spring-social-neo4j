package org.springframework.social.test;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.UserProfile;

public class FakeConnection<A> implements Connection<A> {

	private ConnectionData data;
	public FakeConnection(ConnectionData data) {
		this.data = data;
	}

	public ConnectionKey getKey() {
		return new ConnectionKey(data.getProviderId(), data.getProviderUserId());
	}

	public String getDisplayName() {
		return data.getDisplayName();
	}

	public String getProfileUrl() {
		return data.getProfileUrl();
	}

	public String getImageUrl() {
		return data.getImageUrl();
	}

	public ConnectionData getData() {
		return data;
	}

	public void sync() {
	}

	public boolean test() {
		return true;
	}

	public boolean hasExpired() {
		return false;
	}

	public void refresh() {		
	}

	public UserProfile fetchUserProfile() {
		return null;
	}

	public void updateStatus(String message) {		
	}

	public A getApi() {
		return null;
	}

	public ConnectionData createData() {
		return data;
	}

	@Override
	public String toString() {
		return String.format("{%s, %s, %s}", 
				data.getProviderId(), 
				data.getProviderUserId(),
				data.getDisplayName());
	}

}
