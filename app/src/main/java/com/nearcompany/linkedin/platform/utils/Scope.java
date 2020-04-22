package com.nearcompany.linkedin.platform.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the types of data for which access is being requested.
 */
public class Scope {

    /**
     * Your Profile Overview 	Name, photo, headline, and current positions
     */
    public static final Scope.LIPermission R_BASICPROFILE = new Scope.LIPermission("r_basicprofile", "Name, photo, headline and current position");

    /**
     * Your Full Profile 	Full profile including experience, education, skills, and recommendations
     */
    public static final Scope.LIPermission R_FULLPROFILE = new Scope.LIPermission("r_fullprofile", "Full profile including experience, education, skills and recommendations");

    /**
     * Your Email Address 	The primary email address you use for your LinkedIn account
     */
    public static final Scope.LIPermission R_EMAILADDRESS = new Scope.LIPermission("r_emailaddress", "Your email address");

    /**
     * Your Contact Info 	Address, phone number, and bound accounts
     */
    public static final Scope.LIPermission R_CONTACTINFO = new Scope.LIPermission("r_contactinfo", "Your contact info");

    /**
     * Company Page & Analytics 	Edit company pages for which I am an Admin and post status updates on behalf of those companies
     */
    public static final Scope.LIPermission RW_COMPANY_ADMIN = new Scope.LIPermission("rw_company_admin", "Manage your company page and post updates");

    /**
     * Share, comment & like    Post updates, make comments and like posts
     */
    public static final Scope.LIPermission W_SHARE = new Scope.LIPermission("w_share", "Post updates, make comments and like posts as you");


    private Set<Scope.LIPermission> permissions = new HashSet<Scope.LIPermission>();

    private Scope(Scope.LIPermission... permissions) {
        if (permissions == null) {
            return;
        }
        for (Scope.LIPermission perm : permissions) {
            this.permissions.add(perm);
        }
    }

    /**
     * build a Scope with the list of desired permissions
     *
     * @param permissions
     * @return
     */
    public static synchronized Scope build(Scope.LIPermission... permissions) {
        return new Scope(permissions);
    }

    private static String join(CharSequence delimiter, Collection<Scope.LIPermission> tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Scope.LIPermission token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token.name);
        }
        return sb.toString();
    }

    public String createScope() {
        return join(" ", permissions);
    }

    @Override
    public String toString() {
        return this.createScope();
    }

    public static class LIPermission {
        private final String name;
        private final String description;

        public LIPermission(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }
    }

}
