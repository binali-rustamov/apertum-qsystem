	USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`branches`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`branches` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`branches` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(124) NOT NULL DEFAULT '' COMMENT 'название филиала' ,
  `point` INT NOT NULL DEFAULT 1 COMMENT 'номер киоска, проставляется в настройках киоска' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Филиалы с киосками' ;

CREATE UNIQUE INDEX `point_UNIQUE` ON `qsystem`.`branches` (`point` ASC) ;

CREATE UNIQUE INDEX `name_UNIQUE` ON `qsystem`.`branches` (`name` ASC) ;

-- -----------------------------------------------------
-- Data for table `qsystem`.`branches`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`branches` (`id`, `name`, `point`) VALUES (1, 'Во всех отделениях', 0);

COMMIT;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `branches_id` BIGINT NULL DEFAULT 1;

ALTER TABLE `services` 
    ADD CONSTRAINT `fk_services_branches`
    FOREIGN KEY (`branches_id` )
    REFERENCES `qsystem`.`branches` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL;
	
CREATE INDEX `idx_services_branches` ON `qsystem`.`services` (`branches_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`clients`
-- -----------------------------------------------------

ALTER TABLE `clients` ADD`branches_id` BIGINT NULL DEFAULT 1;

ALTER TABLE `clients` 
    ADD CONSTRAINT `fk_clients_branches`
    FOREIGN KEY (`branches_id` )
    REFERENCES `qsystem`.`branches` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL;
	
CREATE INDEX `idx_clients_branches` ON `qsystem`.`clients` (`branches_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`statistic`
-- -----------------------------------------------------

ALTER TABLE `statistic` ADD`branches_id` BIGINT NULL DEFAULT 1;

ALTER TABLE `statistic` 
    ADD CONSTRAINT `fk_statistic_branches`
    FOREIGN KEY (`branches_id` )
    REFERENCES `qsystem`.`branches` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL;
	
CREATE INDEX `idx_statistic_branches` ON `qsystem`.`statistic` (`branches_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`users`
-- -----------------------------------------------------

ALTER TABLE `users` ADD `branches_id` BIGINT NULL DEFAULT 1;

ALTER TABLE `users` 
    ADD CONSTRAINT `fk_users_branches`
    FOREIGN KEY (`branches_id` )
    REFERENCES `qsystem`.`branches` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL;
	
CREATE INDEX `idx_users_branches` ON `qsystem`.`users` (`branches_id` ASC) ;

-- -----------------------------------------------------
-- Triggers
-- -----------------------------------------------------

USE `qsystem`;

DELIMITER $$

USE `qsystem`$$
DROP TRIGGER IF EXISTS `qsystem`.`insert_to_statistic` $$
USE `qsystem`$$


CREATE TRIGGER insert_to_statistic 
    AFTER INSERT ON clients
    FOR EACH ROW
BEGIN
    SET @finish_start= TIMEDIFF(NEW.finish_time, NEW.start_time);
    SET @start_starnd = TIMEDIFF(NEW.start_time, NEW.stand_time);
    INSERT
        INTO statistic(branches_id, results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.branches_id, NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
        round(
                (HOUR(@finish_start) * 60 * 60 +
                 MINUTE(@finish_start) * 60 +
                 SECOND(@finish_start) + 59)/60),
        round(
                (HOUR(@start_starnd) * 60 * 60 +
                MINUTE(@start_starnd) * 60 +
                SECOND(@start_starnd) + 59)/60)  
        );
END;$$


USE `qsystem`$$
DROP TRIGGER IF EXISTS `qsystem`.`update_to_statistic` $$
USE `qsystem`$$


CREATE TRIGGER update_to_statistic
    AFTER UPDATE ON clients
    FOR EACH ROW
BEGIN
    SET @finish_start= TIMEDIFF(NEW.finish_time, NEW.start_time);
    SET @start_starnd = TIMEDIFF(NEW.start_time, NEW.stand_time);
    INSERT
        INTO statistic(branches_id, results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.branches_id, NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
        round(
                (HOUR(@finish_start) * 60 * 60 +
                 MINUTE(@finish_start) * 60 +
                 SECOND(@finish_start) + 59)/60),
        round(
                (HOUR(@start_starnd) * 60 * 60 +
                MINUTE(@start_starnd) * 60 +
                SECOND(@start_starnd) + 59)/60)  
        );
END;$$


DELIMITER ;


-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '2.2' where id<>-1;

COMMIT;

