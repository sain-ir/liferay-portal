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

package com.liferay.layout.content.page.editor.web.internal.sidebar.panel;

import com.liferay.layout.content.page.editor.sidebar.panel.ContentPageEditorSidebarPanel;
import com.liferay.layout.content.page.editor.web.internal.configuration.FFLayoutContentPageEditorConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.permission.LayoutPermissionUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Eudaldo Alonso
 */
@Component(
	configurationPid = "com.liferay.layout.content.page.editor.web.internal.configuration.FFLayoutContentPageEditorConfiguration",
	immediate = true, property = "service.ranking:Integer=800",
	service = ContentPageEditorSidebarPanel.class
)
public class FragmentsContentPageEditorSidebarPanel
	implements ContentPageEditorSidebarPanel {

	@Override
	public String getIcon() {
		return "cards2";
	}

	@Override
	public String getId() {
		return "fragments-widgets";
	}

	@Override
	public String getLabel(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		return LanguageUtil.get(resourceBundle, "fragments-and-widgets");
	}

	@Override
	public boolean isVisible(
		PermissionChecker permissionChecker, long plid,
		boolean pageIsDisplayPage) {

		try {
			if (LayoutPermissionUtil.contains(
					permissionChecker, plid, ActionKeys.UPDATE)) {

				return true;
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception, exception);
			}
		}

		return false;
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_ffLayoutContentPageEditorConfiguration =
			ConfigurableUtil.createConfigurable(
				FFLayoutContentPageEditorConfiguration.class, properties);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentsContentPageEditorSidebarPanel.class);

	private volatile FFLayoutContentPageEditorConfiguration
		_ffLayoutContentPageEditorConfiguration;

}