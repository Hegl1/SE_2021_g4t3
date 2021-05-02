package at.qe.timeguess.configs;

import at.qe.timeguess.model.User;
import at.qe.timeguess.services.AuthenticationService;
import at.qe.timeguess.services.UserService;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TokenChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
            StompCommand.SEND.equals(accessor.getCommand())
        ) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            Claim idClaim = null;

            // Removing Bearer from token
            if (jwtToken != null && jwtToken.startsWith("Bearer")) {
                jwtToken = jwtToken.substring(7);
                idClaim = authenticationService.getClaimFromToken(jwtToken, "user_id");
            }

            //Validate token and set authentication if valid
            if (idClaim != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Long id = idClaim.asLong();
                User user = this.userService.getUserById(id);

                if (authenticationService.validateToken(jwtToken, user)) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));

                    //creating user authentication object
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                    accessor.setUser(usernamePasswordAuthenticationToken);
                }
            }
        }
        return message;
    }

}
