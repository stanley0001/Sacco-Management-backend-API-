package com.example.demo.system.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {
        @GetMapping(value = "/welcome")
        public String welcome() {
            return "index.html";
        }
}
