ALTER TABLE events
    ADD CONSTRAINT chk_event_category
        CHECK (category IN ('MUSIC', 'SPORTS', 'TECHNOLOGY', 'EDUCATION', 'CULTURE', 'ENTERTAINMENT')),
    ADD CONSTRAINT chk_event_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'));

ALTER TABLE tickets
    ADD CONSTRAINT chk_ticket_type
        CHECK (type IN ('GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT')),
    ADD CONSTRAINT chk_ticket_status
        CHECK (status IN ('RESERVED', 'PAID', 'CANCELLED', 'USED'));

CREATE INDEX idx_events_venue_id ON events (venue_id);
CREATE INDEX idx_tickets_user_id ON tickets (user_id);
CREATE INDEX idx_tickets_event_id ON tickets (event_id);
CREATE INDEX idx_event_artists_artist_id ON event_artists (artist_id);
