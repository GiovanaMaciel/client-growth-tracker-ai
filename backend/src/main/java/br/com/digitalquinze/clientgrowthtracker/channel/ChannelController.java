package br.com.digitalquinze.clientgrowthtracker.channel;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelRepository channelRepository;

    public ChannelController(
            ChannelRepository channelRepository) {

        this.channelRepository = channelRepository;
    }

    @GetMapping
    public ResponseEntity<List<ChannelResponse>> findAll() {

        List<ChannelResponse> channels =
                channelRepository
                    .findAllByActiveTrueOrderByNameAsc()
                    .stream()
                    .map(channel ->
                        new ChannelResponse(
                            channel.getId(),
                            channel.getName(),
                            channel.getSlug()
                        )
                    )
                    .toList();

        return ResponseEntity.ok(channels);
    }
}