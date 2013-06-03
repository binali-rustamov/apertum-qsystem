USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`services_langs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`services_langs` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`services_langs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `services_id` BIGINT,
  `lang` VARCHAR(45) NOT NULL ,
  `name` VARCHAR(2000) NOT NULL DEFAULT '' ,
  `description` VARCHAR(20) NULL ,
  `button_text` VARCHAR(2500) NOT NULL DEFAULT '' ,
  `input_caption` VARCHAR(200) NOT NULL DEFAULT '' ,
  `ticket_text` VARCHAR(1500) NULL ,
  `pre_info_html` TEXT NOT NULL ,
  `pre_info_print_text` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_services_langs_services1`
    FOREIGN KEY (`services_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

CREATE INDEX `fk_services_langs_services1` ON `qsystem`.`services_langs` (`services_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '1.10' where id<>-1;

COMMIT;

