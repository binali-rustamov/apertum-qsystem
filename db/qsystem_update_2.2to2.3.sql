USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------

ALTER TABLE `net` ADD `button_free_design` TINYINT(1)  NOT NULL DEFAULT false COMMENT 'авторасстановка кнопок на киоске';


-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `but_x` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки';
ALTER TABLE `services` ADD `but_y` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки';
ALTER TABLE `services` ADD `but_b` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки';
ALTER TABLE `services` ADD `but_h` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки';


-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '2.3' where id<>-1;

COMMIT;

