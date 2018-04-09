package edu.uw.edm.edmzuulproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class EdmZuulProxyApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EdmZuulProxyApplication.class);
		app.addListeners(new ApplicationPidFileWriter());

		app.run(args);
	}
}
