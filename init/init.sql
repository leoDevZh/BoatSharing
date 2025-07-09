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
/*1..m join-table for future extensibility*/
CREATE TABLE IF NOT EXISTS users_boats
(
    user_id INTEGER NOT NULL,
    boat_id INTEGER NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_users_boats_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_boats_boat FOREIGN KEY (boat_id) REFERENCES boats (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations
(
    id                  SERIAL PRIMARY KEY,
    start_date          TIMESTAMP NOT NULL,
    end_date            TIMESTAMP NOT NULL,
    boat_hours_on_start DECIMAL,
    boat_hours_on_end   DECIMAL,
    boat_id             SERIAL    NOT NULL,
    user_id             SERIAL    NOT NULL,
    CONSTRAINT fk_reservations_boat FOREIGN KEY (boat_id) REFERENCES boats (id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS payments
(
    id              SERIAL PRIMARY KEY,
    payment_date    TIMESTAMP DEFAULT now(),
    amount          DECIMAL      NOT NULL,
    status          VARCHAR(20) CHECK ( status in ('OPEN', 'CHARGED', 'CLOSED')),
    reason          VARCHAR(255) NOT NULL,
    is_fuel_payment BOOLEAN      NOT NULL,
    user_id         INTEGER      NOT NULL,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS debts
(
    id         SERIAL PRIMARY KEY,
    amount     DECIMAL NOT NULL,
    status     VARCHAR(20) CHECK ( status in ('OPEN', 'PAYED', 'CLOSED')),
    payment_ID INTEGER NOT NULL,
    user_id    INTEGER NOT NULL,
    CONSTRAINT fk_debt_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_debt_payment FOREIGN KEY (payment_ID) REFERENCES payments (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS invoices
(
    id         SERIAL PRIMARY KEY,
    start_date TIMESTAMP NOT NULL,
    end_date   TIMESTAMP NOT NULL,
    boat_id    INTEGER   NOT NULL,
    CONSTRAINT fk_boat_id FOREIGN KEY (boat_id) REFERENCES boats (id)
);

INSERT INTO users(username, password)
VALUES ('testUser', '$2a$12$GZ.66p7Q8jSIDE5F/gK0iuxDxvN9HDQOBH1Ho9Dz5L7z6hx1WW1BW'),
       ('testUser2', '$2a$12$GZ.66p7Q8jSIDE5F/gK0iuxDxvN9HDQOBH1Ho9Dz5L7z6hx1WW1BW'),
       ('testUser3', '$2a$12$GZ.66p7Q8jSIDE5F/gK0iuxDxvN9HDQOBH1Ho9Dz5L7z6hx1WW1BW'); --password123


INSERT INTO boats(name)
VALUES ('Seaquel');

INSERT INTO users_boats(user_id, boat_id)
VALUES (1, 1),
       (2, 1),
       (3, 1);