USE `qsystem`;
SET AUTOCOMMIT=0;

-- -----------------------------------------------------
-- Table `qsystem`.`users`
-- -----------------------------------------------------

ALTER TABLE `users` ADD `point_ext` VARCHAR(1045) NOT NULL DEFAULT '' COMMENT 'Вывод в третью колонку на главном табло. html + клиент ###  окно @@@';


-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
UPDATE net SET version = '2.2' where id<>-1;

COMMIT;

