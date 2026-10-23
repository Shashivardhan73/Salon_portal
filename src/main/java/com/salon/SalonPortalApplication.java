package com.salon;

import com.salon.model.AppUser;
import com.salon.model.Client;
import com.salon.repository.AppUserRepository;
import com.salon.repository.ClientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class SalonPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalonPortalApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/register", "/styles.css").permitAll()
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .requestMatchers("/client/**").hasRole("CLIENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .build();
    }

    @Bean
    CommandLineRunner seedDemoAccounts(
            AppUserRepository users,
            ClientRepository clients,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (users.findByUsername("owner").isEmpty()) {
                users.save(new AppUser("owner", passwordEncoder.encode("owner123"), "OWNER", null));
            }

            if (users.findByUsername("alex@example.com").isEmpty()) {
                Client client = clients.save(new Client("Alex Morgan", "alex@example.com", "555-0100"));
                users.save(new AppUser(
                        client.getEmail(),
                        passwordEncoder.encode("client123"),
                        "CLIENT",
                        client));
            }
        };
    }
}
