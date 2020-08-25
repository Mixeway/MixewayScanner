/*
 * @created  2020-08-18 : 16:55
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.utils;

import java.net.URI;

public final class Constants {
    public static final String DEPENDENCYTRACK_USERNAME = "admin";
    public static final String DEPENDENCYTRACK_PASSWORD = "admin";
    public static final String DEPENDENCYTRACK_URL = "http://localhost:8080";
    public static final String DEPENDENCYTRACK_URL_LOGIN = "/api/v1/user/login";
    public static final String DEPENDENCYTRACK_URL_APIKEY = "/api/v1/team?searchText=&sortOrder=asc";
    public static final String DEPENDENCYTRACK_LOGIN_STRING = "username=admin&password=admin1";
    public static final String DEPENDENCYTRACK_CHANGE_PASSWORD_STRING = "username=admin&password=admin&newPassword=admin1&confirmPassword=admin1";
    public static final String DEPENDENCYTRACK_AUTOMATION = "Automation";
    public static final String DEPENDENCYTRACK_URL_OSS_CONFIG = "/api/v1/configProperty/aggregate";
    public static final String DEPENDENCYTRACK_APIKEY_HEADER = "X-Api-Key";
    public static final String DEPENDENCYTRACK_GET_PROJECTS = "/api/v1/project";
    public static final String DEPENDENCYTRACK_URL_PERMISSIONS = "/api/v1/permission/";
    public static final String GIT_BRANCH_MASTER = "master";
    public static final String DEPENDENCYTRACK_URL_CHANGE_PASSWORD = "/api/v1/user/forceChangePassword";
    public static final String DEPENDENCYTRACK_URL_UPLOAD_BOM = "/api/v1/bom";

    public static final String DEPENDENCYTRACK_URL_VULNS = "/api/v1/vulnerability/project/";
    public static final String STANDALONE_DEFAULT_SOURCE_PATH = "/Users/gs/Documents/workspace/MixewayScanner/mixeway_scan_sources/text";
}
