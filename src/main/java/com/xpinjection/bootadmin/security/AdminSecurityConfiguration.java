package com.xpinjection.bootadmin.security;

import com.xpinjection.bootadmin.config.ActuatorProperties;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties({
        WebEndpointProperties.class,
        SecurityProperties.class,
        ActuatorProperties.class
})
public class AdminSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static final int TOKEN_VALIDITY_SECONDS = 1_209_600;
    private static final String ACTUATOR_ROLE = "ACTUATOR";
    private static final String ADMIN_ROLE = "ADMIN";

    private final SecurityProperties securityProperties;
    private final AdminServerProperties adminServer;
    private final WebEndpointProperties webEndpointProperties;
    private final ActuatorProperties actuatorProperties;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        var handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setTargetUrlParameter("redirectTo");
        handler.setDefaultTargetUrl(adminServer.path("/"));

        var actuatorBasePath = webEndpointProperties.getBasePath();
        var loginPath = adminServer.path("/login");
        http
                .authorizeRequests()
                    .antMatchers(adminServer.path("/assets/**")).permitAll()
                    .antMatchers(loginPath).permitAll()
                    .antMatchers(actuatorBasePath.concat("/health/**")).permitAll()
                    .antMatchers(actuatorBasePath.concat("/info/**")).permitAll()
                    .antMatchers(actuatorBasePath.concat("/**")).hasAnyRole(ACTUATOR_ROLE, ADMIN_ROLE)
                    .antMatchers(adminServer.path("/**")).hasAnyRole(ADMIN_ROLE)
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage(loginPath)
                    .successHandler(handler)
                    .and()
                .logout()
                    .logoutUrl(adminServer.path("/logout"))
                    .and()
                .httpBasic()
                    .and()
                .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                            new AntPathRequestMatcher(adminServer.path("/instances"), HttpMethod.POST.toString()),
                            new AntPathRequestMatcher(adminServer.path("/instances/*"), HttpMethod.DELETE.toString()),
                            new AntPathRequestMatcher(actuatorBasePath.concat("/**"))
                    )
                    .and()
                .rememberMe()
                    .key(UUID.randomUUID().toString())
                    .tokenValiditySeconds(TOKEN_VALIDITY_SECONDS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        var encoder = new BCryptPasswordEncoder();
        auth.inMemoryAuthentication()
                .passwordEncoder(encoder)
                .withUser(securityProperties.getUser().getName())
                .password(encoder.encode(securityProperties.getUser().getPassword()))
                .roles(securityProperties.getUser().getRoles().toArray(String[]::new))
            .and()
                .passwordEncoder(encoder)
                .withUser(actuatorProperties.getUsername())
                .password(encoder.encode(actuatorProperties.getPassword()))
                .roles(ACTUATOR_ROLE);
    }
}
