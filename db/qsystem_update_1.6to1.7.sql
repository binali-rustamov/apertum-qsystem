USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `advance` ADD `input_data` VARCHAR(150) NULL COMMENT 'Введеные при предвариловке данные клиента если услуга этого требует';
ALTER TABLE `services` ADD `ticket_text` VARCHAR(1500) NULL COMMENT 'Текст напечатается на талоне.';

COMMIT;
-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '1.7' where id<>-1;

COMMIT;

