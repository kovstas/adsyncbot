CREATE TABLE "user"
(
    id         uuid PRIMARY KEY         NOT NULL,
    name       VARCHAR                  NOT NULL,
    is_active  BOOLEAN                  NOT NULL,
    tg_chat_id INTEGER                  NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE company
(
    id           uuid PRIMARY KEY         NOT NULL,
    ad_tenant_id VARCHAR                  NOT NULL,
    is_active    BOOLEAN                  NOT NULL,
    created_by   uuid                     NOT NULL REFERENCES "user" (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE company_user
(
    id           uuid PRIMARY KEY         NOT NULL,
    user_id      uuid                     NOT NULL REFERENCES "user" (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    company_id   uuid                     NOT NULL REFERENCES company (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    ad_tenant_id VARCHAR                  NOT NULL,
    email        VARCHAR,
    phone_number VARCHAR,
    is_active    BOOLEAN                  NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE "group"
(
    id          uuid PRIMARY KEY         NOT NULL,
    name        VARCHAR                  NOT NULL,
    company_id  uuid                     NOT NULL REFERENCES company (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    tg_chat_id  INTEGER                  NOT NULL,
    ad_group_id VARCHAR                  NOT NULL,
    is_active   BOOLEAN                  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE group_member
(
    group_id        uuid                     NOT NULL REFERENCES "group" (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    company_user_id uuid                     NOT NULL REFERENCES company_user (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
