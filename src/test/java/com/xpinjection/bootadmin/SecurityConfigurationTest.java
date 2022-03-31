package com.xpinjection.bootadmin;

import com.xpinjection.bootadmin.config.ActuatorProperties;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.Base64;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SecurityConfigurationTest {
    @LocalServerPort
    protected int port;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private ActuatorProperties actuatorProperties;

    private String admin;
    private String actuator;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        admin = securityProperties.getUser().getName() + ":" + securityProperties.getUser().getPassword();
        actuator = actuatorProperties.getUsername() + ":" + actuatorProperties.getPassword();
    }

    @Test
    public void springBootAdminEndpointsAreNotAccessibleWithoutAuthorization() {
        verifyPathAccess("/applications", HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void springBootAdminEndpointsAreAccessibleUnderAdminRole() {
        verifyPathAccessUnderUser("/applications", admin, HttpStatus.SC_OK);
    }

    @Test
    public void springBootAdminEndpointsAreNotAccessibleUnderActuatorRole() {
        verifyPathAccessUnderUser("/applications", actuator, HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void actuatorEndpointsAreNotAccessibleWithoutAuthorization() {
        verifyPathAccess("/admin/env", HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void actuatorEndpointsAreAccessibleUnderActuatorRole() {
        verifyPathAccessUnderUser("/admin/env", actuator, HttpStatus.SC_OK);
    }

    @Test
    public void actuatorEndpointsAreAccessibleUnderAdminRole() {
        verifyPathAccessUnderUser("/admin/env", admin, HttpStatus.SC_OK);
    }

    @Test
    public void healthAndInfoActuatorEndpointsAreOpenWithoutAuthorization() {
        verifyPathAccess("/admin/health", HttpStatus.SC_OK);
        verifyPathAccess("/admin/health/livenessState", HttpStatus.SC_OK);
        verifyPathAccess("/admin/info", HttpStatus.SC_OK);
    }

    private void verifyPathAccess(String path, int expectedStatusCode) {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(URI.create(path))
        .then()
            .log().all()
            .statusCode(expectedStatusCode);
    }

    private void verifyPathAccessUnderUser(String path, String credentials, int expectedStatusCode) {
        given()
            .accept(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, "Basic ".concat(Base64.getEncoder()
                    .encodeToString(credentials.getBytes())))
        .when()
            .get(URI.create(path))
        .then()
            .log().all()
            .statusCode(expectedStatusCode);
    }
}
