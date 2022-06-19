CREATE TYPE enum__team_member_role AS ENUM ('lead', 'member');


CREATE TABLE teams
(
    id           uuid PRIMARY KEY         NOT NULL,
    name         VARCHAR                  NOT NULL,
    tg_chat_id   INTEGER                  NOT NULL,
    is_active    BOOLEAN                  NOT NULL,
    ad_tenant_id VARCHAR                  NOT NULL,
    ad_group_id  VARCHAR                  NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE team_members
(
    id           uuid PRIMARY KEY         NOT NULL,
    team_id      uuid                     NOT NULL REFERENCES teams (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    name         VARCHAR                  NOT NULL,
    email        VARCHAR                  NOT NULL,
    phone_number VARCHAR,
    is_active    BOOLEAN                  NOT NULL,
    role         enum__team_member_role   NOT NULL,
    tg_user_id   INTEGER                  NOT NULL,
    tg_chat_id   INTEGER                  NOT NULL,
    ad_user_id   VARCHAR                  NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);