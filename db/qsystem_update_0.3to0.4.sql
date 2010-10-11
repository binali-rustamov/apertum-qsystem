USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`information`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `qsystem`.`information` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`information` (
  `id` BIGINT NOT NULL ,
  `parent_id` BIGINT NULL ,
  `name` VARCHAR(100) NOT NULL COMMENT 'Наименование узла справки' ,
  `text` VARCHAR(5000) NOT NULL DEFAULT '' COMMENT 'html-текст справки' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_information_information1`
    FOREIGN KEY (`parent_id` )
    REFERENCES `qsystem`.`information` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Таблица справочной информации древовидной структуры';

CREATE INDEX `fk_information_information1` ON `qsystem`.`information` (`parent_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`responses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`response_event` ;
DROP TABLE IF EXISTS `qsystem`.`responses` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`responses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(100) NOT NULL DEFAULT '' ,
  `text` VARCHAR(5000) NOT NULL DEFAULT '' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Список отзывов в отратной связи';

-- -----------------------------------------------------
-- Table `qsystem`.`response_event`
-- -----------------------------------------------------

CREATE  TABLE IF NOT EXISTS `qsystem`.`response_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `resp_date` DATETIME NOT NULL COMMENT 'Дата отклика' ,
  `response_id` BIGINT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_response_date_responses1`
    FOREIGN KEY (`response_id` )
    REFERENCES `qsystem`.`responses` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Даты оставленных отзывов.';

CREATE INDEX `fk_response_date_responses1` ON `qsystem`.`response_event` (`response_id` ASC) ;

-- -----------------------------------------------------
-- Data for table `qsystem`.`responses`
-- -----------------------------------------------------

SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (1, 'Отлично', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Отлично</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (2, 'Хорошо', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Хорошо</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (3, 'Удовлетворительно', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Удовлетворительно</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (4, 'Плохо', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Плохо</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (5, 'Отвратительно', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Отвратительно</span></b>');
INSERT INTO `information` (`id`, `parent_id`, `name`, `text`) VALUES (1, null, 'Справочная система', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Справвочная информационная система.<br>Для  получения детальной информации обратитесь к менеджеру</span></b>');
COMMIT;


UPDATE net SET version = '0.3 updated to 0.4';