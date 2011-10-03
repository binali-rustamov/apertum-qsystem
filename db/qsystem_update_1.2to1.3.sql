USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------

ALTER TABLE `net` ADD `zone_board_serv_port` BIGINT NOT NULL DEFAULT 0;
ALTER TABLE `net` ADD `zone_board_serv_addr` VARCHAR(145) NOT NULL DEFAULT '';

COMMIT;

UPDATE net SET version = '1.3', `zone_board_serv_port`=27007, `zone_board_serv_addr`='127.0.0.1' where id<>-1;

COMMIT;

