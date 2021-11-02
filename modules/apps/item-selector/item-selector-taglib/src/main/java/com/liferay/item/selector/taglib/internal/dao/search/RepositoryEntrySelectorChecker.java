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

package com.liferay.item.selector.taglib.internal.dao.search;

import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.ResultRowSplitterEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileShortcut;
import com.liferay.portal.kernel.repository.model.RepositoryEntry;
import com.liferay.portal.kernel.util.ArrayUtil;

import javax.portlet.RenderResponse;

/**
 * @author Alessio Antonio Rendina
 */
public class RepositoryEntrySelectorChecker extends EmptyOnClickRowChecker {

	public RepositoryEntrySelectorChecker(
		RenderResponse renderResponse, long[] checkedUserIds,
		boolean checkedUserIdsEnabled) {

		super(renderResponse);

		_checkedUserIds = checkedUserIds;
		_checkedUserIdsEnabled = checkedUserIdsEnabled;
	}

	@Override
	public boolean isChecked(Object object) {
//		RepositoryEntry resultRowSplitterEntry = (RepositoryEntry)object;

		FileEntry fileEntry = null;

		if (object instanceof FileEntry) {
			fileEntry = (FileEntry)object;
		}
		else if (object instanceof FileShortcut) {
			FileShortcut fileShortcut = (FileShortcut)object;

			try {
				fileEntry = DLAppLocalServiceUtil.getFileEntry(fileShortcut.getToFileEntryId());
			}
			catch (PortalException e) {
				e.printStackTrace();
			}
		}

		return ArrayUtil.contains(_checkedUserIds, fileEntry.getFileEntryId());
	}

	@Override
	public boolean isDisabled(Object object) {
		if (!_checkedUserIdsEnabled) {
			return isChecked(object);
		}

		return false;
	}

	private final long[] _checkedUserIds;
	private final boolean _checkedUserIdsEnabled;

}