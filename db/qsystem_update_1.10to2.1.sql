USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------

ALTER TABLE `net` ADD `limit_recall` INT NOT NULL DEFAULT 0 COMMENT 'Количество повторных вызовов перед отклонением неявившегося посетителя, 0-бесконечно';

-- -----------------------------------------------------
-- Table `qsystem`.`response_event`
-- -----------------------------------------------------

ALTER TABLE `response_event` ADD `services_id` BIGINT NULL;
ALTER TABLE `response_event` ADD `users_id` BIGINT NULL;
ALTER TABLE `response_event` ADD `clients_id` BIGINT NULL COMMENT 'Клиент оставивший отзыв';
ALTER TABLE `response_event` ADD `client_data` VARCHAR(245) NOT NULL DEFAULT '';
ALTER TABLE `response_event` 
    ADD CONSTRAINT `fk_response_event_services`
    FOREIGN KEY (`services_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE;
	
ALTER TABLE `response_event` 
    ADD CONSTRAINT `fk_response_event_users`
    FOREIGN KEY (`users_id` )
    REFERENCES `qsystem`.`users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE;	
	
ALTER TABLE `response_event` 
    ADD CONSTRAINT `fk_response_event_clients`
    FOREIGN KEY (`clients_id` )
    REFERENCES `qsystem`.`clients` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE;

CREATE INDEX `idx_response_event_services` ON `qsystem`.`response_event` (`services_id` ASC) ;

CREATE INDEX `idx_response_event_users` ON `qsystem`.`response_event` (`users_id` ASC) ;	

CREATE INDEX `idx_response_event_clients` ON `qsystem`.`response_event` (`clients_id` ASC) ;

-- -----------------------------------------------------
-- Table `qsystem`.`advance`
-- -----------------------------------------------------

ALTER TABLE `advance` ADD `comments` VARCHAR(345) NULL DEFAULT '';

-- -----------------------------------------------------
-- Table `qsystem`.`standards`
-- -----------------------------------------------------

CREATE  TABLE IF NOT EXISTS `qsystem`.`standards` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `wait_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное время ожидания, в минутах' ,
  `work_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное время работы с одним клиентом, в минутах' ,
  `downtime_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное время простоя при наличии очереди, в минутах' ,
  `line_service_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальная длинна очереди к одной услуге' ,
  `line_total_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное количество клиентов ко всем услугам' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;

INSERT INTO `qsystem`.`standards` (`id`, `wait_max`, `work_max`, `downtime_max`, `line_service_max`, `line_total_max`) VALUES (1, 0, 0, 0, 0, 0);

-- -----------------------------------------------------
-- Table `qsystem`.`statistic`
-- -----------------------------------------------------

ALTER TABLE `statistic` ADD`results_id` BIGINT NULL;

ALTER TABLE `statistic` 
    ADD CONSTRAINT `fk_statistic_results`
    FOREIGN KEY (`results_id` )
    REFERENCES `qsystem`.`results` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL;
	
CREATE INDEX `idx_statistic_results` ON `qsystem`.`statistic` (`results_id` ASC) ;

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
        INTO statistic(results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
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
        INTO statistic(results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
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
UPDATE net SET version = '2.1' where id<>-1;

COMMIT;

