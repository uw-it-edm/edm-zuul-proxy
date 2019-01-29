package edu.uw.edm.edmzuulproxy.certificateauthorizer.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

import java.io.Serializable;

/**
 * @author Maxime Deravet Date: 2019-01-29
 */
public class CertificateAuthorizationId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String certificateName;
    private String uriRegex;

    @DynamoDBHashKey
    public String getCertificateName() {
        return certificateName;
    }

    @DynamoDBRangeKey
    public String getUriRegex() {
        return uriRegex;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public void setUriRegex(String uriRegex) {
        this.uriRegex = uriRegex;
    }

}
