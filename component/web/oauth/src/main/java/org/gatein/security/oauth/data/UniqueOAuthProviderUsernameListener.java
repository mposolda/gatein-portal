/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.security.oauth.data;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.gatein.common.exception.GateInException;
import org.gatein.common.exception.GateInExceptionConstants;
import org.gatein.security.oauth.generic.OAuthProviderType;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UniqueOAuthProviderUsernameListener extends UserProfileEventListener {

    private final SocialNetworkService socialNetworkService;

    public UniqueOAuthProviderUsernameListener(InitParams params, SocialNetworkService socialNetworkService) {
        this.socialNetworkService = socialNetworkService;
    }

    @Override
    public void preSave(UserProfile user, boolean isNew) throws Exception {
        for (OAuthProviderType opt : OAuthProviderType.values()) {
            String oauthProviderUsername = user.getAttribute(opt.getUserNameAttrName());

            if (oauthProviderUsername == null) {
                continue;
            }

            User foundUser = socialNetworkService.findUserByOAuthProviderUsername(opt, oauthProviderUsername);
            if (foundUser != null && !user.getUserName().equals(foundUser.getUserName())) {
                String message = "Attempt to save " + opt.getUserNameAttrName() + " with value " + oauthProviderUsername +
                        " but it already exists. currentUser=" + user.getUserName() + ", userWithThisOAuthUsername=" + foundUser.getUserName();
                Map<String, Object> exceptionAttribs = new HashMap<String, Object>();
                exceptionAttribs.put(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME, opt.getUserNameAttrName());
                exceptionAttribs.put(GateInExceptionConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME, oauthProviderUsername);

                throw new GateInException(GateInExceptionConstants.EXCEPTION_CODE_DUPLICATE_OAUTH_PROVIDER_USERNAME, exceptionAttribs, message);
            }
        }
    }
}
