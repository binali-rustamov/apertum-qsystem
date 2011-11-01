USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `day_limit` INT NOT NULL DEFAULT 0 COMMENT 'ограничение выданных билетов в день. 0-нет ограничения';
ALTER TABLE `services` ADD `person_day_limit` INT NOT NULL DEFAULT 0 COMMENT 'ограничение выданных билетов в день клиентам с одинаковыми введенными данными. 0-нет ограничения';

COMMIT;
-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '1.4' where id<>-1;

COMMIT;

