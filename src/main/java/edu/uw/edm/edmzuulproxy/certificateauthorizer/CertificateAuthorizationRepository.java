package edu.uw.edm.edmzuulproxy.certificateauthorizer;

import org.springframework.data.repository.CrudRepository;

import edu.uw.edm.edmzuulproxy.certificateauthorizer.model.dao.CertificateAuthorizationDAO;

/**
 * @author Maxime Deravet Date: 2019-01-28
 */
public interface CertificateAuthorizationRepository extends CrudRepository<CertificateAuthorizationDAO, String> {


    Iterable<CertificateAuthorizationDAO> findByCertificateName(String certificateName);
}
