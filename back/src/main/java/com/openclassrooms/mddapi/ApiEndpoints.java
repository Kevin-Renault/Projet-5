package com.openclassrooms.mddapi;

public final class ApiEndpoints {
    private ApiEndpoints() {
    }

    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String AUTH_REFRESH = "/api/auth/refresh";
    public static final String AUTH_LOGOUT = "/api/auth/logout";
    public static final String AUTH_CSRF = "/api/auth/csrf";
    public static final String AUTH_ME = "/api/auth/me";
    public static final String SUBSCRIPTIONS = "/api/subscriptions";
    public static final String TOPICS = "/api/topics";
    public static final String ARTICLES = "/api/articles";
    public static final String COMMENTS = "/api/comments";

}
