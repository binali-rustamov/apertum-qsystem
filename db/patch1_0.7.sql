-- Выполняется только один раз.
-- Для повторного выполнения закоментировать строчку 6 и раскоментировать строку 8
SET AUTOCOMMIT=0;
USE `qsystem`;
-- services_ibfk_1
-- ДЛЯ ПЕРВОГО ИСПОЛНЕНИЯ, закоментировать следующую строку если хочется применить еще раз
ALTER TABLE `qsystem`.`services` DROP FOREIGN KEY `fk_services_calendar1`;
-- ДЛЯ ВТОРОГО И ПОСЛЕДУЮЩЕГО ПРИМЕНЕНИЯ, раскоментировать для применения следующую строку
-- ALTER TABLE `qsystem`.`services` DROP FOREIGN KEY `fk_services_calendar`;
-- убрать остатки зависимостей
UPDATE `qsystem`.`services` SET calendar_id = NULL;
-- удалить таблицу для пересоздания
DROP TABLE IF EXISTS `qsystem`.`calendar_out_days` ;
-- удалить таблицу для пересоздания
DROP TABLE IF EXISTS `qsystem`.`calendar` ;

-- -----------------------------------------------------
-- Table `qsystem`.`calendar`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `qsystem`.`calendar` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Название календаря' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Календарь услуг на год';


-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------
ALTER TABLE `qsystem`.`services`
    ADD
    CONSTRAINT `fk_services_calendar`
    FOREIGN KEY `fk_services_calendar`(`calendar_id`)
    REFERENCES `calendar` (`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- CREATE INDEX `fk_services_calendar` ON `qsystem`.`services` (`calendar_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `qsystem`.`calendar_out_days` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `out_day` DATE NOT NULL COMMENT 'Дата неработы. Важен месяц и день' ,
  `calendar_id` BIGINT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_calendar_out_days_calendar`
    FOREIGN KEY (`calendar_id` )
    REFERENCES `qsystem`.`calendar` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Дни неработы услуг';

CREATE INDEX `fk_calendar_out_days_calendar` ON `qsystem`.`calendar_out_days` (`calendar_id` ASC) ;

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar` (`id`, `name`) VALUES (1, 'Общий календарь');
COMMIT;
-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar_out_days` (`id`, `out_day`, `calendar_id`) VALUES (1, '2010-01-01', 1);
COMMIT;

SET AUTOCOMMIT=0;
USE `qsystem`;
UPDATE net SET version = '0.8a';
COMMIT;