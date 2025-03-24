DROP TABLE IF EXISTS users_boats;
DROP TABLE IF EXISTS boats;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(25) NOT NULL UNIQUE,
    password VARCHAR     NOT NULL
);

CREATE TABLE IF NOT EXISTS boats
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS users_boats
(
    user_id BIGSERIAL NOT NULL,
    boat_id BIGSERIAL NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_users_boats_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_boats_boat FOREIGN KEY (boat_id) REFERENCES boats (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations
(
    id                  BIGSERIAL PRIMARY KEY,
    start_date          TIMESTAMP NOT NULL,
    end_date            TIMESTAMP NOT NULL,
    boat_hours_on_start INTEGER,
    boat_hours_on_end   INTEGER,
    boat_id             BIGSERIAL NOT NULL,
    user_id             BIGSERIAL NOT NULL,
    CONSTRAINT fk_reservations_boat FOREIGN KEY (boat_id) REFERENCES boats (id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id)
);
