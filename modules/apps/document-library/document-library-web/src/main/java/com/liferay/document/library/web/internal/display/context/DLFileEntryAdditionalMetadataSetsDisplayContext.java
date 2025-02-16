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

package com.liferay.document.library.web.internal.display.context;

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeServiceUtil;
import com.liferay.document.library.web.internal.configuration.FFDocumentLibraryDDMEditorConfigurationUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureServiceUtil;
import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Adolfo Pérez
 */
public class DLFileEntryAdditionalMetadataSetsDisplayContext {

	public DLFileEntryAdditionalMetadataSetsDisplayContext(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest = httpServletRequest;
	}

	public long getDDMStructureId() throws PortalException {
		return BeanParamUtil.getLong(
			_getDDMStructure(), _httpServletRequest, "structureId");
	}

	public List<com.liferay.dynamic.data.mapping.kernel.DDMStructure>
			getDDMStructures()
		throws PortalException {

		if (_ddmStructures != null) {
			return _ddmStructures;
		}

		DLFileEntryType dlFileEntryType = getDLFileEntryType();

		if (dlFileEntryType == null) {
			_ddmStructures = Collections.emptyList();

			return _ddmStructures;
		}

		DDMStructure ddmStructure = _getDDMStructure();

		if (ddmStructure == null) {
			_ddmStructures = dlFileEntryType.getDDMStructures();
		}
		else {
			_ddmStructures = ListUtil.filter(
				dlFileEntryType.getDDMStructures(),
				currentDDMStructure ->
					currentDDMStructure.getStructureId() !=
						ddmStructure.getStructureId());
		}

		return _ddmStructures;
	}

	public int getDDMStructuresCount() throws PortalException {
		List<com.liferay.dynamic.data.mapping.kernel.DDMStructure>
			ddmStructures = getDDMStructures();

		return ddmStructures.size();
	}

	public DLFileEntryType getDLFileEntryType() throws PortalException {
		if (_dlFileEntryType != null) {
			return _dlFileEntryType;
		}

		if (FFDocumentLibraryDDMEditorConfigurationUtil.useDataEngineEditor()) {
			_dlFileEntryType = DLFileEntryTypeServiceUtil.getFileEntryType(
				ParamUtil.getLong(_httpServletRequest, "fileEntryTypeId"));
		}
		else {
			_dlFileEntryType =
				(DLFileEntryType)_httpServletRequest.getAttribute(
					WebKeys.DOCUMENT_LIBRARY_FILE_ENTRY_TYPE);
		}

		return _dlFileEntryType;
	}

	private DDMStructure _getDDMStructure() throws PortalException {
		if (_ddmStructure != null) {
			return _ddmStructure;
		}

		if (FFDocumentLibraryDDMEditorConfigurationUtil.useDataEngineEditor()) {
			DLFileEntryType dlFileEntryType = getDLFileEntryType();

			if (dlFileEntryType.getDataDefinitionId() == 0) {
				return null;
			}

			_ddmStructure = DDMStructureServiceUtil.getStructure(
				dlFileEntryType.getDataDefinitionId());
		}
		else {
			_ddmStructure = (DDMStructure)_httpServletRequest.getAttribute(
				WebKeys.DOCUMENT_LIBRARY_DYNAMIC_DATA_MAPPING_STRUCTURE);
		}

		return _ddmStructure;
	}

	private DDMStructure _ddmStructure;
	private List<com.liferay.dynamic.data.mapping.kernel.DDMStructure>
		_ddmStructures;
	private DLFileEntryType _dlFileEntryType;
	private final HttpServletRequest _httpServletRequest;

}