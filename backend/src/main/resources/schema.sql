DROP TABLE IF EXISTS users_boats CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS boats CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(25) NOT NULL UNIQUE,
    password VARCHAR     NOT NULL
);

CREATE TABLE IF NOT EXISTS boats
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS users_boats
(
    user_id SERIAL NOT NULL,
    boat_id SERIAL NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_users_boats_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_boats_boat FOREIGN KEY (boat_id) REFERENCES boats (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations
(
    id                  SERIAL PRIMARY KEY,
    start_date          TIMESTAMP NOT NULL,
    end_date            TIMESTAMP NOT NULL,
    boat_hours_on_start INTEGER,
    boat_hours_on_end   INTEGER,
    boat_id             SERIAL    NOT NULL,
    user_id             SERIAL    NOT NULL,
    CONSTRAINT fk_reservations_boat FOREIGN KEY (boat_id) REFERENCES boats (id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id)
);
