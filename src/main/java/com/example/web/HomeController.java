package com.example.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${APP_ENV:prod}")
    String appEnv;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appEnv", appEnv);
        model.addAttribute("sysProp", System.getProperty("APP_ENV"));
        model.addAttribute("envVar", System.getenv("APP_ENV"));
        return "home";
    }
}
