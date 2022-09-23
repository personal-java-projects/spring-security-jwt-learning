package com.security.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    public static PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }
}
