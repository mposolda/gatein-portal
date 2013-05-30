/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

package org.exoplatform.portal.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.ServletContainerFactory;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class PortalLogoutLifecycle implements ApplicationLifecycle<WebuiRequestContext> {

    private static final Logger log = LoggerFactory.getLogger(PortalLogoutLifecycle.class);

    public void onInit(Application app) throws Exception {
    }

    public void onStartRequest(Application app, WebuiRequestContext context) throws Exception {
        LogoutControl.cancelLogout();
    }

    public void onFailRequest(Application app, WebuiRequestContext context, RequestFailure failureType) {
    }

    public void onEndRequest(Application app, WebuiRequestContext context) throws Exception {
        if (LogoutControl.isLogoutRequired()) {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            HttpServletRequest request = prContext.getRequest();
            HttpServletResponse response = prContext.getResponse();

            if (request.getRemoteUser() != null) {
                ServletContainerFactory.getServletContainer().logout(request, response);
            }
        }
    }

    public void onDestroy(Application app) throws Exception {
    }
}
