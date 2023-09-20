package com.xpinjection.bootadmin.security;

import com.xpinjection.bootadmin.config.ActuatorProperties;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties({
        WebEndpointProperties.class,
        SecurityProperties.class,
        ActuatorProperties.class
})
public class AdminSecurityConfiguration {
    private static final int TOKEN_VALIDITY_SECONDS = 1_209_600;
    private static final String ACTUATOR_ROLE = "ACTUATOR";
    private static final String ADMIN_ROLE = "ADMIN";

    private final SecurityProperties securityProperties;
    private final AdminServerProperties adminServer;
    private final WebEndpointProperties webEndpointProperties;
    private final ActuatorProperties actuatorProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setTargetUrlParameter("redirectTo");
        handler.setDefaultTargetUrl(adminServer.path("/"));

        var actuatorBasePath = webEndpointProperties.getBasePath();
        var loginPath = adminServer.path("/login");
        http
                .authorizeHttpRequests(requests ->
                        requests.requestMatchers(adminServer.path("/assets/**")).permitAll()
                                .requestMatchers(loginPath).permitAll()
                                .requestMatchers(actuatorBasePath.concat("/health/**")).permitAll()
                                .requestMatchers(actuatorBasePath.concat("/info/**")).permitAll()
                                .requestMatchers(actuatorBasePath.concat("/**")).hasAnyRole(ACTUATOR_ROLE, ADMIN_ROLE)
                                .requestMatchers(adminServer.path("/**")).hasAnyRole(ADMIN_ROLE)
                                .anyRequest().authenticated())
                .formLogin(form -> form.loginPage(loginPath)
                        .successHandler(handler))
                .logout(logout -> logout.logoutUrl(adminServer.path("/logout")))
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .rememberMe(rememberMe -> rememberMe.key(UUID.randomUUID().toString())
                        .tokenValiditySeconds(TOKEN_VALIDITY_SECONDS));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        var admin = User.withUsername(securityProperties.getUser().getName())
                .password(encoder.encode(securityProperties.getUser().getPassword()))
                .roles(securityProperties.getUser().getRoles().toArray(String[]::new))
                .build();
        var actuator = User.withUsername(actuatorProperties.getUsername())
                .password(encoder.encode(actuatorProperties.getPassword()))
                .roles(ACTUATOR_ROLE)
                .build();
        return new InMemoryUserDetailsManager(admin, actuator);
    }
}
