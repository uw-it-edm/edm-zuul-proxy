package edu.uw.edm.edmzuulproxy.config.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletResponse;

import edu.uw.edm.edmzuulproxy.properties.SecurityProperties;
import edu.uw.edm.edmzuulproxy.security.UserDetailsService;

/**
 * @author Maxime Deravet
 * Date: 3/27/18
 */
@Configuration
@EnableWebSecurity
public class AuthenticationConfiguration {
    private final SecurityProperties securityProperties;

    @Autowired
    public AuthenticationConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders(securityProperties.getAuthenticationHeaderName())
                        .allowedHeaders(securityProperties.getDocfinityAuthenticationHeaderName())
                        .allowCredentials(false).maxAge(3600);
            }
        };
    }

    @Configuration
    @Order(1)
    public static class ConfigurerAdapter extends WebSecurityConfigurerAdapter {
        private final PreAuthenticatedAuthenticationProvider authenticationProvider;

        private final SecurityProperties securityProperties;

        @Autowired
        public ConfigurerAdapter(PreAuthenticatedAuthenticationProvider authenticationProvider, SecurityProperties securityProperties) {
            this.authenticationProvider = authenticationProvider;
            this.securityProperties = securityProperties;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();

            http.authorizeRequests()
                    .requestMatchers(EndpointRequest.to("status", "info", "health"))
                    .permitAll()
                    .antMatchers("/docs/**")
                    .permitAll()
                    .antMatchers(HttpMethod.OPTIONS)
                    .permitAll()
                    .anyRequest()
                    .permitAll();


            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

            /**
             * The intent is for 'x-audituser' to take precedence as the authentication header, so if a
             * request also has the 'x-uw-act-as' header (potentially added by mistake) the 'x-audituser'
             * will be used for authentication. See CAB-4314.
             */
            http.addFilterBefore(
                    requestHeaderAuthenticationFilter(securityProperties.getDocfinityAuthenticationHeaderName()),
                    UsernamePasswordAuthenticationFilter.class);
            
            http.addFilterBefore(
                    requestHeaderAuthenticationFilter(securityProperties.getAuthenticationHeaderName()),
                    UsernamePasswordAuthenticationFilter.class);

            http.headers().frameOptions().disable();
        }


        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
            authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        }

        public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter(String authenticationHeaderName) throws Exception {
            RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter = new RequestHeaderAuthenticationFilter();

            requestHeaderAuthenticationFilter.setPrincipalRequestHeader(authenticationHeaderName);
            requestHeaderAuthenticationFilter.setAuthenticationManager(authenticationManager());

            requestHeaderAuthenticationFilter.setExceptionIfHeaderMissing(false);
            requestHeaderAuthenticationFilter.setAuthenticationFailureHandler((request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED));

            return requestHeaderAuthenticationFilter;
        }
    }

    @Bean
    public PreAuthenticatedAuthenticationProvider authenticationProvider(UserDetailsByNameServiceWrapper userDetailsByNameServiceWrapper) {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(userDetailsByNameServiceWrapper);
        return authenticationProvider;
    }

    @Bean
    public UserDetailsByNameServiceWrapper userDetailsByNameServiceWrapper(final UserDetailsService userDetailsService) {
        return new UserDetailsByNameServiceWrapper(userDetailsService);
    }
}
