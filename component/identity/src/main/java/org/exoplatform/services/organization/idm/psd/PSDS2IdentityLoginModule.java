package org.exoplatform.services.organization.idm.psd;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;

public class PSDS2IdentityLoginModule extends org.exoplatform.services.security.jaas.AbstractLoginModule {
    private static final Log LOG = ExoLogger.getLogger(PSDS2IdentityLoginModule.class);
    private Identity identity;

    @Override
    public boolean login() throws LoginException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In login of PSDS2IdentityLoginModule.");
        }

        try {
            if (sharedState.containsKey("exo.security.identity")) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Use Identity from previous LoginModule");
                identity = (Identity)sharedState.get("exo.security.identity");
            } else {
                if (LOG.isDebugEnabled())
                    LOG.debug("Try create identity");
                Callback[] callbacks = new Callback[2];
                callbacks[0] = new NameCallback("Username");
                callbacks[1] = new PasswordCallback("Password", false);

                callbackHandler.handle(callbacks);
                String username = ((NameCallback)callbacks[0]).getName();
                String password = new String(((PasswordCallback)callbacks[1]).getPassword());
                ((PasswordCallback)callbacks[1]).clearPassword();
                if (username == null || password == null) {
                    return false;
                }

                Authenticator authenticator = new PSDS2Authenticator(getContainer());

                Credential[] credentials =
                        new Credential[]{new UsernameCredential(username), new PasswordCredential(password)};

                String userId = authenticator.validateUser(credentials);

                identity = authenticator.createIdentity(userId);
                sharedState.put("javax.security.auth.login.name", userId);
                subject.getPrivateCredentials().add(password);
                subject.getPublicCredentials().add(new UsernameCredential(username));
            }
            return true;

        } catch (final Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }

            if (LOG.isWarnEnabled()) {
                LOG.warn(e.getMessage());
            }

            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        try {
            IdentityRegistry identityRegistry =
                    (IdentityRegistry)getContainer().getComponentInstanceOfType(IdentityRegistry.class);

            if (identityRegistry.getIdentity(identity.getUserId()) != null) {
                throw new LoginException("User " + identity.getUserId() + " already logined.");
            }

            // Do not need implement logout by self if use tomcat 6.0.21 and later.
            // See deprecation comments in
            // org.exoplatform.services.security.web.JAASConversationStateListener
            identity.setSubject(subject);
            identityRegistry.register(identity);

        } catch (final Exception e) {
            LOG.error(e.getLocalizedMessage());
            throw new LoginException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Log getLogger() {
        return LOG;
    }
}