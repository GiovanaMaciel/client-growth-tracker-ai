package br.com.digitalquinze.clientgrowthtracker.paidtraffic;

public class PaidTrafficMetricNotFoundException
        extends RuntimeException {

    public PaidTrafficMetricNotFoundException(
            Long periodId) {

        super(
            "Paid traffic metrics not found for reporting period: "
            + periodId
        );
    }
}