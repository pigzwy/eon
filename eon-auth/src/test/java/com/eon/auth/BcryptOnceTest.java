package com.eon.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptOnceTest {
    public static void main(String[] args) {
        var pe = new BCryptPasswordEncoder(10); // 10是strength，和你现有哈希里的$2a$10$一致
        String raw = "admin123";
        String hash = pe.encode(raw);
        System.out.println(hash);
        // 验证一下
        System.out.println(pe.matches(raw, hash)); // 期望 true
    }
}
