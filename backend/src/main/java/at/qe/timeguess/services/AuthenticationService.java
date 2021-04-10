package at.qe.timeguess.services;

import at.qe.timeguess.dto.UserDTO;
import at.qe.timeguess.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

/**
 * Class that provides all methods needed for
 * creating and validating JWT tokens.
 */
@Component
public class AuthenticationService {

    public static final String TOKEN_PREFIX = "Bearer";

    @Value("${jwt.secret}")
    private String secret;

    private DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (JWTDecodeException exception) {
            //Invalid token
        }
        return null;
    }

    public String getSubject(String token) {
        DecodedJWT decodedToken = decodeToken(token);
        if (decodedToken != null) {
            return decodedToken.getSubject();
        } else {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        DecodedJWT decodedToken = decodeToken(token);
        if (decodedToken != null) {
            return decodedToken.getExpiresAt();
        } else {
            return null;
        }
    }

    public Claim getClaimFromToken(String token, String claim) {
        DecodedJWT decodedToken = decodeToken(token);
        if (decodedToken != null) {
            return decodedToken.getClaim(claim);
        } else {
            return null;
        }
    }


    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Generated the jwt token for the specified user with the specified expiration time
     * puts the role of the user into the token and it's username as subject.
     * This is done so that it can be checked with the token if a user is allowed to perform
     * a specific action depending on his role and username.
     *
     * @param user             user that generates token
     * @param token_expiration date when token expires
     * @return JWT Token String
     */
    public String generateToken(User user, Long token_expiration) {
        try {
            return TOKEN_PREFIX + " " + JWT.create()
                .withClaim("last_updated", user.getUpdateDate())
                .withClaim("user_id", user.getId())
                .withClaim("user_username", user.getUsername())
                .withClaim("user_role", user.getRole().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + token_expiration))
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .sign(HMAC512(this.secret.getBytes()));
        } catch (JWTCreationException exception) {
            return null;
        }

    }

    /**
     * Validates a JWT token.
     * It checks if the token subject matches with the given user and checks if the token
     * is still valid and not expired.
     *
     * @param token to validate
     * @param user  to check token for
     * @return if token is valid
     */
    public boolean validateToken(String token, User user) {
        if (token == null) {
            return false;
        }
        try {
            JWTVerifier verifier = JWT.require(HMAC512(this.secret.getBytes()))
                .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            Long id = jwt.getClaim("user_id").asLong();
            Date lastUpdated = jwt.getClaim("last_updated").asDate();
            Date updateDate = user.getUpdateDate();

            Long timeDifference = -2000L;

            if (lastUpdated != null && updateDate != null) {
                timeDifference = lastUpdated.getTime() - updateDate.getTime();
            }

            //check if it's token of right user and not expired
            return id != null && id.equals(user.getId())
                && (user.getUpdateDate() == null || (timeDifference < 1000 && timeDifference > -1000))
                && !this.isTokenExpired(token);
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            return false;
        }
    }


}
