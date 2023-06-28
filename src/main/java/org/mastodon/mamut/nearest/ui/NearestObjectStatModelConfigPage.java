/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.nearest.ui;

import org.mastodon.mamut.nearest.NearestObjectStatModel;

import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.style.StyleProfile;
import bdv.ui.settings.style.StyleProfileManager;

public class NearestObjectStatModelConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< NearestObjectStatModel > >
{
	/**
	 * Creates a new render-settings config page.
	 *
	 * @param treePath
	 *            path of this page in the settings tree.
	 * @param controller
	 *            the render settings manager.
	 * @param nearestObjectStatModelManager
	 */
	public NearestObjectStatModelConfigPage( final String treePath, final NearestObjectStatController controller, final NearestObjectStatModelManager nearestObjectStatModelManager )
	{
		super(
				treePath,
				new StyleProfileManager<>( new NearestObjectStatModelManager(), new NearestObjectStatModelManager( false ) ),
				controller.getProfileEditor() );
	}
}
