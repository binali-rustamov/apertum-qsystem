ALTER TABLE services MODIFY button_text VARCHAR(2500) NOT NULL DEFAULT '';

ALTER TABLE services MODIFY name VARCHAR(2000) NOT NULL DEFAULT '';

UPDATE net SET version = '0.2 updated';