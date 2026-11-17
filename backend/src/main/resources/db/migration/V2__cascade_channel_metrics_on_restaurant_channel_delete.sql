-- Keep channel metrics consistent when an agency administrator removes a restaurant.
-- Other restaurant-owned tables already use ON DELETE CASCADE.

ALTER TABLE channel_metrics
    DROP CONSTRAINT fk_channel_metrics_restaurant_channel;

ALTER TABLE channel_metrics
    ADD CONSTRAINT fk_channel_metrics_restaurant_channel
    FOREIGN KEY (restaurant_channel_id)
    REFERENCES restaurant_channels(id)
    ON DELETE CASCADE;
