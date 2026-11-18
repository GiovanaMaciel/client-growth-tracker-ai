CREATE TABLE ifood_financial_details (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    channel_metric_id BIGINT NOT NULL UNIQUE,
    services_and_promotions NUMERIC(12, 2),
    adjustments NUMERIC(12, 2),
    direct_receipts NUMERIC(12, 2),
    marketplace_transfers NUMERIC(12, 2),

    CONSTRAINT fk_ifood_financial_details_metric
        FOREIGN KEY (channel_metric_id)
        REFERENCES channel_metrics(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_ifood_services_promotions_nonnegative
        CHECK (services_and_promotions IS NULL OR services_and_promotions >= 0),

    CONSTRAINT chk_ifood_direct_receipts_nonnegative
        CHECK (direct_receipts IS NULL OR direct_receipts >= 0),

    CONSTRAINT chk_ifood_marketplace_transfers_nonnegative
        CHECK (marketplace_transfers IS NULL OR marketplace_transfers >= 0)
);