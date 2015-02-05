package org.exoplatform.services.organization.idm.psd;

import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;



public class PSDS2Authenticator implements Authenticator {

    public PSDS2Authenticator() {
    }

    @Override
    public Identity createIdentity(String arg0) throws Exception {
        ArrayList<String> jaasRoles = new ArrayList<String>();
        jaasRoles.add("PSDS2_Authenticated");
        jaasRoles.add("PSDS2_Event_Live_Editor");

        ArrayList<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
        memberships.add(new MembershipEntry("member", "/platform/users"));

        return new Identity("xadmin", memberships, jaasRoles);
    }

    @Override
    public String validateUser(Credential[] arg0) throws Exception {
        return "xadmin";
    }


    // Newly added method in recent Organization API
    public java.lang.Exception getLastExceptionOnValidateUser() {
        return null;
    }


}