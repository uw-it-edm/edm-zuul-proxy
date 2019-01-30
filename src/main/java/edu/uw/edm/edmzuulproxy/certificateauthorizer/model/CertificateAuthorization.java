package edu.uw.edm.edmzuulproxy.certificateauthorizer.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CertificateAuthorization.TABLE_NAME;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
@DynamoDBTable(tableName = TABLE_NAME)

@NoArgsConstructor
@AllArgsConstructor
public class CertificateAuthorization {

    public static final String TABLE_NAME = "edm-zuul-proxy-certificate-authorization";

    @Id
    private CertificateAuthorizationId certificateAuthorizationId;

    @DynamoDBHashKey(attributeName = "certificateName")
    public String getCertificateName() {
        return certificateAuthorizationId != null ? certificateAuthorizationId.getCertificateName() : null;
    }

    public void setCertificateName(String certificateName) {
        if (certificateAuthorizationId == null) {
            certificateAuthorizationId = new CertificateAuthorizationId();
        }
        certificateAuthorizationId.setCertificateName(certificateName);
    }


    @DynamoDBRangeKey(attributeName = "methodAndURI")
    public String getMethodAndURI() {
        return certificateAuthorizationId != null ? certificateAuthorizationId.getMethodAndURI() : null;

    }

    public void setMethodAndURI(String methodAndURI) {
        if (certificateAuthorizationId == null) {
            certificateAuthorizationId = new CertificateAuthorizationId();
        }
        certificateAuthorizationId.setMethodAndURI(methodAndURI);
    }

    @DynamoDBAttribute
    @Getter
    @Setter
    private String uriRegex;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String httpMethods;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String uwGroups;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String contactEmails;

    @DynamoDBAttribute
    @Getter
    @Setter
    private String notes;

}
