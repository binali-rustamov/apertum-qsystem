USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------

ALTER TABLE `net` ADD `voice` BIGINT NOT NULL DEFAULT 0 COMMENT '0 - по умолчанию, ну и т.д. по набору звуков';
ALTER TABLE `net` ADD `black_time` INT NOT NULL DEFAULT 0 COMMENT 'Время нахождения в блеклисте в минутах. 0 - попавшие в блекслист не блокируются';

COMMIT;
-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '1.5' where id<>-1;

COMMIT;

