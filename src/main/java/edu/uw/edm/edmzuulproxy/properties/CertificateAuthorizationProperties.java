package edu.uw.edm.edmzuulproxy.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */

@Component
@ConfigurationProperties(prefix = "uw.cert-authentication")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class CertificateAuthorizationProperties {

    @NonNull
    private String certificateNameHeader;
}
