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
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        Claim roleClaim = null;

        // Removing Bearer from token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer")) {
            jwtToken = requestTokenHeader.substring(7);
            username = authenticationService.getSubject(jwtToken);
            roleClaim = authenticationService.getClaimFromToken(jwtToken, "role");
        }

        //Validate token and set authentication if valid
        if (username != null && roleClaim != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = this.userService.loadUser(username);
            String role = roleClaim.asString();

            if (authenticationService.validateToken(jwtToken, user)) {

                //adding role to authentication
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(role));

                //creating user authentication object
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
                usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Setting Authentication in the context, and specifying that the current user is authenticated.
                // So that Spring Security Configurations works.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}
