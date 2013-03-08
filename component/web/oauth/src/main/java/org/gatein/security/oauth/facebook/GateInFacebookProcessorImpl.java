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

package org.gatein.security.oauth.facebook;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.gatein.common.exception.GateInException;
import org.gatein.common.exception.GateInExceptionConstants;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;
import org.picketlink.social.standalone.fb.FacebookPrincipal;
import org.picketlink.social.standalone.fb.FacebookProcessor;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GateInFacebookProcessorImpl implements GateInFacebookProcessor {

    private static Logger log = LoggerFactory.getLogger(GateInFacebookProcessorImpl.class);

    private FacebookProcessor facebookProcessor;

    public GateInFacebookProcessorImpl(ExoContainerContext context, InitParams params) {
        String appid = params.getValueParam("appid").getValue();
        String appsecret = params.getValueParam("appsecret").getValue();
        String scope = params.getValueParam("scope").getValue();
        String redirectURL = params.getValueParam("redirectUrl").getValue();

        if (appid == null || appid.length() == 0 || appid.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'appid' of FacebookFilter needs to be provided. The value should be " +
                    "appId (clientId) of your Facebook application");
        }

        if (appsecret == null || appsecret.length() == 0 || appsecret.trim().equals("<<to be replaced>>")) {
            throw new IllegalArgumentException("Property 'appsecret' of FacebookFilter needs to be provided. The value should be " +
                    "appSecret (clientSecret) of your Facebook application");
        }

        if (scope == null || scope.length() == 0) {
            scope = "email";
        }

        if (redirectURL == null || redirectURL.length() == 0) {
            redirectURL = "http://localhost:8080/" + context.getName() + "/facebookAuth";
        }  else {
            redirectURL = redirectURL.replaceAll(AbstractSSOInterceptor.PORTAL_CONTAINER_SUBSTITUTION_PATTERN, context.getName());
        }

        log.debug("configuration: appid=" + appid +
                ", appsecret=" + appsecret +
                ", scope=" + scope +
                ", redirectURL=" + redirectURL);

        // Use empty rolesList because we don't need rolesList for GateIn integration
        facebookProcessor = new FacebookProcessor(appid, appsecret, scope, redirectURL, Arrays.asList(new String[]{}));
    }

    @Override
    public void initialInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        facebookProcessor.initialInteraction(httpRequest, httpResponse);
    }

    @Override
    public void handleAuthStage(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        facebookProcessor.handleAuthStage(httpRequest, httpResponse);
    }

    @Override
    public FacebookPrincipal getPrincipal(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return (FacebookPrincipal)facebookProcessor.handleAuthenticationResponse(httpRequest, httpResponse);
    }

    @Override
    public FacebookInteractionState processFacebookInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        HttpSession session = httpRequest.getSession();
        String state = (String) session.getAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);

        if (log.isTraceEnabled()) {
            log.trace("state=" + state);
        }

        // Very initial request to portal
        if (state == null || state.isEmpty()) {
            facebookProcessor.initialInteraction(httpRequest, httpResponse);
            state = (String) session.getAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);
            return new FacebookInteractionState(state, null);
        }

        // We have sent an auth request
        if (state.equals(FacebookProcessor.STATES.AUTH.name())) {
            facebookProcessor.handleAuthStage(httpRequest, httpResponse);
            state = (String) session.getAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE);
            return new FacebookInteractionState(state, null);
        }

        // Finish OAuth handshake
        if (state.equals(FacebookProcessor.STATES.AUTHZ.name())) {
            FacebookPrincipal principal = (FacebookPrincipal)facebookProcessor.handleAuthenticationResponse(httpRequest, httpResponse);

            if (principal == null) {
                throw new GateInException(GateInExceptionConstants.EXCEPTION_CODE_OAUTH_UNSPECIFIED, null, "Principal was null. Maybe login modules need to be configured properly.");
            } else {
                state = FacebookProcessor.STATES.FINISH.name();
                httpRequest.getSession().setAttribute(FacebookProcessor.FB_AUTH_STATE_SESSION_ATTRIBUTE, state);
                return new FacebookInteractionState(state, principal);
            }
        }

        // Likely shouldn't happen...
        return new FacebookInteractionState(state, null);
    }
}
