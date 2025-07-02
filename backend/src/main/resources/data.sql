INSERT INTO users(username, password)
VALUES ('testUser', '$2a$12$GbqlJ0fs.00G5MLiCA9AZuA0QF000rXCrkQ0sP5EkGbwsOb.KmlEm'),
       ('testUser2', '$2a$12$GbqlJ0fs.00G5MLiCA9AZuA0QF000rXCrkQ0sP5EkGbwsOb.KmlEm'),
       ('testUser3', '$2a$12$GbqlJ0fs.00G5MLiCA9AZuA0QF000rXCrkQ0sP5EkGbwsOb.KmlEm');/*password123*/


INSERT INTO boats(name)
VALUES ('MS Schissschüssle');

INSERT INTO users_boats(user_id, boat_id)
VALUES (1, 1),
       (2, 1),
       (3, 1);

INSERT INTO reservations (start_date, end_date, boat_hours_on_start, boat_hours_on_end, boat_id, user_id)
VALUES ('2025-05-01 08:00:00', '2025-05-01 12:00:00', 100, 104, 1, 1),
       ('2025-05-02 09:00:00', '2025-05-02 13:00:00', 104, 204, 1, 1),
       ('2025-05-03 10:00:00', '2025-05-03 14:00:00', 204, 209, 1, 2),
       ('2025-05-03 19:00:00', '2025-05-03 20:00:00', 209, 209, 1, 1),
       ('2025-05-04 07:30:00', '2025-05-04 10:30:00', 209, 212, 1, 2),
       ('2025-05-04 14:30:00', '2025-05-04 16:30:00', 212, 214, 1, 1);

INSERT INTO payments (payment_date, amount, status, reason, is_fuel_payment, user_id)
VALUES ('2025-05-04 14:30:00', 100., 'CHARGED', 'Erste Rechnung', 'false', 1),
       ('2025-05-04 14:40:00', 200., 'CHARGED', 'Zweite Rechnung ganz eine lange Begründung findet sich hier!', 'false',
        2);

INSERT INTO debts (amount, status, payment_id, user_id)
VALUES (48., 'OPEN', 2, 1),
       (88., 'PAYED', 1, 2)