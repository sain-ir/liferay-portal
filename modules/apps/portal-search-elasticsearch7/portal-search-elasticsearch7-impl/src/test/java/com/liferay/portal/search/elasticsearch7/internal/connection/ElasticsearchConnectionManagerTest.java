/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.elasticsearch7.internal.connection;

import com.liferay.portal.search.elasticsearch7.configuration.RESTClientLoggerLevel;
import com.liferay.portal.search.elasticsearch7.internal.configuration.ElasticsearchConfigurationWrapper;
import com.liferay.portal.search.elasticsearch7.internal.configuration.OperationModeResolver;

import org.elasticsearch.client.RestHighLevelClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author André de Oliveira
 */
public class ElasticsearchConnectionManagerTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		resetAndSetUpMocks();

		_elasticsearchConnectionManager = createElasticsearchConnectionManager(
			_sidecarElasticsearchConnection, _remoteElasticsearchConnection1,
			_remoteElasticsearchConnection2, _remoteElasticsearchConnection3);
	}

	@Test
	public void testActivateRemoteModeDisabled() {
		ElasticsearchConnectionManager elasticsearchConnectionManager =
			Mockito.spy(_elasticsearchConnectionManager);

		elasticsearchConnectionManager.activate();

		Mockito.verify(
			elasticsearchConnectionManager, Mockito.never()
		).addElasticsearchConnection(
			Mockito.any()
		);

		Mockito.verify(
			elasticsearchConnectionManager
		).removeElasticsearchConnection(
			Mockito.any()
		);
	}

	@Test
	public void testActivateRemoteModeEnabledWithConnectionId() {
		Mockito.when(
			_operationModeResolver.isProductionModeEnabled()
		).thenReturn(
			true
		);

		Mockito.when(
			_elasticsearchConfigurationWrapper.remoteClusterConnectionId()
		).thenReturn(
			"test"
		);

		ElasticsearchConnectionManager elasticsearchConnectionManager =
			Mockito.spy(_elasticsearchConnectionManager);

		elasticsearchConnectionManager.activate();

		Mockito.verify(
			elasticsearchConnectionManager, Mockito.never()
		).addElasticsearchConnection(
			Mockito.any()
		);

		Mockito.verify(
			elasticsearchConnectionManager, Mockito.never()
		).removeElasticsearchConnection(
			Mockito.any()
		);
	}

	@Test
	public void testActivateRemoteModeEnabledWithoutConnectionId() {
		Mockito.when(
			_operationModeResolver.isProductionModeEnabled()
		).thenReturn(
			true
		);

		Mockito.when(
			_elasticsearchConfigurationWrapper.remoteClusterConnectionId()
		).thenReturn(
			null
		);

		Mockito.when(
			_elasticsearchConfigurationWrapper.networkHostAddresses()
		).thenReturn(
			new String[] {"http://localhost:9200"}
		);

		ElasticsearchConnectionManager elasticsearchConnectionManager =
			Mockito.spy(_elasticsearchConnectionManager);

		elasticsearchConnectionManager.activate();

		Mockito.verify(
			elasticsearchConnectionManager
		).addElasticsearchConnection(
			Mockito.any()
		);

		Mockito.verify(
			elasticsearchConnectionManager, Mockito.never()
		).removeElasticsearchConnection(
			Mockito.any()
		);
	}

	@Test
	public void testAddConnectionNoConnectionIdAndIsActive() {
		ElasticsearchConnection elasticsearchConnection = Mockito.mock(
			ElasticsearchConnection.class);

		Mockito.when(
			elasticsearchConnection.getConnectionId()
		).thenReturn(
			null
		);
		Mockito.when(
			elasticsearchConnection.isActive()
		).thenReturn(
			true
		);

		_elasticsearchConnectionManager.addElasticsearchConnection(
			elasticsearchConnection);

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).isActive();

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).connect();
	}

	@Test
	public void testAddConnectionNoConnectionIdAndIsNotActive() {
		ElasticsearchConnection elasticsearchConnection = Mockito.mock(
			ElasticsearchConnection.class);

		Mockito.when(
			elasticsearchConnection.getConnectionId()
		).thenReturn(
			null
		);
		Mockito.when(
			elasticsearchConnection.isActive()
		).thenReturn(
			false
		);

		_elasticsearchConnectionManager.addElasticsearchConnection(
			elasticsearchConnection);

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).isActive();

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).connect();
	}

	@Test
	public void testAddConnectionWithConnectionIdAndIsActive() {
		ElasticsearchConnection elasticsearchConnection = Mockito.mock(
			ElasticsearchConnection.class);

		Mockito.when(
			elasticsearchConnection.getConnectionId()
		).thenReturn(
			"test"
		);
		Mockito.when(
			elasticsearchConnection.isActive()
		).thenReturn(
			true
		);

		_elasticsearchConnectionManager.addElasticsearchConnection(
			elasticsearchConnection);

		Mockito.verify(
			elasticsearchConnection
		).isActive();

		Mockito.verify(
			elasticsearchConnection
		).connect();
	}

	@Test
	public void testAddConnectionWithConnectionIdAndIsNotActive() {
		ElasticsearchConnection elasticsearchConnection = Mockito.mock(
			ElasticsearchConnection.class);

		Mockito.when(
			elasticsearchConnection.getConnectionId()
		).thenReturn(
			"test"
		);
		Mockito.when(
			elasticsearchConnection.isActive()
		).thenReturn(
			false
		);

		_elasticsearchConnectionManager.addElasticsearchConnection(
			elasticsearchConnection);

		Mockito.verify(
			elasticsearchConnection
		).isActive();

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).connect();
	}

	@Test
	public void testGetElasticsearchConnectionWithRemoteModeDisabled() {
		Assert.assertEquals(
			_sidecarElasticsearchConnection,
			_elasticsearchConnectionManager.getElasticsearchConnection());
	}

	@Test
	public void testGetElasticsearchConnectionWithRemoteModeEnabled() {
		enableRemoteMode();

		Assert.assertEquals(
			_defaultRemoteElasticsearchConnection,
			_elasticsearchConnectionManager.getElasticsearchConnection());
	}

	@Test
	public void testGetElasticsearchConnectionWithRemoteModeEnabledAndConnectionId() {
		enableRemoteMode();
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection1,
			_elasticsearchConnectionManager.getElasticsearchConnection());

		setRemoteConnectionId(_REMOTE_2_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection2,
			_elasticsearchConnectionManager.getElasticsearchConnection());
	}

	@Test
	public void testGetExplicitElasticsearchConnectionWhenConnectionDoesNotExist() {
		Assert.assertEquals(
			null,
			_elasticsearchConnectionManager.getElasticsearchConnection("none"));
	}

	@Test
	public void testGetExplicitElasticsearchConnectionWhenConnectionIdNull() {
		try {
			_elasticsearchConnectionManager.getElasticsearchConnection(null);

			Assert.fail();
		}
		catch (NullPointerException nullPointerException) {
		}
	}

	@Test
	public void testGetExplicitElasticsearchConnectionWithDifferentConnectionId() {
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection2,
			_elasticsearchConnectionManager.getElasticsearchConnection(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitElasticsearchConnectionWithRemoteModeDisabled() {
		Assert.assertEquals(
			_remoteElasticsearchConnection1,
			_elasticsearchConnectionManager.getElasticsearchConnection(
				_REMOTE_1_CONNECTION_ID));

		Assert.assertEquals(
			_remoteElasticsearchConnection2,
			_elasticsearchConnectionManager.getElasticsearchConnection(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitElasticsearchConnectionWithRemoteModeEnabled() {
		enableRemoteMode();

		Assert.assertEquals(
			_remoteElasticsearchConnection1,
			_elasticsearchConnectionManager.getElasticsearchConnection(
				_REMOTE_1_CONNECTION_ID));

		Assert.assertEquals(
			_remoteElasticsearchConnection2,
			_elasticsearchConnectionManager.getElasticsearchConnection(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitElasticsearchConnectionWithRemoteModeEnabledAndDifferentConnectionId() {
		enableRemoteMode();
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection2,
			_elasticsearchConnectionManager.getElasticsearchConnection(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitRestHighLevelClientWhenRestClientNull() {
		try {
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_3_CONNECTION_ID);

			Assert.fail();
		}
		catch (ElasticsearchConnectionNotInitializedException
					elasticsearchConnectionNotInitializedException) {

			String message =
				elasticsearchConnectionNotInitializedException.getMessage();

			Assert.assertTrue(
				message.contains("REST high level client not found"));
		}
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeDisabled() {
		Assert.assertEquals(
			_remoteElasticsearchConnection1.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_1_CONNECTION_ID));

		Assert.assertEquals(
			_remoteElasticsearchConnection2.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeDisabledAndConnectionDoesNotExist() {
		try {
			_elasticsearchConnectionManager.getRestHighLevelClient("none");

			Assert.fail();
		}
		catch (ElasticsearchConnectionNotInitializedException
					elasticsearchConnectionNotInitializedException) {

			String message =
				elasticsearchConnectionNotInitializedException.getMessage();

			Assert.assertTrue(
				message.contains("Elasticsearch connection not found"));
		}
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeDisabledAndDifferentConnectionId() {
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection2.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeDisabledAndIdNull() {
		Assert.assertEquals(
			_sidecarElasticsearchConnection.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(null));
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeEnabled() {
		enableRemoteMode();

		Assert.assertEquals(
			_remoteElasticsearchConnection1.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_1_CONNECTION_ID));

		Assert.assertEquals(
			_remoteElasticsearchConnection2.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeEnabledAndConnectionDoesNotExist() {
		enableRemoteMode();

		try {
			_elasticsearchConnectionManager.getRestHighLevelClient("none");

			Assert.fail();
		}
		catch (ElasticsearchConnectionNotInitializedException
					elasticsearchConnectionNotInitializedException) {

			String message =
				elasticsearchConnectionNotInitializedException.getMessage();

			Assert.assertTrue(
				message.contains("Elasticsearch connection not found"));
		}
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeEnabledAndDifferentConnectionId() {
		enableRemoteMode();
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection2.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(
				_REMOTE_2_CONNECTION_ID));
	}

	@Test
	public void testGetExplicitRestHighLevelClientWithRemoteModeEnabledAndIdNull() {
		enableRemoteMode();

		Assert.assertEquals(
			_defaultRemoteElasticsearchConnection.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient(null));
	}

	@Test
	public void testGetRestHighLevelClientWithRemoteModeDisabled() {
		Assert.assertEquals(
			_sidecarElasticsearchConnection.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient());
	}

	@Test
	public void testGetRestHighLevelClientWithRemoteModeDisabledAndConnectionId() {
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_sidecarElasticsearchConnection.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient());

		setRemoteConnectionId(_REMOTE_2_CONNECTION_ID);

		Assert.assertEquals(
			_sidecarElasticsearchConnection.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient());
	}

	@Test
	public void testGetRestHighLevelClientWithRemoteModeEnabled() {
		enableRemoteMode();

		Assert.assertEquals(
			_defaultRemoteElasticsearchConnection.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient());
	}

	@Test
	public void testGetRestHighLevelClientWithRemoteModeEnabledAndConnectionId() {
		enableRemoteMode();
		setRemoteConnectionId(_REMOTE_1_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection1.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient());

		setRemoteConnectionId(_REMOTE_2_CONNECTION_ID);

		Assert.assertEquals(
			_remoteElasticsearchConnection2.getRestHighLevelClient(),
			_elasticsearchConnectionManager.getRestHighLevelClient());
	}

	@Test
	public void testRemoveConnectionThatDoesNotExistWithConnectionId() {
		ElasticsearchConnection elasticsearchConnection = Mockito.mock(
			ElasticsearchConnection.class);

		Mockito.when(
			elasticsearchConnection.getConnectionId()
		).thenReturn(
			"test"
		);

		_elasticsearchConnectionManager.removeElasticsearchConnection(
			elasticsearchConnection.getConnectionId());

		Mockito.verify(
			elasticsearchConnection, Mockito.never()
		).close();
	}

	@Test
	public void testRemoveConnectionThatExistsWithConnectionId() {
		_elasticsearchConnectionManager.removeElasticsearchConnection(
			_remoteElasticsearchConnection1.getConnectionId());

		Mockito.verify(
			_remoteElasticsearchConnection1
		).close();
	}

	@Test
	public void testRemoveConnectionWithNullConnectionId() {
		_elasticsearchConnectionManager.removeElasticsearchConnection(null);
	}

	protected ElasticsearchConnectionManager
		createElasticsearchConnectionManager(
			ElasticsearchConnection remoteElasticsearchConnection1,
			ElasticsearchConnection remoteElasticsearchConnection2,
			ElasticsearchConnection remoteElasticsearchConnection3,
			ElasticsearchConnection sidecarElasticsearchConnection) {

		ElasticsearchConnectionManager elasticsearchConnectionManager =
			new ElasticsearchConnectionManager() {
				{
					elasticsearchConfigurationWrapper =
						_elasticsearchConfigurationWrapper;
					operationModeResolver = _operationModeResolver;
				}
			};

		elasticsearchConnectionManager.addElasticsearchConnection(
			remoteElasticsearchConnection1);
		elasticsearchConnectionManager.addElasticsearchConnection(
			remoteElasticsearchConnection2);
		elasticsearchConnectionManager.addElasticsearchConnection(
			remoteElasticsearchConnection3);
		elasticsearchConnectionManager.addElasticsearchConnection(
			sidecarElasticsearchConnection);

		elasticsearchConnectionManager.activate();

		return elasticsearchConnectionManager;
	}

	protected void enableRemoteMode() {
		Mockito.when(
			_operationModeResolver.isProductionModeEnabled()
		).thenReturn(
			true
		);

		Mockito.when(
			_operationModeResolver.isDevelopmentModeEnabled()
		).thenReturn(
			false
		);

		_elasticsearchConnectionManager.addElasticsearchConnection(
			_defaultRemoteElasticsearchConnection);
	}

	protected void resetAndSetUpMocks() {
		Mockito.reset(
			_defaultRemoteElasticsearchConnection,
			_elasticsearchConfigurationWrapper, _operationModeResolver,
			_remoteElasticsearchConnection1, _remoteElasticsearchConnection2,
			_remoteElasticsearchConnection3, _sidecarElasticsearchConnection);

		setUpDefaultConnection();
		setUpElasticsearchConfigurationWrapper();
		setUpOperationModeResolver();
		setUpRemoteConnection1();
		setUpRemoteConnection2();
		setUpRemoteConnection3();
		setUpSidecarConnection();
	}

	protected void setRemoteConnectionId(String connectionId) {
		Mockito.when(
			_elasticsearchConfigurationWrapper.remoteClusterConnectionId()
		).thenReturn(
			connectionId
		);
	}

	protected void setUpDefaultConnection() {
		Mockito.when(
			_defaultRemoteElasticsearchConnection.getConnectionId()
		).thenReturn(
			ConnectionConstants.REMOTE_CONNECTION_ID
		);
		Mockito.when(
			_defaultRemoteElasticsearchConnection.getRestHighLevelClient()
		).thenReturn(
			Mockito.mock(RestHighLevelClient.class)
		);
		Mockito.when(
			_defaultRemoteElasticsearchConnection.isActive()
		).thenReturn(
			true
		);
	}

	protected void setUpElasticsearchConfigurationWrapper() {
		Mockito.when(
			_elasticsearchConfigurationWrapper.restClientLoggerLevel()
		).thenReturn(
			RESTClientLoggerLevel.ERROR
		);
	}

	protected void setUpOperationModeResolver() {
		Mockito.when(
			_operationModeResolver.isProductionModeEnabled()
		).thenReturn(
			false
		);

		Mockito.when(
			_operationModeResolver.isDevelopmentModeEnabled()
		).thenReturn(
			true
		);
	}

	protected void setUpRemoteConnection1() {
		Mockito.when(
			_remoteElasticsearchConnection1.getConnectionId()
		).thenReturn(
			_REMOTE_1_CONNECTION_ID
		);
		Mockito.when(
			_remoteElasticsearchConnection1.getRestHighLevelClient()
		).thenReturn(
			Mockito.mock(RestHighLevelClient.class)
		);
		Mockito.when(
			_remoteElasticsearchConnection1.isActive()
		).thenReturn(
			true
		);
	}

	protected void setUpRemoteConnection2() {
		Mockito.when(
			_remoteElasticsearchConnection2.getConnectionId()
		).thenReturn(
			_REMOTE_2_CONNECTION_ID
		);
		Mockito.when(
			_remoteElasticsearchConnection2.getRestHighLevelClient()
		).thenReturn(
			Mockito.mock(RestHighLevelClient.class)
		);
		Mockito.when(
			_remoteElasticsearchConnection2.isActive()
		).thenReturn(
			true
		);
	}

	protected void setUpRemoteConnection3() {
		Mockito.when(
			_remoteElasticsearchConnection3.getConnectionId()
		).thenReturn(
			_REMOTE_3_CONNECTION_ID
		);
		Mockito.when(
			_remoteElasticsearchConnection3.getRestHighLevelClient()
		).thenReturn(
			null
		);
		Mockito.when(
			_remoteElasticsearchConnection3.isActive()
		).thenReturn(
			false
		);
	}

	protected void setUpSidecarConnection() {
		Mockito.when(
			_sidecarElasticsearchConnection.getConnectionId()
		).thenReturn(
			ConnectionConstants.SIDECAR_CONNECTION_ID
		);
		Mockito.when(
			_sidecarElasticsearchConnection.getRestHighLevelClient()
		).thenReturn(
			Mockito.mock(RestHighLevelClient.class)
		);
		Mockito.when(
			_sidecarElasticsearchConnection.isActive()
		).thenReturn(
			true
		);
	}

	private static final String _REMOTE_1_CONNECTION_ID = "remote 1";

	private static final String _REMOTE_2_CONNECTION_ID = "remote 2";

	private static final String _REMOTE_3_CONNECTION_ID = "remote 3";

	@Mock
	private ElasticsearchConnection _defaultRemoteElasticsearchConnection;

	@Mock
	private ElasticsearchConfigurationWrapper
		_elasticsearchConfigurationWrapper;

	private ElasticsearchConnectionManager _elasticsearchConnectionManager;

	@Mock
	private OperationModeResolver _operationModeResolver;

	@Mock
	private ElasticsearchConnection _remoteElasticsearchConnection1;

	@Mock
	private ElasticsearchConnection _remoteElasticsearchConnection2;

	@Mock
	private ElasticsearchConnection _remoteElasticsearchConnection3;

	@Mock
	private ElasticsearchConnection _sidecarElasticsearchConnection;

}