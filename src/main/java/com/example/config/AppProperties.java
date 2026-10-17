package com.example.config;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.io.FileHandler;

/**
 * Apache Commons Configuration 2 で環境ごとのファイルを読む。
 * プレースホルダは {@code ${ext:名前:-既定値}}。{@code ext} はシステムプロパティ、環境変数の順に見る。
 */
public final class AppProperties {

    private static final String FILE_NAME = "application-" + WebConfig.resolveAppEnv() + ".properties";
    private static final Configuration CONFIGURATION = load(FILE_NAME);

    private AppProperties() {
    }

    public static String fileName() {
        return FILE_NAME;
    }

    public static String get(String key) {
        return CONFIGURATION.getString(key);
    }

    private static Configuration load(String fileName) {
        URL url = AppProperties.class.getClassLoader().getResource(fileName);
        if (url == null) {
            throw new IllegalStateException(fileName + " がクラスパスにありません");
        }
        PropertiesConfiguration config = new PropertiesConfiguration();
        FileHandler handler = new FileHandler(config);
        handler.setEncoding(StandardCharsets.UTF_8.name());
        try {
            handler.load(url);
        } catch (ConfigurationException e) {
            throw new IllegalStateException(fileName + " を読めません", e);
        }
        // ${sys:A:-${env:A:-既定値}} は、sys が解決できると閉じ括弧が1つ残る。
        // そのため接頭辞 ext で、システムプロパティの次に環境変数を見る。
        config.getInterpolator().registerLookup("ext", new SysThenEnvLookup());
        return config;
    }

    private static final class SysThenEnvLookup implements Lookup {
        @Override
        public Object lookup(String key) {
            String value = System.getProperty(key);
            if (value == null || value.isBlank()) {
                value = System.getenv(key);
            }
            return (value == null || value.isBlank()) ? null : value;
        }
    }
}
