CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL,

    CONSTRAINT chk_venue_capacity
        CHECK (capacity > 0)
);

CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(150) NOT NULL UNIQUE,
    country VARCHAR(100) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    minimum_age INTEGER NOT NULL,

    venue_id BIGINT NOT NULL,

    CONSTRAINT fk_event_venue
        FOREIGN KEY (venue_id)
        REFERENCES venues(id)
);

CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    city VARCHAR(100),
    birth_date DATE,

    user_id BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_profile_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,

    ticket_code VARCHAR(50) NOT NULL UNIQUE,

    type VARCHAR(50) NOT NULL,

    price NUMERIC(10,2) NOT NULL,

    status VARCHAR(50) NOT NULL,

    purchase_date TIMESTAMP NOT NULL,

    user_id BIGINT NOT NULL,

    event_id BIGINT NOT NULL,


    CONSTRAINT chk_ticket_price
        CHECK(price >= 0),

    CONSTRAINT fk_ticket_user
        FOREIGN KEY(user_id)
        REFERENCES users(id),

    CONSTRAINT fk_ticket_event
        FOREIGN KEY(event_id)
        REFERENCES events(id)
);


CREATE TABLE event_artists (
    event_id BIGINT NOT NULL,
    artist_id BIGINT NOT NULL,

    PRIMARY KEY(event_id, artist_id),

    CONSTRAINT fk_event_artist_event
        FOREIGN KEY(event_id)
        REFERENCES events(id),

    CONSTRAINT fk_event_artist_artist
        FOREIGN KEY(artist_id)
        REFERENCES artists(id)
);