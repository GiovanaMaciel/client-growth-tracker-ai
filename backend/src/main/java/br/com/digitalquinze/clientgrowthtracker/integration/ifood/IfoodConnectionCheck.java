package br.com.digitalquinze.clientgrowthtracker.integration.ifood;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "ifood.check-on-startup",
        havingValue = "true"
)
public class IfoodConnectionCheck implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(IfoodConnectionCheck.class);

    private final IfoodAuthenticationService authenticationService;

    public IfoodConnectionCheck(
            IfoodAuthenticationService authenticationService) {

        this.authenticationService = authenticationService;
    }

    @Override
    public void run(ApplicationArguments args) {

        authenticationService.getAccessToken();

        logger.info("iFood authentication completed successfully.");
    }
}
