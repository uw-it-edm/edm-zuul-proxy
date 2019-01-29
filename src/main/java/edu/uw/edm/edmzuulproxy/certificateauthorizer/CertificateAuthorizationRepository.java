package edu.uw.edm.edmzuulproxy.certificateauthorizer;

import org.springframework.data.repository.CrudRepository;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.CertificateAuthorization;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
public interface CertificateAuthorizationRepository extends CrudRepository<CertificateAuthorization, String> {


    Iterable<CertificateAuthorization> findByCertificateName(String certificateName);
}
