package br.com.digitalquinze.clientgrowthtracker.channelmetric;

public class ChannelMetricAlreadyExistsException
        extends RuntimeException {

    public ChannelMetricAlreadyExistsException() {
        super(
            "Metrics already exist for this channel and reporting period"
        );
    }
}