package br.com.digitalquinze.clientgrowthtracker.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.digitalquinze.clientgrowthtracker.user.AppUser;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}")
            long expirationMinutes) {

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(AppUser user) {

        Instant now = Instant.now();

        JwtClaimsSet.Builder claims =
                JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(
                        now.plus(
                            Duration.ofMinutes(
                                expirationMinutes
                            )
                        )
                    )
                    .subject(user.getEmail())
                    .claim(
                        "userId",
                        user.getId()
                    )
                    .claim(
                        "roles",
                        List.of(user.getRole().name())
                    );

        if (user.getRestaurant() != null) {

            claims.claim(
                "restaurantId",
                user.getRestaurant().getId()
            );
        }

        JwsHeader header =
                JwsHeader
                    .with(MacAlgorithm.HS256)
                    .build();

        return jwtEncoder
                .encode(
                    JwtEncoderParameters.from(
                        header,
                        claims.build()
                    )
                )
                .getTokenValue();
    }

    public long getExpirationSeconds() {

        return Duration
                .ofMinutes(expirationMinutes)
                .toSeconds();
    }
}