package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Класс конфигурации для настройки SpringSecurity в приложении.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan("org.example")
public class WebSecurityConfig {
    /**
     * Сервис информации о пользователе
     */
    private final UserDetailsService userDetailsService;

    /**
     * Конструктор для SecurityConfiguration
     * @param userDetailsService Пользовательский сервис деталей пользователя для аутентификации
     */
    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Конфигурирует цепочку фильтров безопасности для различных HTTP-запросов и формной аутентификации/выхода
     * @param http Конфигурация безопасности HTTP
     * @return Сконфигурированный бин SecurityFilterChain
     * @throws Exception Если происходит ошибка во время конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/tableware/add") // Отключаем CSRF только для этого маршрута
                        .ignoringRequestMatchers("/tableware/search")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Разрешаем доступ к статическим ресурсам
                        .requestMatchers("/sign-up", "/error", "/login", "process_reg", "/process_login").permitAll()
                        //.requestMatchers("/tableware/search").hasAnyRole("USER", "ADMIN") // Разрешаем доступ к поиску для пользователей с ролями USER и ADMIN
                        .requestMatchers("/tableware/add", "/tableware/edit/*", "/tableware/delete/*").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/process_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                );
        return http.build();
    }

    /**
     * Предоставляет бин BCryptPasswordEncoder для кодирования пароля
     * @return Бин BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Конфигурирует менеджер аутентификации с пользовательским сервисом деталей и кодировщиком пароля
     * @param http Конфигурация безопасности HTTP
     * @return Сконфигурированный бин AuthenticationManager
     * @throws Exception Если происходит ошибка во время конфигурации
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
