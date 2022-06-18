package com.example.demo.system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class WelcomeController {
        @GetMapping(value = "/welcome")
        public String welcome() {
            return "index.html";
        }


}
