package com.gooddata.qa.graphene.lcmconsole;

public final class NamingConstants {
    public static final String DOMAIN_ID_1 = "data-admin-test1";
    public static final String DOMAIN_ID_2 = "data-admin-test2";
    public static final String DOMAIN_ID_3 = "data-admin-test3";

    public static final String ADMIN_LOGIN_SUFFIX = "admin@gooddata.com";

    public static String getAdminLogin(String domainId) {
        return domainId + ADMIN_LOGIN_SUFFIX;
    }
}
