package edu.uw.edm;

import com.google.common.base.Preconditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.service.CertificateAuthorizerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */

@SpringBootApplication(
        scanBasePackages = {"edu.uw.edm.edmzuulproxy.certificateauthorizer", "edu.uw.edm.edmzuulproxy.config.aws", "edu.uw.edm.edmzuulproxy.properties"})
@Slf4j
public class CertificateManagementApplication implements ApplicationRunner {

    private CertificateAuthorizerService certificateAuthorizerService;


    @Autowired
    public CertificateManagementApplication(CertificateAuthorizerService certificateAuthorizerService) {
        this.certificateAuthorizerService = certificateAuthorizerService;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {

        log.info("starting CertificateManagementApplication");
        log.info("args are");
        applicationArguments.getOptionNames().forEach(optionName -> {
            log.info("{} - {}", optionName, String.join(", ", applicationArguments.getOptionValues(optionName)));
        });

        Preconditions.checkArgument(applicationArguments.getOptionValues("certName").size() == 1, "can only have one cert");
        Preconditions.checkArgument(applicationArguments.getOptionValues("uriRegex").size() == 1, "can only have one uriRegex");
        Preconditions.checkArgument(applicationArguments.getOptionValues("notes") == null || applicationArguments.getOptionValues("notes").size() <= 1, "can only have 0 or 1 note");

        final String certName = applicationArguments.getOptionValues("certName").get(0);
        final String uriRegex = applicationArguments.getOptionValues("uriRegex").get(0);
        final List<String> httpMethods = applicationArguments.getOptionValues("httpMethods");
        final List<String> uwGroups = applicationArguments.getOptionValues("uwGroups");
        final List<String> contactEmails = applicationArguments.getOptionValues("contactEmails");
        final String notes = applicationArguments.getOptionValues("notes") != null ? applicationArguments.getOptionValues("notes").get(0) : null;

        certificateAuthorizerService.addNewAuthorization(certName, uriRegex, httpMethods, uwGroups, contactEmails, notes);

    }

    public static void main(String[] args) {
        System.exit(SpringApplication
                .exit(SpringApplication.run(CertificateManagementApplication.class, args)));

    }
}
