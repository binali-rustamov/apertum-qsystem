USE `qsystem`;

SET AUTOCOMMIT=0;

DELETE FROM `qsystem`.`statistic` where `statistic`.id<>-1;

DELETE FROM `qsystem`.`clients` where `clients`.id<>-1;

DELETE FROM `qsystem`.`advance` where `advance`.id<>-1;

COMMIT;