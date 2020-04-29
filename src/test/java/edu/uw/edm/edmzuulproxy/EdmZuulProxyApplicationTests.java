package edu.uw.edm.edmzuulproxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uw.edm.edmzuulproxy.properties.CertificateAuthorizationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EdmZuulProxyApplicationTests {
	@MockBean
	private CertificateAuthorizationProperties mockCertificateAuthorizationProperties;

	@Test
	public void contextLoads() {
		//NOOP check if App can be started
	}

}
