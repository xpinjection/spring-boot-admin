package com.xpinjection.bootadmin.security;

import com.xpinjection.bootadmin.config.ActuatorProperties;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Primary
@RequiredArgsConstructor
public class CustomBasicAuthHttpHeaderProvider extends BasicAuthHttpHeaderProvider {
    private final ActuatorProperties actuatorProperties;

    @Override
    public HttpHeaders getHeaders(Instance instance) {
        var username = actuatorProperties.getUsername();
        var password = actuatorProperties.getPassword();
        var headers = new HttpHeaders();
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            headers.set("Authorization", super.encode(username, password));
        }
        return headers;
    }
}
