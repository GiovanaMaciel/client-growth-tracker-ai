package br.com.digitalquinze.clientgrowthtracker.reportingperiod;

import java.time.LocalDate;

public class ReportingPeriodAlreadyExistsException
        extends RuntimeException {

    public ReportingPeriodAlreadyExistsException(
            Long restaurantId,
            LocalDate referenceMonth) {

        super(
            "Reporting period already exists for restaurant "
            + restaurantId
            + " and month "
            + referenceMonth
        );
    }
}