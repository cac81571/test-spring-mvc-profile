package com.example.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Value("${APP_ENV:prod}")
    String appEnv;

    @GetMapping("/")
    public String home(Model model) {
        log.debug("app-env-log-check level=DEBUG appEnv={}", appEnv);
        log.info("app-env-log-check level=INFO appEnv={}", appEnv);
        model.addAttribute("appEnv", appEnv);
        model.addAttribute("sysProp", System.getProperty("APP_ENV"));
        model.addAttribute("envVar", System.getenv("APP_ENV"));
        model.addAttribute("logLevelSys", System.getProperty("LOG_LEVEL"));
        model.addAttribute("logLevelEnv", System.getenv("LOG_LEVEL"));
        model.addAttribute("rootLevelSys", System.getProperty("ROOT_LOG_LEVEL"));
        model.addAttribute("rootLevelEnv", System.getenv("ROOT_LOG_LEVEL"));
        model.addAttribute("debugEnabled", log.isDebugEnabled());
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level rootLevel = root.getLevel();
        model.addAttribute("rootLevel", rootLevel == null ? "" : rootLevel.toString());
        return "home";
    }
}
