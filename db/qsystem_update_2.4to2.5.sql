USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------
delete from  `services` where `deleted` is not null;
ALTER TABLE `services` ADD `duration` INT NOT NULL DEFAULT '1' COMMENT 'Норматив. Среднее время оказания этой услуги.  Пока для маршрутизации при медосмотре' ;
ALTER TABLE `services` ADD `sound_template` VARCHAR(45) NULL COMMENT 'шаблон звукового приглашения. null или 0... - использовать родительский.';

-- -----------------------------------------------------
-- Table `qsystem`.`standards`
-- -----------------------------------------------------

ALTER TABLE `standards` ADD `relocation` INT NOT NULL DEFAULT 1 COMMENT 'типа параметр если есть перемещение, например между корпусами или ходьба до оператора';

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '2.5' where id<>-1;

COMMIT;

