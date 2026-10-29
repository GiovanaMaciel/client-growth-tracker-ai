-- ============================================================
-- CLIENT GROWTH TRACKER
-- Initial Database Schema
-- ============================================================
-- 1. RESTAURANTS
-- Clientes da agência.
--
-- Exemplo:
-- Casa Pratello
-- Restaurante B
-- Restaurante C
-- ============================================================

CREATE TABLE restaurants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(160) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 2. USERS
--
-- Existem dois tipos principais:
--
-- AGENCY_ADMIN
--   Podem acessar todos os restaurantes.
--
-- RESTAURANT_USER
--   Login do restaurante.
--   Só pode acessar o restaurante relacionado à conta.
-- ============================================================

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    restaurant_id BIGINT,

    name VARCHAR(120) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    password_hash TEXT NOT NULL,

    role VARCHAR(30) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_restaurant
        FOREIGN KEY (restaurant_id)
        REFERENCES restaurants(id)
        ON DELETE CASCADE,

    CONSTRAINT check_user_role
        CHECK (
            role IN (
                'AGENCY_ADMIN',
                'RESTAURANT_USER'
            )
        ),

    CONSTRAINT check_user_restaurant_relation
        CHECK (
            (role = 'AGENCY_ADMIN' AND restaurant_id IS NULL)
            OR
            (role = 'RESTAURANT_USER' AND restaurant_id IS NOT NULL)
        )
);


-- ============================================================
-- 3. REPORTING PERIODS
--
-- Representa um período de análise.
--
-- Exemplo:
-- Casa Pratello
-- Agosto/2026
-- 26 dias abertos
-- ============================================================

CREATE TABLE reporting_periods (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    restaurant_id BIGINT NOT NULL,

    reference_month DATE NOT NULL,

    open_days SMALLINT NOT NULL,

    notes TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reporting_period_restaurant
        FOREIGN KEY (restaurant_id)
        REFERENCES restaurants(id)
        ON DELETE CASCADE,

    CONSTRAINT check_open_days
        CHECK (
            open_days BETWEEN 0 AND 31
        ),

    CONSTRAINT check_reference_month
        CHECK (
            EXTRACT(DAY FROM reference_month) = 1
        ),

    CONSTRAINT unique_restaurant_month
        UNIQUE (
            restaurant_id,
            reference_month
        )
);


-- ============================================================
-- 4. CHANNELS
--
-- Marketplaces/canais disponíveis no sistema.
--
-- São globais.
--
-- Exemplo:
-- iFood
-- 99 Food
-- Anota Aí
-- ============================================================

CREATE TABLE channels (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    slug VARCHAR(50) NOT NULL UNIQUE,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 5. RESTAURANT CHANNELS
--
-- Define quais canais cada restaurante utiliza.
--
-- Casa Pratello:
--   iFood
--   99 Food
--   Anota Aí
--
-- Outro restaurante pode ter:
--   iFood
--   Rappi
--
-- external_store_id poderá ser utilizado futuramente nas
-- integrações com APIs dos marketplaces.
-- ============================================================

CREATE TABLE restaurant_channels (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    restaurant_id BIGINT NOT NULL,

    channel_id BIGINT NOT NULL,

    external_store_id VARCHAR(255),

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_restaurant_channel_restaurant
        FOREIGN KEY (restaurant_id)
        REFERENCES restaurants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_restaurant_channel_channel
        FOREIGN KEY (channel_id)
        REFERENCES channels(id),

    CONSTRAINT unique_restaurant_channel
        UNIQUE (
            restaurant_id,
            channel_id
        )
);


-- ============================================================
-- 6. CHANNEL METRICS
--
-- Métricas financeiras mensais do canal.
--
-- Exemplo:
--
-- Casa Pratello
-- Agosto/2026
-- iFood
--
-- gross_revenue = 4121.20
-- orders = 88
-- net_revenue = 2769.84
--
-- source:
--   MANUAL       -> usuário inseriu
--   INTEGRATION  -> API do marketplace
-- ============================================================

CREATE TABLE channel_metrics (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    period_id BIGINT NOT NULL,

    restaurant_channel_id BIGINT NOT NULL,

    gross_revenue NUMERIC(12,2) NOT NULL,

    orders INTEGER NOT NULL,

    net_revenue NUMERIC(12,2) NOT NULL,

    fees NUMERIC(12,2),

    promotions NUMERIC(12,2),

    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_channel_metrics_period
        FOREIGN KEY (period_id)
        REFERENCES reporting_periods(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_channel_metrics_restaurant_channel
        FOREIGN KEY (restaurant_channel_id)
        REFERENCES restaurant_channels(id),

    CONSTRAINT check_gross_revenue
        CHECK (
            gross_revenue >= 0
        ),

    CONSTRAINT check_orders
        CHECK (
            orders >= 0
        ),

    CONSTRAINT check_net_revenue
        CHECK (
            net_revenue >= 0
        ),

    CONSTRAINT check_fees
        CHECK (
            fees IS NULL
            OR fees >= 0
        ),

    CONSTRAINT check_promotions
        CHECK (
            promotions IS NULL
            OR promotions >= 0
        ),

    CONSTRAINT check_net_revenue_value
        CHECK (
            net_revenue <= gross_revenue
        ),

    CONSTRAINT check_channel_metric_source
        CHECK (
            source IN (
                'MANUAL',
                'INTEGRATION'
            )
        ),

    CONSTRAINT unique_period_restaurant_channel
        UNIQUE (
            period_id,
            restaurant_channel_id
        )
);


-- ============================================================
-- 7. FUNNEL STAGE DEFINITIONS
--
-- Define a estrutura do funil de cada marketplace.
--
-- iFood:
-- Visitas
-- Produtos
-- Sacola
-- Revisão
-- Pedidos
--
-- 99 Food:
-- Visitas
-- Carrinho
-- Pagos
-- ============================================================

CREATE TABLE funnel_stage_definitions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    channel_id BIGINT NOT NULL,

    stage_key VARCHAR(50) NOT NULL,

    stage_name VARCHAR(100) NOT NULL,

    stage_order SMALLINT NOT NULL,

    value_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',

    CONSTRAINT fk_funnel_stage_channel
        FOREIGN KEY (channel_id)
        REFERENCES channels(id)
        ON DELETE CASCADE,

    CONSTRAINT check_stage_order
        CHECK (
            stage_order > 0
        ),

    CONSTRAINT check_funnel_value_source
        CHECK (
            value_source IN (
                'MANUAL',
                'ORDERS'
            )
        ),

    CONSTRAINT unique_channel_stage
        UNIQUE (
            channel_id,
            stage_key
        ),

    CONSTRAINT unique_channel_stage_order
        UNIQUE (
            channel_id,
            stage_order
        )
);


-- ============================================================
-- 8. FUNNEL STAGE VALUES
--
-- Valores reais do funil em determinado mês.
--
-- Exemplo:
--
-- Agosto/2026 - iFood
--
-- visitas = 544
-- produtos = 396
-- sacola = 216
-- revisão = 208
-- ============================================================

CREATE TABLE funnel_stage_values (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    channel_metric_id BIGINT NOT NULL,

    funnel_stage_id BIGINT NOT NULL,

    value INTEGER NOT NULL,

    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_funnel_value_channel_metric
        FOREIGN KEY (channel_metric_id)
        REFERENCES channel_metrics(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_funnel_value_stage
        FOREIGN KEY (funnel_stage_id)
        REFERENCES funnel_stage_definitions(id),

    CONSTRAINT check_funnel_value
        CHECK (
            value >= 0
        ),

    CONSTRAINT check_funnel_source
        CHECK (
            source IN (
                'MANUAL',
                'INTEGRATION'
            )
        ),

    CONSTRAINT unique_metric_funnel_stage
        UNIQUE (
            channel_metric_id,
            funnel_stage_id
        )
);


-- ============================================================
-- 9. PAID TRAFFIC METRICS
--
-- Dados de tráfego pago daquele período.
--
-- Exemplo:
--
-- investimento
-- alcance
-- impressões
-- visitas
-- carrinhos
-- compras
-- receita atribuída
-- ============================================================

CREATE TABLE paid_traffic_metrics (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    period_id BIGINT NOT NULL UNIQUE,

    investment NUMERIC(12,2),

    reach INTEGER,

    impressions INTEGER,

    visits INTEGER,

    carts INTEGER,

    purchases INTEGER,

    attributed_revenue NUMERIC(12,2),

    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_paid_traffic_period
        FOREIGN KEY (period_id)
        REFERENCES reporting_periods(id)
        ON DELETE CASCADE,

    CONSTRAINT check_investment
        CHECK (
            investment IS NULL
            OR investment >= 0
        ),

    CONSTRAINT check_reach
        CHECK (
            reach IS NULL
            OR reach >= 0
        ),

    CONSTRAINT check_impressions
        CHECK (
            impressions IS NULL
            OR impressions >= 0
        ),

    CONSTRAINT check_visits
        CHECK (
            visits IS NULL
            OR visits >= 0
        ),

    CONSTRAINT check_carts
        CHECK (
            carts IS NULL
            OR carts >= 0
        ),

    CONSTRAINT check_purchases
        CHECK (
            purchases IS NULL
            OR purchases >= 0
        ),

    CONSTRAINT check_attributed_revenue
        CHECK (
            attributed_revenue IS NULL
            OR attributed_revenue >= 0
        ),

    CONSTRAINT check_paid_traffic_source
        CHECK (
            source IN (
                'MANUAL',
                'INTEGRATION'
            )
        )
);


-- ============================================================
-- 10. REPORT INSIGHTS
--
-- Textos analíticos utilizados nos relatórios.
--
-- Futuramente poderão ser gerados pelo Spring AI + LLM.
--
-- MANUAL = usuário escreveu
-- AI     = IA gerou
-- ============================================================

CREATE TABLE report_insights (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    period_id BIGINT NOT NULL,

    section VARCHAR(30) NOT NULL,

    title VARCHAR(200),

    content TEXT NOT NULL,

    display_order SMALLINT NOT NULL DEFAULT 1,

    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_report_insight_period
        FOREIGN KEY (period_id)
        REFERENCES reporting_periods(id)
        ON DELETE CASCADE,

    CONSTRAINT check_report_section
        CHECK (
            section IN (
                'EXECUTIVE',
                'STRATEGIC'
            )
        ),

    CONSTRAINT check_report_source
        CHECK (
            source IN (
                'MANUAL',
                'AI'
            )
        ),

    CONSTRAINT check_display_order
        CHECK (
            display_order > 0
        )
);


-- ============================================================
-- INITIAL CHANNELS
-- ============================================================

INSERT INTO channels (
    name,
    slug
)
VALUES
    ('iFood', 'ifood'),
    ('99 Food', '99-food'),
    ('Anota Aí', 'anota-ai');


-- ============================================================
-- IFOOD FUNNEL
--
-- Pedidos não serão preenchidos novamente.
-- O sistema utiliza channel_metrics.orders.
-- ============================================================

INSERT INTO funnel_stage_definitions
(
    channel_id,
    stage_key,
    stage_name,
    stage_order,
    value_source
)

SELECT
    id,
    'visits',
    'Visitas',
    1,
    'MANUAL'
FROM channels
WHERE slug = 'ifood'

UNION ALL

SELECT
    id,
    'products',
    'Produtos',
    2,
    'MANUAL'
FROM channels
WHERE slug = 'ifood'

UNION ALL

SELECT
    id,
    'bag',
    'Sacola',
    3,
    'MANUAL'
FROM channels
WHERE slug = 'ifood'

UNION ALL

SELECT
    id,
    'review',
    'Revisão',
    4,
    'MANUAL'
FROM channels
WHERE slug = 'ifood'

UNION ALL

SELECT
    id,
    'orders',
    'Pedidos',
    5,
    'ORDERS'
FROM channels
WHERE slug = 'ifood';


-- ============================================================
-- 99 FOOD FUNNEL
-- ============================================================

INSERT INTO funnel_stage_definitions
(
    channel_id,
    stage_key,
    stage_name,
    stage_order,
    value_source
)

SELECT
    id,
    'visits',
    'Visitas',
    1,
    'MANUAL'
FROM channels
WHERE slug = '99-food'

UNION ALL

SELECT
    id,
    'cart',
    'Carrinho',
    2,
    'MANUAL'
FROM channels
WHERE slug = '99-food'

UNION ALL

SELECT
    id,
    'paid',
    'Pagos',
    3,
    'MANUAL'
FROM channels
WHERE slug = '99-food';


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_users_restaurant
ON users(restaurant_id);


CREATE INDEX idx_reporting_periods_restaurant
ON reporting_periods(restaurant_id);


CREATE INDEX idx_restaurant_channels_restaurant
ON restaurant_channels(restaurant_id);


CREATE INDEX idx_restaurant_channels_channel
ON restaurant_channels(channel_id);


CREATE INDEX idx_channel_metrics_period
ON channel_metrics(period_id);


CREATE INDEX idx_channel_metrics_restaurant_channel
ON channel_metrics(restaurant_channel_id);


CREATE INDEX idx_funnel_values_channel_metric
ON funnel_stage_values(channel_metric_id);


CREATE INDEX idx_report_insights_period
ON report_insights(period_id);