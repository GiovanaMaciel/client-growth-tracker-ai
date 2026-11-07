package br.com.digitalquinze.clientgrowthtracker.channelmetric;

public class ChannelMetricNotFoundException
        extends RuntimeException {

    public ChannelMetricNotFoundException(Long id) {
        super("Channel metric not found with id: " + id);
    }
}