/*
 * @created  2020-08-18 : 21:15
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.config;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class TokenAuthenticationFilter extends GenericFilterBean {

    private final AuthenticationManager authenticationManager;

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String apiKey = httpRequest.getHeader("apiKey");

        try {
            if (!StringUtils.isEmpty(apiKey)) {
                processTokenAuthentication(apiKey);
            }
            chain.doFilter(request, response);
        } catch (InternalAuthenticationServiceException internalAuthenticationServiceException)
        {
            SecurityContextHolder.clearContext();
            logger.error("Internal authentication service exception", internalAuthenticationServiceException);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch(AuthenticationException authenticationException)
        {
            SecurityContextHolder.clearContext();
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, authenticationException.getMessage());
        }
    }

    private void processTokenAuthentication(String apiKey) {
        SessionCredentials authCredentials = new SessionCredentials(apiKey,null);
        Authentication requestAuthentication = new PreAuthenticatedAuthenticationToken(authCredentials, authCredentials);
        Authentication resultOfAuthentication = tryToAuthenticate(requestAuthentication);
        SecurityContextHolder.getContext().setAuthentication(resultOfAuthentication);
    }

    private Authentication tryToAuthenticate(Authentication requestAuthentication) {
        Authentication responseAuthentication = authenticationManager.authenticate(requestAuthentication);
        if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
            throw new InternalAuthenticationServiceException("Unable to authenticate Domain User for provided credentials");
        }
        return responseAuthentication;
    }
}

class TokenAuthenticationProvider implements AuthenticationProvider {

    private final String apiKey;

    public TokenAuthenticationProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SessionCredentials credentials = (SessionCredentials) authentication.getCredentials();
        if (credentials != null && credentials.apiKey.equals(this.apiKey)) {

            //Also evaluate the token here

            Authentication newAuthentication = new PreAuthenticatedAuthenticationToken(apiKey, credentials);
            newAuthentication.setAuthenticated(true);
            return newAuthentication;
        }
        throw new BadCredentialsException("Incorrect ApiKey");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(PreAuthenticatedAuthenticationToken.class);
    }
}