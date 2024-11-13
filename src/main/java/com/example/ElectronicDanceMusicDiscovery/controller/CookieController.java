package com.example.ElectronicDanceMusicDiscovery.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class CookieController {

    @GetMapping("/setCookie")
    public String setCookie(@RequestParam String name, @RequestParam String value, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath("/"); // Available for the entire application
        response.addCookie(cookie);

        return "Cookie set";
    }

    @GetMapping("/getCookie")
    public String getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "historyReleases".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .map(value -> {
                        String[] releaseIds = value.split("\\|"); // Split the string into an array
                        return Arrays.toString(releaseIds); // Or further process the array
                    })
                    .orElse("Cookie not found");
        }
        return "Cookie not found";
    }
}
