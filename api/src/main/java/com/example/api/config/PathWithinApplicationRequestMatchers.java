package com.example.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.UrlPathHelper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * PathPattern / 단일 경로 해석이 어긋날 때를 대비해
 * (1) requestURI - contextPath (2) UrlPathHelper 둘 다 검사한다.
 */
public final class PathWithinApplicationRequestMatchers {

    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

    static {
        PATH_HELPER.setUrlDecode(true);
        PATH_HELPER.setRemoveSemicolonContent(true);
    }

    private static final Set<String> FULLY_PUBLIC_PATHS = Set.of(
            "/users/signup",
            "/users/login",
            "/users/email-verify",
            "/users/reset-password",
            "/users/reset-password/email-verify",
            "/users/reset-password/change-password",
            "/admin/register"
    );

    private PathWithinApplicationRequestMatchers() {
    }

    /** JWT 없이 통과시킬 경로 — 별도 SecurityFilterChain 의 securityMatcher 용 */
    public static RequestMatcher fullyPublicPathMatcher() {
        return request -> {
            String a = pathFromServletApi(request);
            String b = normalize(PATH_HELPER.getPathWithinApplication(request));
            return FULLY_PUBLIC_PATHS.contains(a) || FULLY_PUBLIC_PATHS.contains(b);
        };
    }

    public static RequestMatcher matching(HttpMethod method, String... pathPatterns) {
        Set<String> expected = new LinkedHashSet<>();
        for (String p : pathPatterns) {
            expected.add(normalize(p));
        }
        return request -> {
            if (!method.name().equalsIgnoreCase(request.getMethod())) {
                return false;
            }
            String a = pathFromServletApi(request);
            String b = normalize(PATH_HELPER.getPathWithinApplication(request));
            return expected.contains(a) || expected.contains(b);
        };
    }

    private static String pathFromServletApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return "/";
        }
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        int semi = uri.indexOf(';');
        if (semi >= 0) {
            uri = uri.substring(0, semi);
        }
        try {
            uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // keep raw
        }
        return normalize(uri);
    }

    private static String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String p = path;
        if (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }
}
