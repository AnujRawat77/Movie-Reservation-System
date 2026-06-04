-- Migration V2: Seat-hold concurrency tables
-- Compatible with PostgreSQL / Supabase
-- For H2 (dev) Hibernate auto-creates these from entity definitions

-- Holds one temporary reservation group per user per showtime
CREATE TABLE IF NOT EXISTS seat_holds (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    showtime_id BIGINT       NOT NULL REFERENCES showtimes(id),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE|CONFIRMED|EXPIRED|RELEASED
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_seat_hold_user     ON seat_holds(user_id);
CREATE INDEX IF NOT EXISTS idx_seat_hold_showtime ON seat_holds(showtime_id);
CREATE INDEX IF NOT EXISTS idx_seat_hold_status   ON seat_holds(status);
CREATE INDEX IF NOT EXISTS idx_seat_hold_expires  ON seat_holds(expires_at);

-- One row per (showtime, seat) while a hold is ACTIVE.
-- The UNIQUE constraint is the DB-level race guard:
-- concurrent INSERTs for the same seat/showtime → only one wins.
-- Row is deleted when hold expires, is released, or is confirmed.
CREATE TABLE IF NOT EXISTS seat_allocations (
    id              BIGSERIAL    PRIMARY KEY,
    showtime_id     BIGINT       NOT NULL REFERENCES showtimes(id),
    seat_id         BIGINT       NOT NULL REFERENCES seats(id),
    hold_owner_id   BIGINT       NOT NULL REFERENCES users(id),
    hold_id         UUID         NOT NULL REFERENCES seat_holds(id),
    hold_expires_at TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uk_seat_alloc_showtime_seat UNIQUE (showtime_id, seat_id)
);

CREATE INDEX IF NOT EXISTS idx_seat_alloc_showtime ON seat_allocations(showtime_id);
CREATE INDEX IF NOT EXISTS idx_seat_alloc_hold     ON seat_allocations(hold_id);
CREATE INDEX IF NOT EXISTS idx_seat_alloc_expires  ON seat_allocations(hold_expires_at);
