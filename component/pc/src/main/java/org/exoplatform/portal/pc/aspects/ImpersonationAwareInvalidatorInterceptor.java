package org.exoplatform.portal.pc.aspects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.pc.portlet.aspects.SessionInvalidatorInterceptor;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ImpersonationAwareInvalidatorInterceptor extends SessionInvalidatorInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ImpersonationAwareInvalidatorInterceptor.class);

    public static final String ATTR_IMPERSONATION_STATE = "_impersonationState";
    public static final String STATE_IN_PROGRESS = "_inProgress";
    public static final String STATE_FINISHED = "_finished";

    @Override
    public void check(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String impersonationState = (String)session.getAttribute(ATTR_IMPERSONATION_STATE);

            // We will invoke super.check() just in case that we don't need to care about impersonation
            if (impersonationState != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Skipping check for session " + session.getId() + ", context: " + session.getServletContext().getContextPath());
                }
                return;
            }
        }

        super.check(request);
    }

    @Override
    public void update(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String impersonationState = (String)session.getAttribute(ATTR_IMPERSONATION_STATE);

            // We can remove attribute as impersonation has just been finished in previous request
            if (impersonationState != null && STATE_FINISHED.equals(impersonationState)) {
                if (log.isTraceEnabled()) {
                    log.trace("Removing impersonationState attribute for session " + session.getId() + ", context: " + session.getServletContext().getContextPath());
                }
                session.removeAttribute(ATTR_IMPERSONATION_STATE);
            }
        }

        // Update identity in session
        super.update(request);
    }
}
