package br.com.digitalquinze.clientgrowthtracker.security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    private final String jwtSecret;
    private final String issuer;

    public JwtConfig(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.issuer}") String issuer) {

        this.jwtSecret = jwtSecret;
        this.issuer = issuer;
    }

    @Bean
    public JwtEncoder jwtEncoder() {

        return NimbusJwtEncoder
                .withSecretKey(secretKey())
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                    .withSecretKey(secretKey())
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();

        decoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
    }

    private SecretKey secretKey() {

        byte[] keyBytes =
                Base64.getDecoder().decode(jwtSecret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must contain at least 256 bits"
            );
        }

        return new SecretKeySpec(
                keyBytes,
                "HmacSHA256"
        );
    }
}