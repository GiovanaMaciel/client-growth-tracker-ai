package br.com.digitalquinze.clientgrowthtracker.integration.ifood;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import br.com.digitalquinze.clientgrowthtracker.integration.ifood.dto.IfoodTokenResponse;

@Service
public class IfoodAuthenticationService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    private CachedToken cachedToken;

    public IfoodAuthenticationService(
            @Value("${ifood.base-url}") String baseUrl,
            @Value("${ifood.client-id}") String clientId,
            @Value("${ifood.client-secret}") String clientSecret) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getAccessToken() {

        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "iFood credentials are not configured."
            );
        }

        if (cachedToken != null
                && Instant.now().isBefore(cachedToken.renewAt())) {
            return cachedToken.value();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grantType", "client_credentials");
        form.add("clientId", clientId);
        form.add("clientSecret", clientSecret);

        IfoodTokenResponse response = restClient.post()
                .uri("/authentication/v1.0/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(IfoodTokenResponse.class);

        if (response == null
                || response.accessToken() == null
                || response.accessToken().isBlank()
                || response.expiresIn() == null
                || response.expiresIn() <= 0) {

            throw new IllegalStateException(
                    "iFood returned an invalid authentication response."
            );
        }

        long renewalMargin = Math.min(
                300L,
                Math.max(1L, response.expiresIn() / 10L)
        );

        long validFor = Math.max(
                1L,
                response.expiresIn() - renewalMargin
        );

        cachedToken = new CachedToken(
                response.accessToken(),
                Instant.now().plusSeconds(validFor)
        );

        return cachedToken.value();
    }

    private record CachedToken(
            String value,
            Instant renewAt
    ) {
    }
}
