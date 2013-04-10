USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `advance_time_period` INT NOT NULL DEFAULT 60 COMMENT 'периоды, на которые делится день, для записи предварительно';

-- -----------------------------------------------------
-- Table `qsystem`.`breaks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`breaks` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`breaks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(245) NOT NULL DEFAULT 'Unknown' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Списки наборов перерывов для привязки к дневному расписанию' ;

-- -----------------------------------------------------
-- Table `qsystem`.`break`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`break` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`break` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `breaks_id` BIGINT ,
  `from_time` TIME NOT NULL ,
  `to_time` TIME NOT NULL ,
  PRIMARY KEY (`id`, `breaks_id`) ,
  CONSTRAINT `fk_break_breaks`
    FOREIGN KEY (`breaks_id` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB, 
COMMENT = 'Перерывы в работе для предвариловки' ;

CREATE INDEX `idx_break_breaks1` ON `qsystem`.`break` (`breaks_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`schedule`
-- -----------------------------------------------------

ALTER TABLE `schedule` ADD `breaks_id1` BIGINT NULL ;
ALTER TABLE `schedule` ADD `breaks_id2` BIGINT NULL;
ALTER TABLE `schedule` ADD  `breaks_id3` BIGINT NULL;
ALTER TABLE `schedule` ADD  `breaks_id4` BIGINT NULL;
ALTER TABLE `schedule` ADD  `breaks_id5` BIGINT NULL;
ALTER TABLE `schedule` ADD  `breaks_id6` BIGINT NULL;
ALTER TABLE `schedule` ADD   `breaks_id7` BIGINT NULL;
ALTER TABLE `schedule` 
    ADD  CONSTRAINT `fk_schedule_breaks1`
    FOREIGN KEY (`breaks_id1` )
    REFERENCES `qsystem`.`breaks`(`id` )
    ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `schedule` 
    ADD   CONSTRAINT `fk_schedule_breaks2`
    FOREIGN KEY (`breaks_id2` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE `schedule` ADD  CONSTRAINT `fk_schedule_breaks3`
    FOREIGN KEY (`breaks_id7` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE `schedule` ADD  CONSTRAINT `fk_schedule_breaks4`
    FOREIGN KEY (`breaks_id3` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE `schedule` ADD CONSTRAINT `fk_schedule_breaks5`
    FOREIGN KEY (`breaks_id4` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE `schedule` ADD CONSTRAINT `fk_schedule_breaks6`
    FOREIGN KEY (`breaks_id5` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE `schedule` ADD CONSTRAINT `fk_schedule_breaks7`
    FOREIGN KEY (`breaks_id6` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
    

CREATE INDEX `idx_schedule_breaks1` ON `qsystem`.`schedule` (`breaks_id1` ASC) ;

CREATE INDEX `idx_schedule_breaks2` ON `qsystem`.`schedule` (`breaks_id2` ASC) ;

CREATE INDEX `idx_schedule_breaks3` ON `qsystem`.`schedule` (`breaks_id7` ASC) ;

CREATE INDEX `idx_schedule_breaks4` ON `qsystem`.`schedule` (`breaks_id3` ASC) ;

CREATE INDEX `idx_schedule_breaks5` ON `qsystem`.`schedule` (`breaks_id4` ASC) ;

CREATE INDEX `idx_schedule_breaks6` ON `qsystem`.`schedule` (`breaks_id5` ASC) ;

CREATE INDEX `idx_schedule_breaks7` ON `qsystem`.`schedule` (`breaks_id6` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '1.8' where id<>-1;

COMMIT;

