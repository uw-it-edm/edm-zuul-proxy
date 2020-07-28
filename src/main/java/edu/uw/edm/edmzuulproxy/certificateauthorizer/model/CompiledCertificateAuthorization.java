package edu.uw.edm.edmzuulproxy.certificateauthorizer.model;

import java.util.List;
import java.util.regex.Pattern;

import lombok.Builder;
import lombok.Data;

/**
 * @author Maxime Deravet Date: 2019-01-30
 */
@Data
@Builder
public class CompiledCertificateAuthorization {
    private String certificateName;
    private Pattern uriRegex;
    private List<String> httpMethods;
    private List<String> uwGroups;
    private List<String> authorizedProfiles;
}
