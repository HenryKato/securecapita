package io.hkfullstack.securecapita.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.model.UserPrincipal;
import io.hkfullstack.securecapita.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final String HK_INC = "HK_INC";
    private static final String SECURE_SERVICE = "SECURE_SERVICE";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000;
    private static final String CLAIMS = "claims";
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_000_000;
    private final UserService userService;

    @Value("${jwt.secret}")
    private String secret;

    public String generateAccessToken(UserPrincipal userPrincipal) {
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create()
                .withIssuer(HK_INC).withSubject(userPrincipal.getUsername())
                .withAudience(SECURE_SERVICE).withIssuedAt(new Date())
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .withArrayClaim(CLAIMS, claims).sign(Algorithm.HMAC512(secret.getBytes()));
    }

    public String generateRefreshToken(UserPrincipal userPrincipal) {
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create()
                .withIssuer(HK_INC).withSubject(userPrincipal.getUsername())
                .withAudience(SECURE_SERVICE).withIssuedAt(new Date())
                .withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    public List<GrantedAuthority> getUserClaimsFromToken(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public Authentication getAuthentication(String email, List<GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(userService.getUserByEmail(email), null, authorities);
        userPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPassAuthToken;
    }

    public boolean isTokenValid(String email, String token) {
        JWTVerifier verifier = getJWTVerifier();
        if(StringUtils.isNotEmpty(email) && !isTokenExpired(verifier, token))
            return true;
        throw new ApiException("Token invalid");
//        return StringUtils.isNotEmpty(email) && !isTokenExpired(verifier, token);
    }

    public String getSubject(String token, HttpServletRequest request) {
        try {
            return getJWTVerifier().verify(token).getSubject();
        } catch (TokenExpiredException ex) {
            request.setAttribute("expiredMessage", ex.getMessage());
        } catch (InvalidClaimException ex) {
            request.setAttribute("invalidClaim", ex.getMessage());
        } catch (Exception ex) {
            throw ex;
        }
        return "";
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return  verifier.verify(token).getClaim(CLAIMS).asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(HK_INC).build();
        } catch (JWTVerificationException exception){
            throw new JWTVerificationException("Invalid signature/claims");
        }
        return verifier;
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }
}
