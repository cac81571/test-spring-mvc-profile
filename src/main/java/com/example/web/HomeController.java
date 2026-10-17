package com.example.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.config.AppProperties;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Value("${APP_ENV:prod}")
    String appEnv;

    @Value("${app.profile.label}")
    String profileLabel;

    @Value("${app.message}")
    String appMessage;

    final String jdkFile = AppProperties.fileName();
    final String jdkProfileLabel = AppProperties.get("app.profile.label");
    final String jdkAppMessage = AppProperties.get("app.message");

    @GetMapping("/")
    public String home(Model model) {
        log.debug("app-env-log-check level=DEBUG appEnv={} profileLabel={} appMessage={} jdkFile={} jdkProfileLabel={} jdkAppMessage={}",
                appEnv, profileLabel, appMessage, jdkFile, jdkProfileLabel, jdkAppMessage);
        log.info("app-env-log-check level=INFO appEnv={} profileLabel={} appMessage={} jdkFile={} jdkProfileLabel={} jdkAppMessage={}",
                appEnv, profileLabel, appMessage, jdkFile, jdkProfileLabel, jdkAppMessage);
        model.addAttribute("appEnv", appEnv);
        model.addAttribute("profileLabel", profileLabel);
        model.addAttribute("appMessage", appMessage);
        model.addAttribute("jdkFile", jdkFile);
        model.addAttribute("jdkProfileLabel", jdkProfileLabel);
        model.addAttribute("jdkAppMessage", jdkAppMessage);
        model.addAttribute("sysProp", System.getProperty("APP_ENV"));
        model.addAttribute("envVar", System.getenv("APP_ENV"));
        model.addAttribute("messageSysProp", System.getProperty("APP_MESSAGE"));
        model.addAttribute("messageEnvVar", System.getenv("APP_MESSAGE"));
        model.addAttribute("debugEnabled", log.isDebugEnabled());
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        model.addAttribute("rootLevel", root.getLevel() == null ? "" : root.getLevel().toString());
        return "home";
    }
}
