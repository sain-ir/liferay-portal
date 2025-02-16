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

package com.liferay.journal.trash.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.journal.service.JournalFolderService;
import com.liferay.journal.test.util.JournalFolderFixture;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.ClassedModel;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;
import com.liferay.registry.ServiceTracker;
import com.liferay.trash.TrashHelper;
import com.liferay.trash.test.util.BaseTrashHandlerTestCase;
import com.liferay.trash.test.util.DefaultWhenIsAssetable;
import com.liferay.trash.test.util.DefaultWhenIsIndexableBaseModel;
import com.liferay.trash.test.util.WhenCanBeDuplicatedInTrash;
import com.liferay.trash.test.util.WhenHasGrandParent;
import com.liferay.trash.test.util.WhenIsAssetable;
import com.liferay.trash.test.util.WhenIsAssetableBaseModel;
import com.liferay.trash.test.util.WhenIsAssetableParentModel;
import com.liferay.trash.test.util.WhenIsIndexableBaseModel;
import com.liferay.trash.test.util.WhenIsMoveableFromTrashBaseModel;
import com.liferay.trash.test.util.WhenIsRestorableBaseModel;
import com.liferay.trash.test.util.WhenIsUpdatableBaseModel;
import com.liferay.trash.test.util.WhenParentModelIsSameType;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * @author Eudaldo Alonso
 */
@RunWith(Arquillian.class)
public class JournalFolderTrashHandlerTest
	extends BaseTrashHandlerTestCase
	implements WhenCanBeDuplicatedInTrash, WhenHasGrandParent,
			   WhenIsAssetableBaseModel, WhenIsAssetableParentModel,
			   WhenIsIndexableBaseModel, WhenIsMoveableFromTrashBaseModel,
			   WhenIsRestorableBaseModel, WhenIsUpdatableBaseModel,
			   WhenParentModelIsSameType {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() {
		Registry registry = RegistryUtil.getRegistry();

		_serviceTracker = registry.trackServices(TrashHelper.class.getName());

		_serviceTracker.open();
	}

	@AfterClass
	public static void tearDownClass() {
		_serviceTracker.close();
	}

	@Override
	public AssetEntry fetchAssetEntry(ClassedModel classedModel)
		throws Exception {

		return _whenIsAssetable.fetchAssetEntry(classedModel);
	}

	@Override
	public String getBaseModelName(ClassedModel classedModel) {
		JournalFolder folder = (JournalFolder)classedModel;

		return folder.getName();
	}

	@Override
	public String getParentBaseModelClassName() {
		return getBaseModelClass().getName();
	}

	@Override
	public String getSearchKeywords() {
		return _FOLDER_NAME;
	}

	@Override
	public boolean isAssetEntryVisible(ClassedModel classedModel, long classPK)
		throws Exception {

		return _whenIsAssetable.isAssetEntryVisible(classedModel, classPK);
	}

	@Override
	public BaseModel<?> moveBaseModelFromTrash(
			ClassedModel classedModel, Group group,
			ServiceContext serviceContext)
		throws Exception {

		BaseModel<?> parentBaseModel = getParentBaseModel(
			group, serviceContext);

		journalFolderService.moveFolderFromTrash(
			(Long)classedModel.getPrimaryKeyObj(),
			(Long)parentBaseModel.getPrimaryKeyObj(), serviceContext);

		return parentBaseModel;
	}

	@Override
	public void moveParentBaseModelToTrash(long primaryKey) throws Exception {
		journalFolderService.moveFolderToTrash(primaryKey);
	}

	@Override
	public int searchBaseModelsCount(Class<?> clazz, long groupId)
		throws Exception {

		return _whenIsIndexableBaseModel.searchBaseModelsCount(clazz, groupId);
	}

	@Override
	public int searchTrashEntriesCount(
			String keywords, ServiceContext serviceContext)
		throws Exception {

		return _whenIsIndexableBaseModel.searchTrashEntriesCount(
			keywords, serviceContext);
	}

	@Before
	@Override
	public void setUp() throws Exception {
		UserTestUtil.setUser(TestPropsValues.getUser());

		_trashHelper = _serviceTracker.getService();

		super.setUp();

		_journalFolderFixture = new JournalFolderFixture(
			journalFolderLocalService);
	}

	@Override
	public BaseModel<?> updateBaseModel(
			long primaryKey, ServiceContext serviceContext)
		throws Exception {

		JournalFolder folder = journalFolderLocalService.getFolder(primaryKey);

		if (serviceContext.getWorkflowAction() ==
				WorkflowConstants.ACTION_SAVE_DRAFT) {

			folder = journalFolderLocalService.updateStatus(
				TestPropsValues.getUserId(), folder,
				WorkflowConstants.STATUS_DRAFT);
		}

		return folder;
	}

	@Override
	protected BaseModel<?> addBaseModelWithWorkflow(
			BaseModel<?> parentBaseModel, ServiceContext serviceContext)
		throws Exception {

		JournalFolder parentFolder = (JournalFolder)parentBaseModel;

		String name = getSearchKeywords();

		name += RandomTestUtil.randomString(
			_FOLDER_NAME_MAX_LENGTH - name.length());

		return _journalFolderFixture.addFolder(
			parentFolder.getGroupId(), parentFolder.getFolderId(), name);
	}

	@Override
	protected BaseModel<?> addBaseModelWithWorkflow(
			ServiceContext serviceContext)
		throws Exception {

		return _journalFolderFixture.addFolder(
			serviceContext.getScopeGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			getSearchKeywords());
	}

	@Override
	protected void deleteParentBaseModel(
			BaseModel<?> parentBaseModel, boolean includeTrashedEntries)
		throws Exception {

		JournalFolder folder = (JournalFolder)parentBaseModel;

		journalFolderService.deleteFolder(folder.getFolderId(), false);
	}

	@Override
	protected BaseModel<?> getBaseModel(long primaryKey) throws Exception {
		return journalFolderLocalService.getFolder(primaryKey);
	}

	@Override
	protected Class<?> getBaseModelClass() {
		return JournalFolder.class;
	}

	@Override
	protected int getNotInTrashBaseModelsCount(BaseModel<?> parentBaseModel)
		throws Exception {

		JournalFolder parentDLFolder = (JournalFolder)parentBaseModel;

		return journalFolderLocalService.getFoldersCount(
			parentDLFolder.getGroupId(), parentDLFolder.getFolderId());
	}

	@Override
	protected BaseModel<?> getParentBaseModel(
			Group group, long parentBaseModelId, ServiceContext serviceContext)
		throws Exception {

		return _journalFolderFixture.addFolder(
			group.getGroupId(), parentBaseModelId,
			RandomTestUtil.randomString(_FOLDER_NAME_MAX_LENGTH));
	}

	@Override
	protected BaseModel<?> getParentBaseModel(
			Group group, ServiceContext serviceContext)
		throws Exception {

		return getParentBaseModel(
			group, JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			serviceContext);
	}

	@Override
	protected String getUniqueTitle(BaseModel<?> baseModel) {
		JournalFolder folder = (JournalFolder)baseModel;

		String name = folder.getName();

		return _trashHelper.getOriginalTitle(name);
	}

	@Override
	protected void moveBaseModelToTrash(long primaryKey) throws Exception {
		journalFolderService.moveFolderToTrash(primaryKey);
	}

	@Inject
	protected JournalFolderLocalService journalFolderLocalService;

	@Inject
	protected JournalFolderService journalFolderService;

	private static final String _FOLDER_NAME = RandomTestUtil.randomString(100);

	private static final int _FOLDER_NAME_MAX_LENGTH = 100;

	private static ServiceTracker<TrashHelper, TrashHelper> _serviceTracker;

	private JournalFolderFixture _journalFolderFixture;
	private TrashHelper _trashHelper;
	private final WhenIsAssetable _whenIsAssetable =
		new DefaultWhenIsAssetable();
	private final WhenIsIndexableBaseModel _whenIsIndexableBaseModel =
		new DefaultWhenIsIndexableBaseModel();

}