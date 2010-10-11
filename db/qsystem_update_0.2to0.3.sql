-- -----------------------------------------------------
-- Table `qsystem`.`advance`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `advance` ;

CREATE  TABLE IF NOT EXISTS `advance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `service_id` BIGINT NOT NULL COMMENT 'Услуга предварительной записи' ,
  `advance_time` DATETIME NOT NULL COMMENT 'Время предварительной записи' ,
  `priority` INT NOT NULL DEFAULT 2 COMMENT 'Приоритет заранее записавшегося клиента.' ,
  `site_mark` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Маркировка сайта. если используется доменная структура' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Таблица предварительной записи';

CREATE INDEX `fk_scenario_services` ON `advance` (`service_id` ASC) ;

ALTER TABLE services ADD `advance_limit` INT NOT NULL DEFAULT 1 COMMENT 'Ограничение по количеству предварительно регистрировшихся в час';

USE `qsystem`;

DELIMITER //

DROP TRIGGER IF EXISTS `qsystem`.`insert_to_statistic` //
CREATE TRIGGER insert_to_statistic
    AFTER INSERT ON clients
    FOR EACH ROW
BEGIN
    SET @finish_start= TIMEDIFF(NEW.finish_time, NEW.start_time);
    SET @start_starnd = TIMEDIFF(NEW.start_time, NEW.stand_time);
    INSERT
        INTO statistic(user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period)
    VALUES
        (NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time,
        round(
                (HOUR(@finish_start) * 60 * 60 +
                 MINUTE(@finish_start) * 60 +
                 SECOND(@finish_start) + 59)/60),
        round(
                (HOUR(@start_starnd) * 60 * 60 +
                MINUTE(@start_starnd) * 60 +
                SECOND(@start_starnd) + 59)/60)  
        );
END;//


DROP TRIGGER IF EXISTS `qsystem`.`update_to_statistic` //


CREATE TRIGGER update_to_statistic
    AFTER UPDATE ON clients
    FOR EACH ROW
BEGIN
    SET @finish_start= TIMEDIFF(NEW.finish_time, NEW.start_time);
    SET @start_starnd = TIMEDIFF(NEW.start_time, NEW.stand_time);
    INSERT
        INTO statistic(user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period)
    VALUES
        (NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
        round(
                (HOUR(@finish_start) * 60 * 60 +
                 MINUTE(@finish_start) * 60 +
                 SECOND(@finish_start) + 59)/60),
        round(
                (HOUR(@start_starnd) * 60 * 60 +
                MINUTE(@start_starnd) * 60 +
                SECOND(@start_starnd) + 59)/60)
        );
END;//


DELIMITER ;


UPDATE net SET version = '0.3 updated from 0.2';