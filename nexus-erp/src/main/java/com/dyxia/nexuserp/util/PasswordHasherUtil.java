package com.dyxia.nexuserp.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherUtil implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    // Injection automatique du bean BCrypt défini dans SecurityConfig
    public PasswordHasherUtil(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==================================================");
        System.out.println("GÉNÉRATION DES MOTS DE PASSE DE TEST (BCRYPT) :");
        System.out.println("Mot de passe 'admin123' haché : " + passwordEncoder.encode("admin123"));
        System.out.println("Mot de passe 'employee123' haché : " + passwordEncoder.encode("employee123"));
        System.out.println("==================================================");
    }
}