USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `seq_id` INT NOT NULL DEFAULT 0 COMMENT 'порядок следования кнопок услуг на пункте регистрации';

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '1.9' where id<>-1;

COMMIT;

