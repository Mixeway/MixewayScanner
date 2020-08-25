/*
 * @created  2020-08-18 : 21:47
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${APIKEY:changeit}")
    private String apiKey;

    //Leave whatever you had here
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(new TokenAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);


        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests()
                .antMatchers("/**").authenticated();
    }




    //Add these two below.
    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(apiKeyAuthenticationProvider());
    }

    @Bean
    public TokenAuthenticationProvider apiKeyAuthenticationProvider() {
        return new TokenAuthenticationProvider(apiKey);
    }
}
