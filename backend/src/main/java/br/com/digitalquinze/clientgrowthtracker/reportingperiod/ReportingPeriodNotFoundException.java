package br.com.digitalquinze.clientgrowthtracker.reportingperiod;

public class ReportingPeriodNotFoundException
        extends RuntimeException {

    public ReportingPeriodNotFoundException(Long id) {
        super("Reporting period not found with id: " + id);
    }
}