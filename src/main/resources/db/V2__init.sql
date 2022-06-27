CREATE TYPE chat_member_role AS ENUM ('owner','admin','member');

CREATE TABLE "user"
(
    id         uuid PRIMARY KEY         NOT NULL,
    name       VARCHAR                  NOT NULL,
    is_active  BOOLEAN                  NOT NULL,
    tg_chat_id INTEGER                  NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE organization
(
    id           uuid PRIMARY KEY         NOT NULL,
    ad_tenant_id VARCHAR                  NOT NULL UNIQUE,
    name         VARCHAR                  NOT NULL,
    is_active    BOOLEAN                  NOT NULL,
    created_by   uuid                     NOT NULL REFERENCES "user" (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE organization_member
(
    id              uuid PRIMARY KEY         NOT NULL,
    user_id         uuid                     NOT NULL REFERENCES "user" (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    organization_id uuid                     NOT NULL REFERENCES organization (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    ad_tenant_id    VARCHAR                  NOT NULL UNIQUE,
    name            VARCHAR                  NOT NULL,
    email           VARCHAR                  NOT NULL,
    phone_number    VARCHAR,
    is_active       BOOLEAN                  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE chat
(
    id              uuid                     NOT NULL PRIMARY KEY,
    tg_chat_id      INTEGER                  NOT NULL UNIQUE,
    organization_id uuid                     NOT NULL REFERENCES organization (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    name            VARCHAR,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE chat_member
(
    chat_id                uuid                     NOT NULL REFERENCES chat (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    organization_member_id uuid                     NOT NULL REFERENCES organization_member (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    role                   chat_member_role         NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (chat_id, organization_member_id)
);