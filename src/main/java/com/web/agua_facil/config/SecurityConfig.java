package com.web.agua_facil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.web.agua_facil.services.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, CustomAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

            .authorizeHttpRequests(auth -> auth
                //QUALQUER acesso no sistema. 
                //.anyRequest().permitAll()
            	
            		// 1. Acesso Público
                    .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/styles.css").permitAll()
                    
                    // 2. Acesso Exclusivo do Cliente (Portal)
                    .requestMatchers("/portal/**").hasRole("CLIENTE")

                    // --- A REGRA ESPECÍFICA DEVE VIR PRIMEIRO ---
                    // 3. Libera o histórico do imóvel para Funcionários e Clientes
                    .requestMatchers("/property/*/history").hasAnyRole("FUNCIONARIO", "CLIENTE")
                    
                    // 4. Acesso Administrativo (Funcionários) - O curinga /** vem depois!
                    .requestMatchers("/user/**", "/client/**", "/property/**", "/service/**", "/tariff/**", "/bill/**").hasRole("FUNCIONARIO")
                    
                    // 5. Acesso Compartilhado (Leitor e Funcionário)
                    .requestMatchers("/reading/**").hasAnyRole("FUNCIONARIO", "LEITOR")
                    
                    // 6. Dashboard / Home / Index
                    .requestMatchers("/", "/index").authenticated()
                    
                    // 7. Rede de segurança para rotas esquecidas
                    .anyRequest().authenticated()
                
            )

            //.csrf(csrf -> csrf.disable()) Pra qualquer acesso ao sistema

            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)    
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}