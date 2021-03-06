package at.qe.timeguess.configs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.qe.timeguess.model.User;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * Filter that gets executed before all requests that need to authorized on the server.
 * Only the in the WebSecurityConfig defined endpoints like /api/login don't need
 * to be authorized.
 * Checks if bearer token is in authorization header. If it is in header and is valid
 * the role of the user gets read from the token and the user set as authorized with the role
 * contained in the token.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        Claim idClaim = null;
        String jwtToken = null;

        // Removing Bearer from token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
            jwtToken = requestTokenHeader.substring(7);
            idClaim = authenticationService.getClaimFromToken(jwtToken, "user_id");
        }

        //Validate token and set authentication if valid
        if (idClaim != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Long id = idClaim.asLong();
            User user = this.userService.getUserById(id);

            if (authenticationService.validateToken(jwtToken, user)) {
                authenticationService.setUserAuthentication(user);
            }
        }
        chain.doFilter(request, response);
    }

}
