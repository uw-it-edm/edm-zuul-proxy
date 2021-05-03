package edu.uw.edm.edmzuulproxy.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Component
@ConfigurationProperties(prefix = "uw.profile.security")
@Data
public class SecurityProperties {
    private static final String DOCFINIY_AUDIT_USER_HEADER = "x-audituser";

    private String keystoreLocation;
    private String keystorePassword;
    private String authenticationHeaderName;

    /**
     * Name of request header to use for authentication when routing to DocFinity server.
     * 'X-AUDITUSER' by default, which is the header that DocFinity expects to use for auditing the
     * actions performed by request.
     */
    private String docfinityAuthenticationHeaderName = DOCFINIY_AUDIT_USER_HEADER;
}
