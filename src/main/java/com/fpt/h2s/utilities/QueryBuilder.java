package com.fpt.h2s.utilities;

import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryBuilder {

    private final Map<String, String> paramMap = new TreeMap<>();
    private final Map<String, String> signatureMap = new LinkedHashMap<>();
    private String url;

    private QueryBuilder() {
    }

    public static QueryBuilder builder() {
        return new QueryBuilder();
    }

    public QueryBuilder withUrl(final String url) {
        if (Strings.isBlank(url)) {
            throw new IllegalArgumentException("Url should not be empty.");
        }
        this.url = url;
        return this;
    }

    public QueryBuilder addParams(final Map<String, Object> queryMap) {
        if (queryMap == null) {
            return this;
        }
        queryMap.forEach(this::addParam);
        return this;
    }

    public QueryBuilder addParam(final String key, final Object value) {
        this.paramMap.put(key, Optional.of(value).map(Object::toString).orElse(""));
        return this;
    }

    public QueryBuilder addSignatureParam(final String key, final Object value) {
        this.signatureMap.put(key, Optional.of(value).map(Object::toString).orElse(""));
        return this;
    }

    public QueryBuilder addParamIfNonEmpty(final String key, final Object value) {
        if (Strings.isNotBlank(key) && value != null && Strings.isNotBlank(value.toString())) {
            this.paramMap.put(key, value.toString());
        }
        return this;
    }

    public String build(final Function<String, String> keyProvider, final Function<String, String> valueProvider) {
        final List<Map.Entry<String, String>> params = Stream.concat(this.paramMap.entrySet().stream(), this.signatureMap.entrySet().stream()).toList();
        final String query = params
            .stream()
            .map(param -> keyProvider.apply(param.getKey()) + "=" + valueProvider.apply(param.getValue()))
            .collect(Collectors.joining("&"));
        if (this.url == null) {
            return query;
        }
        return this.url + "?" + query;
    }

    public String build(final Function<String, String> valueProvider) {
        return this.build(k -> k, valueProvider);

    }

    public String build() {
        return this.build(k -> k, v -> v);
    }
}
