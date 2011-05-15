USE `qsystem`;

-- -----------------------------------------------------
-- Drop Table `qsystem`.`sites`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`sites` ;

-- -----------------------------------------------------
-- Table `qsystem`.`advance`
-- -----------------------------------------------------

ALTER TABLE `advance` DROP `site_mark`;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------

ALTER TABLE `net` DROP `super_site`;

ALTER TABLE `net` ADD `first_number` INT NOT NULL DEFAULT 1;
ALTER TABLE `net` ADD `last_number` INT NOT NULL DEFAULT 999;
ALTER TABLE `net` ADD `numering` TINYINT(1) NOT NULL DEFAULT true COMMENT '0 общая нумерация, 1 для каждой услуги своя нумерация';
ALTER TABLE `net` ADD `point` INT NOT NULL DEFAULT 0 COMMENT '0 кабинет, 1 окно, 2 стойка';
ALTER TABLE `net` ADD `sound` INT NOT NULL DEFAULT 2 COMMENT '0 нет оповещения, 1 только сигнал, 2 сигнал+голос';
ALTER TABLE `net` ADD `branch_id` BIGINT NOT NULL DEFAULT 0;
ALTER TABLE `net` ADD `sky_server_url` VARCHAR(145) NOT NULL DEFAULT '';

UPDATE net SET version = '1.1', `first_number`=1, `last_number`=999, `numering`=true, `point`=0, `sound`=2, `branch_id`=0, `sky_server_url`='';

-- -----------------------------------------------------
-- Table `qsystem`.`users_services_users`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `qsystem`.`users_services_users` ;

CREATE TABLE `users_services_users` (
  `planServices_id` bigint(20) NOT NULL,
  `users_id` bigint(20) NOT NULL,
  PRIMARY KEY (`planServices_id`),
  KEY `fk_user` (`users_id`),
  CONSTRAINT `fk_planserv` FOREIGN KEY (`planServices_id`) REFERENCES `services_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Услуги юзеров с приоритетами';



COMMIT;

