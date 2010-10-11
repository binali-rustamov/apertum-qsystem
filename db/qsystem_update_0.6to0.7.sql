USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`calendar`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`calendar` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`calendar` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Название календаря' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Календарь услуг на год';

-- -----------------------------------------------------
-- Table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`calendar_out_days` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`calendar_out_days` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `out_day` DATE NOT NULL COMMENT 'Дата неработы. Важен месяц и день' ,
  `calendar_id` BIGINT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_calendar_out_days_calendar1`
    FOREIGN KEY (`calendar_id` )
    REFERENCES `qsystem`.`calendar` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
COMMENT = 'Дни неработы услуг';

CREATE INDEX `fk_calendar_out_days_calendar1` ON `qsystem`.`calendar_out_days` (`calendar_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE services ADD `calendar_id` BIGINT NULL;

ALTER TABLE services ADD CONSTRAINT `fk_services_calendar1`
    FOREIGN KEY (`calendar_id` )
    REFERENCES `qsystem`.`calendar` (`id` );

CREATE INDEX `fk_services_calendar1` ON `qsystem`.`services` (`calendar_id` ASC) ;

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar`
-- -----------------------------------------------------

SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar` (`id`, `name`) VALUES (1, 'Общий календарь');

UPDATE services SET calendar_id = 1;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------

UPDATE net SET version = '0.6 to 0.7';

COMMIT;

