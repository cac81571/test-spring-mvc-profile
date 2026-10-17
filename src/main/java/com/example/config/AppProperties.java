package com.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 標準の {@link Properties} で環境ごとのファイルを読む。
 * {@code ${NAME:既定値}} は、システムプロパティ、環境変数、既定値の順で置き換える。
 */
public final class AppProperties {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?}");

    private static final String FILE_NAME = "application-" + WebConfig.resolveAppEnv() + ".properties";
    private static final Properties PROPERTIES = load(FILE_NAME);

    private AppProperties() {
    }

    public static String fileName() {
        return FILE_NAME;
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static Properties load(String fileName) {
        Properties props = new Properties();
        try (InputStream in = AppProperties.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IllegalStateException(fileName + " がクラスパスにありません");
            }
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(fileName + " を読めません", e);
        }
        for (String key : props.stringPropertyNames()) {
            props.setProperty(key, resolvePlaceholders(props.getProperty(key)));
        }
        return props;
    }

    private static String resolvePlaceholders(String value) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder resolved = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String fallback = matcher.group(2);
            String replacement = firstNonBlank(System.getProperty(name), System.getenv(name));
            if (replacement == null) {
                replacement = fallback == null ? matcher.group(0) : fallback;
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
