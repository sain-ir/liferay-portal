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

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import React from 'react';

import {EVENT_TYPES, usePage} from '../hooks/usePage.es';

export const PaginationControls = ({
	activePage,
	readOnly,
	showSubmitButton,
	strings = null,
	submitLabel,
	total,
}) => {
	const {dispatch, store} = usePage();
	const {cancelLabel, redirectURL, showCancelButton} = store;

	return (
		<div className="lfr-ddm-form-pagination-controls">
			{activePage > 0 && (
				<ClayButton
					className="lfr-ddm-form-pagination-prev"
					onClick={() =>
						dispatch({type: EVENT_TYPES.PAGINATION_PREVIOUS})
					}
					type="button"
				>
					<ClayIcon symbol="angle-left" />
					{strings !== null
						? strings['previous']
						: Liferay.Language.get('previous')}
				</ClayButton>
			)}

			{activePage < total - 1 && (
				<ClayButton
					className="float-right lfr-ddm-form-pagination-next"
					onClick={() =>
						dispatch({type: EVENT_TYPES.PAGINATION_NEXT})
					}
					type="button"
				>
					{strings !== null
						? strings['next']
						: Liferay.Language.get('next')}
					<ClayIcon symbol="angle-right" />
				</ClayButton>
			)}

			{activePage === total - 1 && !readOnly && showSubmitButton && (
				<ClayButton
					className="float-right lfr-ddm-form-submit"
					type="submit"
				>
					{submitLabel}
				</ClayButton>
			)}

			{showCancelButton && !readOnly && (
				<div className="ddm-btn-cancel float-right">
					<a
						className="btn btn-cancel btn-secondary"
						href={redirectURL}
					>
						{cancelLabel}
					</a>
				</div>
			)}
		</div>
	);
};
