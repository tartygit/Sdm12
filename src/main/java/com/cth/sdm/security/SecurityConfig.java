package com.cth.sdm.security;

import com.cth.sdm.model.SdmUser;
import com.cth.sdm.repository.SdmUserRepository;
import com.cth.sdm.service.SdmConfigService;
import com.cth.sdm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SdmUserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SdmConfigService sdmConfigService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/forgot-password", "/reset-password", "/h2-console/**", "/css/**", "/js/**", "/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    userService.recordFailedAttempt(username);
                    request.getSession().setAttribute("loginError", exception.getMessage());
                    response.sendRedirect("/login?error=true");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // For H2 console

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();

                String currentStrategy = sdmConfigService.getAuthStrategy();

                // 1. Dynamic check for LDAP config
                if ("LDAP".equalsIgnoreCase(currentStrategy)) {
                    // For demo, standard Windows AD LDAP mock authentication
                    if (username.startsWith("ldap_") && "LdapPass@123".equals(password)) {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_MAKER"));
                        return new UsernamePasswordAuthenticationToken(username, password, authorities);
                    } else if ("ldapadmin".equalsIgnoreCase(username) && "LdapPass@123".equals(password)) {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        return new UsernamePasswordAuthenticationToken(username, password, authorities);
                    } else {
                        throw new BadCredentialsException("LDAP Active Directory authentication failed.");
                    }
                }

                // 2. Default Database authentication
                Optional<SdmUser> userOpt = userRepository.findById(username);
                if (userOpt.isEmpty()) {
                    throw new BadCredentialsException("Invalid username or password.");
                }

                SdmUser user = userOpt.get();
                if (user.isLocked()) {
                    throw new LockedException("Your account is locked. Please contact your system administrator.");
                }

                if (!passwordEncoder().matches(password, user.getPasswordHash())) {
                    throw new BadCredentialsException("Invalid username or password.");
                }

                // Reset failed attempts on success
                userService.resetFailedAttempts(username);

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

                return new UsernamePasswordAuthenticationToken(username, password, authorities);
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return authentication.equals(UsernamePasswordAuthenticationToken.class);
            }
        };
    }
}
