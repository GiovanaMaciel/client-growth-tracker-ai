package br.com.digitalquinze.clientgrowthtracker.channel;

public class ChannelNotFoundException
        extends RuntimeException {

    public ChannelNotFoundException(Long id) {
        super("Channel not found with id: " + id);
    }
}