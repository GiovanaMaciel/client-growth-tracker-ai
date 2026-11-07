package br.com.digitalquinze.clientgrowthtracker.restaurantchannel;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digitalquinze.clientgrowthtracker.channel.Channel;
import br.com.digitalquinze.clientgrowthtracker.channel.ChannelNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.channel.ChannelRepository;
import br.com.digitalquinze.clientgrowthtracker.restaurant.Restaurant;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantRepository;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto.CreateRestaurantChannelRequest;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto.RestaurantChannelResponse;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.dto.UpdateRestaurantChannelRequest;
import br.com.digitalquinze.clientgrowthtracker.security.TenantAccessService;

@Service
public class RestaurantChannelService {

    private final RestaurantChannelRepository restaurantChannelRepository;
    private final RestaurantRepository restaurantRepository;
    private final ChannelRepository channelRepository;
    private final TenantAccessService tenantAccessService;

    public RestaurantChannelService(
            RestaurantChannelRepository restaurantChannelRepository,
            RestaurantRepository restaurantRepository,
            ChannelRepository channelRepository,
            TenantAccessService tenantAccessService) {

        this.restaurantChannelRepository =
                restaurantChannelRepository;

        this.restaurantRepository =
                restaurantRepository;

        this.channelRepository =
                channelRepository;

        this.tenantAccessService =
                tenantAccessService;
    }

    @Transactional
    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public RestaurantChannelResponse create(
            Long restaurantId,
            CreateRestaurantChannelRequest request) {

        Restaurant restaurant =
                restaurantRepository
                    .findById(restaurantId)
                    .orElseThrow(
                        () -> new RestaurantNotFoundException(
                            restaurantId
                        )
                    );

        Channel channel =
                channelRepository
                    .findById(request.channelId())
                    .orElseThrow(
                        () -> new ChannelNotFoundException(
                            request.channelId()
                        )
                    );

        if (restaurantChannelRepository
                .existsByRestaurant_IdAndChannel_Id(
                    restaurantId,
                    request.channelId()
                )) {

            throw new RestaurantChannelAlreadyExistsException();
        }

        RestaurantChannel restaurantChannel =
                new RestaurantChannel(
                    restaurant,
                    channel,
                    request.externalStoreId()
                );

        return toResponse(
            restaurantChannelRepository.save(
                restaurantChannel
            )
        );
    }

    @Transactional(readOnly = true)
    public List<RestaurantChannelResponse> findAll(
            Long restaurantId) {

        tenantAccessService
                .validateRestaurantAccess(restaurantId);

        return restaurantChannelRepository
                .findAllByRestaurant_IdOrderByChannel_NameAsc(
                    restaurantId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public RestaurantChannelResponse update(
            Long restaurantId,
            Long restaurantChannelId,
            UpdateRestaurantChannelRequest request) {

        RestaurantChannel restaurantChannel =
                findRestaurantChannel(
                    restaurantId,
                    restaurantChannelId
                );

        restaurantChannel.setExternalStoreId(
            request.externalStoreId()
        );

        restaurantChannel.setActive(
            request.active()
        );

        return toResponse(
            restaurantChannelRepository.save(
                restaurantChannel
            )
        );
    }

    private RestaurantChannel findRestaurantChannel(
            Long restaurantId,
            Long restaurantChannelId) {

        return restaurantChannelRepository
                .findByIdAndRestaurant_Id(
                    restaurantChannelId,
                    restaurantId
                )
                .orElseThrow(
                    () ->
                        new RestaurantChannelNotFoundException(
                            restaurantChannelId
                        )
                );
    }

    private RestaurantChannelResponse toResponse(
            RestaurantChannel restaurantChannel) {

        return new RestaurantChannelResponse(
            restaurantChannel.getId(),
            restaurantChannel.getRestaurant().getId(),

            restaurantChannel.getChannel().getId(),
            restaurantChannel.getChannel().getName(),
            restaurantChannel.getChannel().getSlug(),

            restaurantChannel.getExternalStoreId(),
            restaurantChannel.isActive(),

            restaurantChannel.getCreatedAt(),
            restaurantChannel.getUpdatedAt()
        );
    }
}