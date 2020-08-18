/*
 * @created  2020-08-18 : 16:55
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.utils;

public final class Constants {
    public static final String DEPENDENCYTRACK_USERNAME = "admin";
    public static final String DEPENDENCYTRACK_PASSWORD = "admin";
    public static final String DEPENDENCYTRACK_URL = "http://localhost:8080";
    public static final String DEPENDENCYTRACK_URL_LOGIN = "/api/v1/user/login";
    public static final String DEPENDENCYTRACK_URL_APIKEY = "/api/v1/team?searchText=&sortOrder=asc";
    public static final String DEPENDENCYTRACK_LOGIN_STRING = "username=admin&password=admin";
    public static final String DEPENDENCYTRACK_AUTOMATION = "Automation";
    public static final String DEPENDENCYTRACK_URL_OSS_CONFIG = "/api/v1/configProperty/aggregate";
    public static final String DEPENDENCYTRACK_APIKEY_HEADER = "X-Api-Key";
    public static final String DEPENDENCYTRACK_GET_PROJECTS = "/api/v1/project";
    public static final String DEPENDENCYTRACK_URL_PERMISSIONS = "/api/v1/permission/";
}
