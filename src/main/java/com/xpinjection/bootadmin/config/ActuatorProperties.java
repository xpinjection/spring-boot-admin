package com.xpinjection.bootadmin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "actuator")
@Getter
@Setter
@Validated
public class ActuatorProperties {

    @NotBlank
    /*
      Username to access Actuator endpoints.
     */
    private String username;

    @NotBlank
    /*
      Password to access actuator endpoints.
     */
    private String password;
}
