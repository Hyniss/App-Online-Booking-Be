package com.fpt.h2s.utilities;

import ananta.utility.StringEx;
import com.fpt.h2s.models.exceptions.ApiException;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class MoreRequests {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String CURL_LINE_SEPARATOR = " \\\n";
    private static final String VALUE_FORMAT = "'%s'";

    /**
     * Create query for url from map of keys and values.
     */
    public static String createQuery(@Nullable final Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        return map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }

    public static HttpServletRequest getCurrentHttpRequest() {
        return Optional
            .ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .orElseThrow(() -> ApiException.notFound("Can not find request."));
    }

    public static HandlerMethod getCallingMethod(@NonNull final HttpServletRequest request) throws ClassCastException {
        return (HandlerMethod) request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
    }

    public static Map<String, String[]> getRequestParams() {
        final HttpServletRequest request = MoreRequests.getCurrentHttpRequest();
        return Collections
            .list(request.getParameterNames())
            .stream()
            .collect(ImmutableCollectors.toMapWithValue(request::getParameterValues));
    }

    public static Map<String, String> getPathVariablesOf(@NonNull final HttpServletRequest request) {
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    public static boolean isUrlMatchAnyOf(@NonNull final Collection<String> urls, final String pathToCheck) {
        return urls.stream().anyMatch(url -> MoreRequests.PATH_MATCHER.match(url, pathToCheck));
    }

    public static String getUrlOf(final @NonNull HttpServletRequest request) {
        final String scheme = request.getScheme(); // http
        final String serverName = request.getServerName(); // hostname.com
        final int serverPort = request.getServerPort(); // 80
        final String contextPath = request.getContextPath(); // /mywebapp
        final String servletPath = request.getServletPath(); // /servlet/MyServlet
        final String pathInfo = request.getPathInfo(); // /a/b;c=123
        final String queryString = request.getQueryString(); // d=789

        // Reconstruct original requesting URL
        final StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath).append(servletPath);

        if (pathInfo != null) {
            url.append(pathInfo);
        }
        if (queryString != null) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    public static String getIPAddress(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getLocalAddr());
    }

    public static String getIPAddress() {
        return MoreRequests.getIPAddress(MoreRequests.getCurrentHttpRequest());
    }

    public static String getCurlOf(final ContentCachingRequestWrapper request) {
        final ArrayList<String> curlLines = Lists.newArrayList();

        final String urlKey = String.format("curl --location --request %s", request.getMethod());
        curlLines.add(concat(urlKey, getValueOf(MoreRequests.getUrlOf(request))));

        final List<String> headers = Lists.newArrayList(request.getHeaderNames().asIterator());

        final List<String> headerLines = headers.stream()
            .map(header -> concat("--header", getValueOf(concat(header, ": ", request.getHeader(header)))))
            .toList();

        curlLines.addAll(headerLines);

        final String body = getValueOf(getBodyOf(request));
        curlLines.add(concat("--data-raw", body));

        return StringEx.join(CURL_LINE_SEPARATOR, curlLines);
    }

    private static String getValueOf(final String value) {
        return Strings.isBlank(value) ? null : String.format(VALUE_FORMAT, value);
    }

    private static @Nullable String concat(final String key, final String separator, final String value) {
        return Strings.isBlank(value) ? null : String.format("%s%s %s", key, separator, value);
    }

    private static @Nullable String concat(final String key, final String value) {
        return concat(key, "", value);
    }

    @Contract("_ -> new")
    private static @NotNull String getBodyOf(@NotNull final ContentCachingRequestWrapper request) {
        return new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
    }
}
