USE qsystem;

ALTER TABLE services MODIFY pre_info_html TEXT NOT NULL;
ALTER TABLE services MODIFY pre_info_print_text TEXT NOT NULL;

ALTER TABLE information MODIFY `text` TEXT NOT NULL;
ALTER TABLE information MODIFY text_print TEXT NOT NULL;

UPDATE net SET version = '1.1 updated';