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

package org.gatein.web.security.impersonation;

import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter to check if admin user wants to stop impersonation. It's checked by presence of flag in session represented
 * by attribute {@link #ATTR_CANCEL_IMPERSONATION}
 *
 * It should be in filter chain after {@link org.exoplatform.services.security.web.SetCurrentIdentityFilter}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CancelImpersonationFilter extends AbstractFilter {
    private static final Logger log = LoggerFactory.getLogger(CancelImpersonationFilter.class);

    // Flag to indicate that we want to cancel impersonation
    public static final String ATTR_CANCEL_IMPERSONATION = "_cancelImpersonation";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        Identity currentIdentity = ConversationState.getCurrent().getIdentity();
        if (currentIdentity instanceof ImpersonatedIdentity) {
            if (checkCancelImpersonation(httpRequest, httpResponse)) {
                return;
            }
        }

        // Continue with filter chain if we are not in impersonation or if cancel of impersonation wasn't requested
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Check if admin user wants to cancel Impersonation session. Perform redirection to ImpersonationServlet if it's the case
     *
     * @param req servlet request
     * @param resp servlet response
     * @return true if impersonation was requested. In this case, response is already commited and redirected to ImpersonationServlet
     * @throws IOException
     */
    protected boolean checkCancelImpersonation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(ATTR_CANCEL_IMPERSONATION) != null) {
            // Remove flag from session
            session.removeAttribute(ATTR_CANCEL_IMPERSONATION);

            // Redirect to ImpersonationServlet and trigger stop of Impersonation session
            String redirectURI = req.getContextPath() + ImpersonationServlet.IMPERSONATE_URL_SUFIX;

            // Attach params
            redirectURI = new StringBuilder(redirectURI)
                    .append("?")
                    .append(ImpersonationServlet.PARAM_ACTION)
                    .append("=")
                    .append(ImpersonationServlet.PARAM_ACTION_STOP_IMPERSONATION)
                    .toString();

            // Redirect to impersonation servlet
            if (log.isTraceEnabled()) {
                log.trace("Going to logout from impersonation session. Redirecting to: " + redirectURI);
            }
            resp.sendRedirect(redirectURI);

            return true;
        } else {
            return false;
        }
    }
}
