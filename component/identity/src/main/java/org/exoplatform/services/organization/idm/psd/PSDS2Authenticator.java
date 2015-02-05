package org.exoplatform.services.organization.idm.psd;

import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;



public class PSDS2Authenticator implements Authenticator {

    private OrganizationService orgService;
    private Authenticator orgAuthenticator;

    public PSDS2Authenticator(ExoContainer container) {
        this.orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
        this.orgAuthenticator = (Authenticator) container.getComponentInstanceOfType(Authenticator.class);
    }

    @Override
    public Identity createIdentity(String username) throws Exception {
        UserHandler userHandler = orgService.getUserHandler();

        // Sync user to portal if he is not already there
        if (userHandler.findUserByName(username) == null) {
            try {
                begin(orgService);
                User user = userHandler.createUserInstance(username);

                // fill data somehow (for example add data from LDAP)
                user.setFirstName("Joe");
                user.setLastName("Doe");
                user.setEmail("joe@email.org");

                userHandler.createUser(user, true);


                // Fill needed memberships. Filling memberships can be also done based on LDAP memberships, but at least member:/platform/users is needed for each authenticated user to see portal data
                MembershipType memberType = orgService.getMembershipTypeHandler().findMembershipType("member");
                Group platformUsersGroup = orgService.getGroupHandler().findGroupById("/platform/users");
                orgService.getMembershipHandler().linkMembership(user, platformUsersGroup, memberType, true);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                end(orgService);
            }
        }

        // Create identity based on organization authenticator. Since we added needed memberships to user with organizationService, the created Identity will have the memberships as well
        Identity identity = orgAuthenticator.createIdentity(username);

        // Add any custom needed JAAS roles. Generally it can be done more properly by extending RolesExtractor component, but adding them manually can work too
        identity.getRoles().add("PSDS2_Authenticated");
        identity.getRoles().add("PSDS2_Event_Live_Editor");

        return identity;

    }

    @Override
    public String validateUser(Credential[] arg0) throws Exception {
        return "xadmin";
    }


    // Newly added method in recent Organization API
    public java.lang.Exception getLastExceptionOnValidateUser() {
        return null;
    }

    private void begin(OrganizationService orgService) {
        if (orgService instanceof ComponentRequestLifecycle) {
            RequestLifeCycle.begin((ComponentRequestLifecycle) orgService);
        }
    }

    private void end(OrganizationService orgService) {
        if (orgService instanceof ComponentRequestLifecycle) {
            RequestLifeCycle.end();
        }
    }


}