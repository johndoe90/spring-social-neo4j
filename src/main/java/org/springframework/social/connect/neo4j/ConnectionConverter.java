package org.springframework.social.connect.neo4j;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;

/**
 * Converts a SpringSocialConnection to a SocialConnection that can be stored in the database
 * @author johndoe
 *
 */
public class ConnectionConverter {

	private final TextEncryptor textEncryptor;
	private final ConnectionFactoryLocator connectionFactoryLocator;

	public ConnectionConverter(ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.textEncryptor = textEncryptor;
		this.connectionFactoryLocator = connectionFactoryLocator;
	}

	public Connection<?> convert(SocialConnection connection) {
		if (connection == null) {
			return null;
		}

		ConnectionData connectionData = fillConnectionData(connection);
		ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());

		return connectionFactory.createConnection(connectionData);
	}

	public SocialConnection convert(Connection<?> connection) {
		ConnectionData data = connection.createData();

		SocialConnection userConnection = new SocialConnection();
		userConnection.setProviderId(data.getProviderId());
		userConnection.setProviderUserId(data.getProviderUserId());
		userConnection.setDisplayName(data.getDisplayName());
		userConnection.setProfileUrl(data.getProfileUrl());
		userConnection.setImageUrl(data.getImageUrl());
		userConnection.setAccessToken(encrypt(data.getAccessToken()));
		userConnection.setSecret(encrypt(data.getSecret()));
		userConnection.setRefreshToken(encrypt(data.getRefreshToken()));
		userConnection.setExpireTime(data.getExpireTime());

		return userConnection;
	}

	private ConnectionData fillConnectionData(SocialConnection connection) {
		return new ConnectionData(connection.getProviderId(),
				connection.getProviderUserId(), connection.getDisplayName(),
				connection.getProfileUrl(), connection.getImageUrl(),
				decrypt(connection.getAccessToken()),
				decrypt(connection.getSecret()),
				decrypt(connection.getRefreshToken()),
				connection.getExpireTime());
	}

	private String decrypt(String encryptedText) {
		return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
	}

	private String encrypt(String text) {
		return text != null ? textEncryptor.encrypt(text) : text;
	}
}
