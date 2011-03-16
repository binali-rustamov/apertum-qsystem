SET AUTOCOMMIT=0;
USE `qsystem`;

-- -----------------------------------------------------
-- Data for table `qsystem`.`streets`
-- -----------------------------------------------------

CREATE TABLE `qsystem`.`streets` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `name` varchar(100) NOT NULL,
 PRIMARY KEY  ( `id`, `name` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
AUTO_INCREMENT = 1
ROW_FORMAT = COMPACT
COMMENT = 'Словарь улиц';

-- -----------------------------------------------------
-- Data for table `qsystem`.`clients_authorization`
-- -----------------------------------------------------

CREATE TABLE `qsystem`.`clients_authorization` (
 `id` bigint(20) NOT NULL,
 `name` varchar(45) NOT NULL,
 `surname` varchar(45) NOT NULL,
 `otchestvo` varchar(45) NOT NULL,
 `birthday` date DEFAULT NULL,
 `streets_id` bigint(20) DEFAULT NULL,
 `house` varchar(10) DEFAULT NULL,
 `korp` varchar(10) DEFAULT NULL,
 `flat` varchar(10) DEFAULT NULL,
 `validity` int(11) NOT NULL DEFAULT '-1',
 KEY `fk_clients_authorization_streets` ( `streets_id` ),
 PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
ROW_FORMAT = COMPACT
COMMENT = 'Словарь клиентов для авторизации.';

ALTER TABLE `qsystem`.`clients_authorization` ADD CONSTRAINT `fk_clients_authorization_streets`
 FOREIGN KEY ( `streets_id` ) REFERENCES `streets` ( `id` ) ON DELETE SET NULL ON UPDATE CASCADE;

-- -----------------------------------------------------
-- Data for table `qsystem`.`advance`
-- -----------------------------------------------------

DROP TABLE IF EXISTS `qsystem`.`_temp_advance`;

CREATE TABLE `qsystem`.`_temp_advance` (
 `id` bigint(20) NOT NULL,
 `service_id` bigint(20) NOT NULL,
 `advance_time` datetime NOT NULL,
 `priority` int(11) NOT NULL DEFAULT '2',
 `site_mark` varchar(100) NOT NULL,
 `clients_authorization_id` bigint(20) DEFAULT NULL,
 KEY `fk_advance_clients_authorization` ( `clients_authorization_id` ),
 KEY `fk_scenario_services` ( `service_id` ),
 PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
ROW_FORMAT = COMPACT
COMMENT = 'Таблица предварительной записи';

INSERT INTO `qsystem`.`_temp_advance`(`advance_time`,
                                          `id`,
                                          `priority`,
                                          `service_id`,
                                          `site_mark`)
   SELECT `advance_time`,
          `id`,
          `priority`,
          `service_id`,
          `site_mark`
     FROM `qsystem`.`advance`;

DROP TABLE `qsystem`.`advance`;

ALTER TABLE `qsystem`.`_temp_advance` RENAME `advance`;

ALTER TABLE `qsystem`.`advance` ADD CONSTRAINT `fk_advance_clients_authorization`
 FOREIGN KEY ( `clients_authorization_id` ) REFERENCES `clients_authorization` ( `id` ) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `qsystem`.`advance` ADD CONSTRAINT `fk_scenario_services`
 FOREIGN KEY ( `service_id` ) REFERENCES `services` ( `id` ) ON DELETE CASCADE ON UPDATE CASCADE;
 
-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar`
-- -----------------------------------------------------

CREATE TABLE `qsystem`.`calendar` (
 `id` bigint(20) NOT NULL,
 `name` varchar(45) NOT NULL,
 PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
ROW_FORMAT = COMPACT
COMMENT = 'Календарь услуг на год';

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------

CREATE TABLE `qsystem`.`calendar_out_days` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `out_day` date NOT NULL,
 `calendar_id` bigint(20) NOT NULL,
 KEY `fk_calendar_out_days_calendar` ( `calendar_id` ),
 PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
AUTO_INCREMENT = 59
ROW_FORMAT = COMPACT
COMMENT = 'Дни неработы услуг';

ALTER TABLE `qsystem`.`calendar_out_days` ADD CONSTRAINT `fk_calendar_out_days_calendar`
 FOREIGN KEY ( `calendar_id` ) REFERENCES `calendar` ( `id` ) ON DELETE CASCADE ON UPDATE CASCADE;
 
 
 -- -----------------------------------------------------
-- Data for table `qsystem`.`results`
-- -----------------------------------------------------

CREATE TABLE `qsystem`.`results` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `name` varchar(150) NOT NULL,
 PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
AUTO_INCREMENT = 3
ROW_FORMAT = COMPACT
COMMENT = 'Справочник результатов работы с клиентом';
 
-- -----------------------------------------------------
-- Data for table `qsystem`.`clients`
-- -----------------------------------------------------

ALTER TABLE `qsystem`.`clients` ADD 
 `clients_authorization_id` bigint(20) DEFAULT NULL;
 
 ALTER TABLE `qsystem`.`clients` ADD 
 `result_id` bigint(20) DEFAULT NULL;
 
 ALTER TABLE `qsystem`.`clients` ADD 
 `input_data` varchar(150) NOT NULL DEFAULT '' ;
 
 ALTER TABLE `qsystem`.`clients` ADD 
 KEY `fk_clients_clients_authorization` ( `clients_authorization_id` );
 
 ALTER TABLE `qsystem`.`clients` ADD 
 KEY `fk_clients_results` ( `result_id` );


ALTER TABLE `qsystem`.`clients` ADD CONSTRAINT `fk_clients_clients_authorization`
 FOREIGN KEY ( `clients_authorization_id` ) REFERENCES `clients_authorization` ( `id` ) ON DELETE SET NULL ON UPDATE SET NULL;

ALTER TABLE `qsystem`.`clients` ADD CONSTRAINT `fk_clients_results`
 FOREIGN KEY ( `result_id` ) REFERENCES `results` ( `id` ) ON DELETE SET NULL ON UPDATE SET NULL;
 
-- -----------------------------------------------------
-- Data for table `qsystem`.`information`
-- -----------------------------------------------------

ALTER TABLE `qsystem`.`information` ADD 
 `text_print` varchar(5000) NOT NULL DEFAULT '' ;

-- -----------------------------------------------------
-- Data for table `qsystem`.`schedule`
-- -----------------------------------------------------

CREATE TABLE `qsystem`.`schedule` (
 `id` bigint(20) NOT NULL,
 `name` varchar(150) NOT NULL,
 `type` int(11) NOT NULL DEFAULT '0',
 `time_begin_1` time DEFAULT NULL,
 `time_end_1` time DEFAULT NULL,
 `time_begin_2` time DEFAULT NULL,
 `time_end_2` time DEFAULT NULL,
 `time_begin_3` time DEFAULT NULL,
 `time_end_3` time DEFAULT NULL,
 `time_begin_4` time DEFAULT NULL,
 `time_end_4` time DEFAULT NULL,
 `time_begin_5` time DEFAULT NULL,
 `time_end_5` time DEFAULT NULL,
 `time_begin_6` time DEFAULT NULL,
 `time_end_6` time DEFAULT NULL,
 `time_begin_7` time DEFAULT NULL,
 `time_end_7` time DEFAULT NULL,
 PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = utf8
ROW_FORMAT = COMPACT
COMMENT = 'Справочник расписаний для услуг';

-- -----------------------------------------------------
-- Data for table `qsystem`.`services`
-- -----------------------------------------------------

ALTER TABLE `qsystem`.`services` ADD 
  `advance_limit_period` int(11) DEFAULT '14';
  
  ALTER TABLE `qsystem`.`services` ADD 
  `schedule_id` bigint(20) DEFAULT NULL;
  
  ALTER TABLE `qsystem`.`services` ADD 
 `input_required` tinyint(1) NOT NULL DEFAULT '0';
  
  ALTER TABLE `qsystem`.`services` ADD 
  `input_caption` varchar(200) NOT NULL DEFAULT 'Введите номер документа';
  
  ALTER TABLE `qsystem`.`services` ADD 
 `result_required` tinyint(1) NOT NULL DEFAULT '0';
  
  ALTER TABLE `qsystem`.`services` ADD 
  `calendar_id` bigint(20) DEFAULT NULL;
  
  ALTER TABLE `qsystem`.`services` ADD 
  `pre_info_html` varchar(2500) NOT NULL DEFAULT '';
  
  ALTER TABLE `qsystem`.`services` ADD 
 `pre_info_print_text` varchar(2500) NOT NULL DEFAULT '';
 
 
 ALTER TABLE `qsystem`.`services` ADD 
 KEY `fk_services_calendar` ( `calendar_id` );
 
 ALTER TABLE `qsystem`.`services` ADD 
 KEY `fk_services_shedule` ( `schedule_id` );
 
ALTER TABLE `qsystem`.`services` ADD CONSTRAINT `fk_services_calendar`
 FOREIGN KEY ( `calendar_id` ) REFERENCES `calendar` ( `id` ) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `qsystem`.`services` ADD CONSTRAINT `fk_services_shedule`
 FOREIGN KEY ( `schedule_id` ) REFERENCES `schedule` ( `id` ) ON DELETE SET NULL ON UPDATE CASCADE;
 
COMMIT;
-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar` (`id`, `name`) VALUES (1, 'Общий календарь');
COMMIT;
-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar_out_days` (`id`, `out_day`, `calendar_id`) VALUES (1, '2010-01-01', 1);
COMMIT;

SET AUTOCOMMIT=0;
USE `qsystem`;
UPDATE `qsystem`.`net` SET version = '1.0';
COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`results`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `results` (`id`, `name`) VALUES (1, 'Обращение отработано');
INSERT INTO `results` (`id`, `name`) VALUES (2, 'Невозможно отработать');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`reports`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;

DELETE FROM  `reports`;


INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (1, 'Статистический отчет в разрезе услуг за период', 'ru.apertum.qsystem.reports.formirovators.StatisticServices', '/ru/apertum/qsystem/reports/templates/statisticServicesPeriod.jasper', 'statistic_period_services');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (2, 'Статистический отчет в разрезе персонала за период', 'ru.apertum.qsystem.reports.formirovators.StatisticUsers', '/ru/apertum/qsystem/reports/templates/statisticUsersPeriod.jasper', 'statistic_period_users');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (3, 'Отчет по распределению клиентов по виду услуг за период', 'ru.apertum.qsystem.reports.formirovators.RatioServices', '/ru/apertum/qsystem/reports/templates/ratioServicesPeriod.jasper', 'ratio_period_services');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (4, 'Распределение нагрузки внутри дня', 'ru.apertum.qsystem.reports.formirovators.DistributionJobDay', '/ru/apertum/qsystem/reports/templates/DistributionJobDay.jasper', 'distribution_job_day');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (5, 'Распредение нагрузки внутри дня для услуги', 'ru.apertum.qsystem.reports.formirovators.DistributionJobDayServices', '/ru/apertum/qsystem/reports/templates/DistributionJobDayServices.jasper', 'distribution_job_services');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (6, 'Распределение нагрузки внутри дня для пользователя', 'ru.apertum.qsystem.reports.formirovators.DistributionJobDayUsers', '/ru/apertum/qsystem/reports/templates/DistributionJobDayUsers.jasper', 'distribution_job_users');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (7, 'Распределение среднего времени ожидания внутри дня', 'ru.apertum.qsystem.reports.formirovators.DistributionWaitDay', '/ru/apertum/qsystem/reports/templates/DistributionWaitDay.jasper', 'distribution_wait_day');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (8, 'Распределение среднего времени ожидания внутри дня для услуги', 'ru.apertum.qsystem.reports.formirovators.DistributionWaitDayServices', '/ru/apertum/qsystem/reports/templates/DistributionWaitDayServices.jasper', 'distribution_wait_services');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (9, 'Распределение среднего времени ожидания внутри дня для пользователя', 'ru.apertum.qsystem.reports.formirovators.DistributionWaitDayUsers', '/ru/apertum/qsystem/reports/templates/DistributionWaitDayUsers.jasper', 'distribution_wait_users');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (10, 'Статистический отчет по отзывам клиентов за период', 'ru.apertum.qsystem.reports.formirovators.ResponsesReport', '/ru/apertum/qsystem/reports/templates/responsesReport.jasper', 'statistic_period_responses');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (11, 'Статистический отчет распределения активности клиентов за период', 'ru.apertum.qsystem.reports.formirovators.ResponsesDateReport', '/ru/apertum/qsystem/reports/templates/responsesDateReport.jasper', 'statistic_period_date_responses');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (12, 'Отчет предварительно зарегистрированных клиентов по услуге на дату', 'ru.apertum.qsystem.reports.formirovators.DistributionMedDayServices', '/ru/apertum/qsystem/reports/templates/DistributionMedDayServices.jasper', 'distribution_med_services');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (13, 'Отчет по авторизованным персонам за период для пользователя', 'ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodUsers', '/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodUsers.jasper', 'authorized_clients_period_users');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (14, 'Отчет по авторизованным персонам за период для услуги', 'ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodServices', '/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodServices.jasper', 'authorized_clients_period_services');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`schedule`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;

INSERT INTO `qsystem`.`schedule` (id,name,type,time_begin_1,time_end_1,time_begin_2,time_end_2,time_begin_3,time_end_3,time_begin_4,time_end_4,time_begin_5,time_end_5,time_begin_6,time_end_6,time_begin_7,time_end_7) 
VALUES 
(1,'План работы',
0,
'08:00:00',
'23:00:00','08:00:00','23:00:00','08:00:00','23:00:00','08:00:00','23:00:00','08:00:00','23:00:00','08:00:00','23:00:00','08:00:00','23:00:00');
COMMIT;

SET AUTOCOMMIT=0;
USE `qsystem`;
UPDATE `qsystem`.`services` SET schedule_id=1, calendar_id=1;
COMMIT;

