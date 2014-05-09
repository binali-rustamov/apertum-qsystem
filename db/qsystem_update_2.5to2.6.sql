USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `services` ADD `expectation` INT NOT NULL DEFAULT 0 COMMENT 'Время обязательного ожидания посетителя' ;

-- -----------------------------------------------------
-- Table `qsystem`.`clients`
-- -----------------------------------------------------

ALTER TABLE `clients` ADD `state_in` INT NOT NULL DEFAULT 0 COMMENT 'клиент перешел в это состояние.' ;

-- -----------------------------------------------------
-- Table `qsystem`.`statistic`
-- -----------------------------------------------------

ALTER TABLE `statistic` ADD `state_in` INT NOT NULL DEFAULT 0 COMMENT 'Клиент перешел в это состояние';

UPDATE statistic SET state_in = 10 where id<>-1;

-- -----------------------------------------------------
-- TRIGGERS
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
        INTO statistic(state_in, results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.state_in, NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
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
        INTO statistic(state_in, results_id, user_id, client_id, service_id, user_start_time, user_finish_time, client_stand_time, user_work_period, client_wait_period) 
    VALUES
        (NEW.state_in, NEW.result_id, NEW.user_id, NEW.id, NEW.service_id, NEW.start_time, NEW.finish_time, NEW.stand_time, 
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
-- Table `qsystem`.`reports`
-- -----------------------------------------------------

INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (15, 'Отчет по результатам работы за период в разрезе услуг', 'ru.apertum.qsystem.reports.formirovators.ResultStateServices', '/ru/apertum/qsystem/reports/templates/resultStateServicesPeriod.jasper', 'result_state_services');

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '2.6' where id<>-1;

COMMIT;

