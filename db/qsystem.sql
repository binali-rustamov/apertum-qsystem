SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `qsystem` ;
USE `qsystem`;

-- -----------------------------------------------------
-- Table `qsystem`.`schedule`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`schedule` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`schedule` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(150) NOT NULL DEFAULT '' COMMENT 'Наименование плана' ,
  `type` INT NOT NULL DEFAULT 0 COMMENT 'Тип плана\n0 - недельный\n1 - четные/нечетные дни' ,
  `time_begin_1` TIME NULL COMMENT 'Время начала работы' ,
  `time_end_1` TIME NULL COMMENT 'Время завершения работы' ,
  `time_begin_2` TIME NULL ,
  `time_end_2` TIME NULL ,
  `time_begin_3` TIME NULL ,
  `time_end_3` TIME NULL ,
  `time_begin_4` TIME NULL ,
  `time_end_4` TIME NULL ,
  `time_begin_5` TIME NULL ,
  `time_end_5` TIME NULL ,
  `time_begin_6` TIME NULL ,
  `time_end_6` TIME NULL ,
  `time_begin_7` TIME NULL ,
  `time_end_7` TIME NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Справочник расписаний для услуг';


-- -----------------------------------------------------
-- Table `qsystem`.`calendar`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`calendar` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`calendar` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Название календаря' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Календарь услуг на год';


-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`services` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`services` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(2000) NOT NULL COMMENT 'Наименование услуги' ,
  `description` VARCHAR(200) NULL COMMENT 'Описание услуги.' ,
  `service_prefix` VARCHAR(10) NULL DEFAULT '' ,
  `button_text` VARCHAR(2500) NOT NULL DEFAULT '' COMMENT 'HTML-текст для вывода на кнопки регистрации.' ,
  `status` INT NOT NULL DEFAULT 1 COMMENT 'Состояние услуги. 1 - доступна, 0 - недоступна, -1 - невидима.' ,
  `enable` INT NOT NULL DEFAULT 1 COMMENT 'Дейсткующия услуга или удаленная.' ,
  `prent_id` BIGINT NULL COMMENT 'Групповое подчинение.' ,
  `advance_limit` INT NOT NULL DEFAULT 1 COMMENT 'Ограничение по количеству предварительно регистрировшихся в час' ,
  `schedule_id` BIGINT NULL COMMENT 'План работы услуги' ,
  `input_required` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Обязывать кастомера вводить что-то перед постоновкой в очередь' ,
  `input_caption` VARCHAR(200) NOT NULL DEFAULT 'Введите номер документа' COMMENT 'Текст над полем ввода обязательного ввода' ,
  `result_required` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Требовать ввод пользователем результата работы с клиентом' ,
  `calendar_id` BIGINT NULL ,
  `pre_info_html` VARCHAR(2500) NOT NULL DEFAULT '' COMMENT 'html текст информационного сообщения перед постановкой в очередь' ,
  `pre_info_print_text` VARCHAR(2500) NOT NULL DEFAULT '' COMMENT 'текст для печати при необходимости перед постановкой в очередь' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_servises_parent_id_servises_id`
    FOREIGN KEY (`prent_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_services_shedule`
    FOREIGN KEY (`schedule_id` )
    REFERENCES `qsystem`.`schedule` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_services_calendar`
    FOREIGN KEY (`calendar_id` )
    REFERENCES `qsystem`.`calendar` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
COMMENT = 'Дерево услуг';

CREATE INDEX `fk_servises_parent_id_servises_id` ON `qsystem`.`services` (`prent_id` ASC) ;

CREATE INDEX `fk_services_shedule` ON `qsystem`.`services` (`schedule_id` ASC) ;

CREATE INDEX `fk_services_calendar` ON `qsystem`.`services` (`calendar_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`users` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор пользователя.' ,
  `name` VARCHAR(150) NOT NULL COMMENT 'Наименование' ,
  `password` VARCHAR(45) NOT NULL COMMENT 'Пароль пользователя.' ,
  `point` VARCHAR(45) NOT NULL COMMENT 'Идентификация рабочего места' ,
  `adress_rs` SMALLINT NOT NULL DEFAULT 0 COMMENT 'Адрес табло  пользователя в герлянде RS485' ,
  `enable` INT NOT NULL DEFAULT 1 COMMENT 'Дейсткующий пользователь или удаленный.' ,
  `admin_access` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Доступ к администрирования системы.' ,
  `report_access` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Доступ к получению отчетов.' ,
  PRIMARY KEY (`id`) )
COMMENT = 'Пользователи системы.';


-- -----------------------------------------------------
-- Table `qsystem`.`streets`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`streets` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`streets` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(100) NOT NULL COMMENT 'Наименование улицы.' ,
  PRIMARY KEY (`id`, `name`) )
ENGINE = InnoDB
COMMENT = 'Словарь улиц';


-- -----------------------------------------------------
-- Table `qsystem`.`clients_authorization`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`clients_authorization` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`clients_authorization` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Имя' ,
  `surname` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Фамилие' ,
  `otchestvo` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Отчество, иногда может отсутствовать.' ,
  `birthday` DATE NULL COMMENT 'Дата рождения' ,
  `streets_id` BIGINT NULL COMMENT 'Связь со словарем улиц. Проживание.' ,
  `house` VARCHAR(10) NULL DEFAULT '' COMMENT 'Номер дома' ,
  `korp` VARCHAR(10) NULL DEFAULT '' COMMENT 'Корпус дома' ,
  `flat` VARCHAR(10) NULL DEFAULT '' COMMENT 'Квартира' ,
  `validity` INT NOT NULL DEFAULT -1 COMMENT 'Степень валидности авторизованного клиента' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_clients_authorization_streets`
    FOREIGN KEY (`streets_id` )
    REFERENCES `qsystem`.`streets` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Словарь клиентов для авторизации.';

CREATE INDEX `fk_clients_authorization_streets` ON `qsystem`.`clients_authorization` (`streets_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`results`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`results` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`results` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(150) NOT NULL DEFAULT '' COMMENT 'Текст результата' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Справочник результатов работы с клиентом';


-- -----------------------------------------------------
-- Table `qsystem`.`clients`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`clients` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`clients` (
  `id` BIGINT NOT NULL COMMENT 'Первичный ключ.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `service_id` BIGINT NOT NULL COMMENT 'Услуга, к которой  пришел первоначально кастомер.  Вспомогательное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `user_id` BIGINT NOT NULL COMMENT ' Вспомогательное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `service_prefix` VARCHAR(45) NOT NULL COMMENT 'Префикс номера кастомера. Информационное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `number` INT NOT NULL COMMENT 'Номер клиента без префикса. Информационное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `stand_time` DATETIME NOT NULL COMMENT 'Время постановки в очередь. Информационное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `start_time` DATETIME NOT NULL COMMENT 'Время начала обработки клиента пользователем. Вспомогательное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `finish_time` DATETIME NOT NULL COMMENT 'Время завершения работы с клиентом пользователем. Информационное поле.\nВнимание! Вставлять и апдейтить записи только по завершению работы пользователя с кастомером.' ,
  `clients_authorization_id` BIGINT NULL COMMENT 'Определено если клиент авторизовался' ,
  `result_id` BIGINT NULL COMMENT 'Если выбрали результат работы' ,
  `input_data` VARCHAR(150) NOT NULL DEFAULT '' COMMENT 'Введенные данные пользователем' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_сlients_service_id_services_id`
    FOREIGN KEY (`service_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_сlients_user_id_users_id`
    FOREIGN KEY (`user_id` )
    REFERENCES `qsystem`.`users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_clients_clients_authorization`
    FOREIGN KEY (`clients_authorization_id` )
    REFERENCES `qsystem`.`clients_authorization` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL,
  CONSTRAINT `fk_clients_results`
    FOREIGN KEY (`result_id` )
    REFERENCES `qsystem`.`results` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL)
COMMENT = 'Таблица регистрации статистических событий клиентов.';

CREATE INDEX `fk_сlients_service_id_services_id` ON `qsystem`.`clients` (`service_id` ASC) ;

CREATE INDEX `fk_сlients_user_id_users_id` ON `qsystem`.`clients` (`user_id` ASC) ;

CREATE INDEX `fk_clients_clients_authorization` ON `qsystem`.`clients` (`clients_authorization_id` ASC) ;

CREATE INDEX `fk_clients_results` ON `qsystem`.`clients` (`result_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`net`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`net` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`net` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'ПортСервера=\"3128\" ПортВебСервера=\"8080\" ПортКлиента=\"3129\" АдресСервера=\"localhost\"' ,
  `server_port` INT NOT NULL COMMENT 'Серверный порт приема заданий по сети от клиетских приложений.' ,
  `web_server_port` INT NOT NULL COMMENT 'Серверный порт для приема web запросов в системе отчетов.' ,
  `client_port` INT NOT NULL COMMENT 'UDP Порт клиента, на который идет рассылка широковещательных пакетов.' ,
  `finish_time` TIME NOT NULL COMMENT 'Время прекращения приема заявок на постановку в очередь' ,
  `start_time` TIME NOT NULL COMMENT 'Время начала приема заявок на постановку в очередь' ,
  `super_site` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Является ли сервер суперсайтом' ,
  `version` VARCHAR(25) NOT NULL DEFAULT 'Не присвоена' COMMENT 'Версия БД' ,
  PRIMARY KEY (`id`) )
COMMENT = 'Сетевые настройки сервера.';


-- -----------------------------------------------------
-- Table `qsystem`.`services_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`services_users` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`services_users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `service_id` BIGINT NOT NULL ,
  `user_id` BIGINT NOT NULL ,
  `coefficient` INT NOT NULL DEFAULT 1 COMMENT 'Коэффициент участия.' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_services_id_su_service_id`
    FOREIGN KEY (`service_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_userss_id_su_user_id`
    FOREIGN KEY (`user_id` )
    REFERENCES `qsystem`.`users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
COMMENT = 'Таблица соответствий услуга - пользователь.';

CREATE INDEX `fk_services_id_su_service_id` ON `qsystem`.`services_users` (`service_id` ASC) ;

CREATE INDEX `fk_userss_id_su_user_id` ON `qsystem`.`services_users` (`user_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`statistic`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`statistic` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`statistic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `user_id` BIGINT NOT NULL ,
  `client_id` BIGINT NOT NULL ,
  `service_id` BIGINT NOT NULL ,
  `user_start_time` DATETIME NOT NULL COMMENT 'Время начала обработки кастомера юзером.' ,
  `user_finish_time` DATETIME NOT NULL COMMENT 'Время завершения обработки кастомера юзером.' ,
  `client_stand_time` DATETIME NOT NULL COMMENT 'Время постановки кастомера в очередь' ,
  `user_work_period` INT NOT NULL COMMENT 'Время работы пользователя с клиентом в минутах.' ,
  `client_wait_period` INT NOT NULL COMMENT 'Время ожидания в минутах. Определяется триггером.' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_work_user_id_users_id`
    FOREIGN KEY (`user_id` )
    REFERENCES `qsystem`.`users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_work_сlient_id_сlients_id`
    FOREIGN KEY (`client_id` )
    REFERENCES `qsystem`.`clients` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_work_service_id_services_id`
    FOREIGN KEY (`service_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
COMMENT = 'События работы пользователя с клиентом.Формируется триггером';

CREATE INDEX `fk_work_user_id_users_id` ON `qsystem`.`statistic` (`user_id` ASC) ;

CREATE INDEX `fk_work_сlient_id_сlients_id` ON `qsystem`.`statistic` (`client_id` ASC) ;

CREATE INDEX `fk_work_service_id_services_id` ON `qsystem`.`statistic` (`service_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`reports`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`reports` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`reports` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NOT NULL COMMENT 'Название отчета, выводимое пользователю.' ,
  `className` VARCHAR(150) NOT NULL COMMENT 'Класс формирования отчета. Полное наименование класса с пакетами.' ,
  `template` VARCHAR(150) NOT NULL COMMENT 'Шаблон отчета. Хранится в отдельном пакете в jar.' ,
  `href` VARCHAR(150) NOT NULL COMMENT 'Ссылка на отчет в index.html. Без расширения типа файла.' ,
  PRIMARY KEY (`id`, `href`) )
COMMENT = 'Зарегистрированные аналитические отчеты.';


-- -----------------------------------------------------
-- Table `qsystem`.`sites`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`sites` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`sites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `address` VARCHAR(500) NOT NULL COMMENT 'Адрес сервера сайта' ,
  `server_port` INT NOT NULL COMMENT 'Порт сервера сайта' ,
  `web_server_port` INT NOT NULL COMMENT 'Порт отчетного веб-сервера' ,
  `button_text` VARCHAR(2500) NOT NULL COMMENT 'html-текст сайта на кнопке пункта регистрации' ,
  `description` VARCHAR(2000) NULL COMMENT 'Комментарии к сайту' ,
  `name` VARCHAR(2000) NULL COMMENT 'Наименование сайта' ,
  PRIMARY KEY (`id`) )
COMMENT = 'Список сайтов домена для суперсайта';


-- -----------------------------------------------------
-- Table `qsystem`.`advance`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`advance` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`advance` (
  `id` BIGINT NOT NULL ,
  `service_id` BIGINT NOT NULL COMMENT 'Услуга предварительной записи' ,
  `advance_time` DATETIME NOT NULL COMMENT 'Время предварительной записи' ,
  `priority` INT NOT NULL DEFAULT 2 COMMENT 'Приоритет заранее записавшегося клиента.' ,
  `site_mark` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'Маркировка сайта. если используется доменная структура' ,
  `clients_authorization_id` BIGINT NULL COMMENT 'Определено если клиент авторизовался' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_scenario_services`
    FOREIGN KEY (`service_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_advance_clients_authorization`
    FOREIGN KEY (`clients_authorization_id` )
    REFERENCES `qsystem`.`clients_authorization` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Таблица предварительной записи';

CREATE INDEX `fk_scenario_services` ON `qsystem`.`advance` (`service_id` ASC) ;

CREATE INDEX `fk_advance_clients_authorization` ON `qsystem`.`advance` (`clients_authorization_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`information`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`information` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`information` (
  `id` BIGINT NOT NULL ,
  `parent_id` BIGINT NULL ,
  `name` VARCHAR(100) NOT NULL COMMENT 'Наименование узла справки' ,
  `text` VARCHAR(5000) NOT NULL DEFAULT '' COMMENT 'html-текст справки' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_information_information1`
    FOREIGN KEY (`parent_id` )
    REFERENCES `qsystem`.`information` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Таблица справочной информации древовидной структуры';

CREATE INDEX `fk_information_information1` ON `qsystem`.`information` (`parent_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`responses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`responses` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`responses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(100) NOT NULL DEFAULT '' ,
  `text` VARCHAR(5000) NOT NULL DEFAULT '' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Список отзывов в отратной связи';


-- -----------------------------------------------------
-- Table `qsystem`.`response_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`response_event` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`response_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `resp_date` DATETIME NOT NULL COMMENT 'Дата отклика' ,
  `response_id` BIGINT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_response_date_responses1`
    FOREIGN KEY (`response_id` )
    REFERENCES `qsystem`.`responses` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Даты оставленных отзывов.';

CREATE INDEX `fk_response_date_responses1` ON `qsystem`.`response_event` (`response_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`calendar_out_days` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`calendar_out_days` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `out_day` DATE NOT NULL COMMENT 'Дата неработы. Важен месяц и день' ,
  `calendar_id` BIGINT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_calendar_out_days_calendar`
    FOREIGN KEY (`calendar_id` )
    REFERENCES `qsystem`.`calendar` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
COMMENT = 'Дни неработы услуг';

CREATE INDEX `fk_calendar_out_days_calendar` ON `qsystem`.`calendar_out_days` (`calendar_id` ASC) ;

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


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `qsystem`.`schedule`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `schedule` (`id`, `name`, `type`, `time_begin_1`, `time_end_1`, `time_begin_2`, `time_end_2`, `time_begin_3`, `time_end_3`, `time_begin_4`, `time_end_4`, `time_begin_5`, `time_end_5`, `time_begin_6`, `time_end_6`, `time_begin_7`, `time_end_7`) VALUES (1, 'План работы с 8.00 до 17.00', 0, '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00');
INSERT INTO `schedule` (`id`, `name`, `type`, `time_begin_1`, `time_end_1`, `time_begin_2`, `time_end_2`, `time_begin_3`, `time_end_3`, `time_begin_4`, `time_end_4`, `time_begin_5`, `time_end_5`, `time_begin_6`, `time_end_6`, `time_begin_7`, `time_end_7`) VALUES (2, 'План работы по четным/нечетным', 1, '08:00:00', '13:00:00', '12:00:00', '17:00:00', null, null, null, null, null, null, null, null, null, null);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar` (`id`, `name`) VALUES (1, 'Общий календарь');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`services`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `services` (`id`, `name`, `description`, `service_prefix`, `button_text`, `status`, `enable`, `prent_id`, `advance_limit`, `schedule_id`, `input_required`, `input_caption`, `result_required`, `calendar_id`, `pre_info_html`, `pre_info_print_text`) VALUES (1, 'Дерево услуг', 'Дерево услуг', '-', '<html><b><p align=center><u><span style=\'font-size:30.0pt;color:blue\'>Предлагаемые услуги</span></u><br><br><span style=\'font-size:20.0pt;color:red\'>выберите требуемую услугу', 1, 1, null, 1, null, false, '', false, null, '', '');
INSERT INTO `services` (`id`, `name`, `description`, `service_prefix`, `button_text`, `status`, `enable`, `prent_id`, `advance_limit`, `schedule_id`, `input_required`, `input_caption`, `result_required`, `calendar_id`, `pre_info_html`, `pre_info_print_text`) VALUES (2, 'Услуга', 'Описание услуги', 'А', '<html><b><p align=center><span style=\'font-size:20.0pt;color:blue\'>Некая услуга', 1, 1, 1, 1, 1, false, '', false, 1, '', '');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`users`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `users` (`id`, `name`, `password`, `point`, `adress_rs`, `enable`, `admin_access`, `report_access`) VALUES (1, 'Администратор', '', '1', 32, 1, true, true);
INSERT INTO `users` (`id`, `name`, `password`, `point`, `adress_rs`, `enable`, `admin_access`, `report_access`) VALUES (2, 'Пользователь', '', '2', 33, 1, false, false);

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
-- Data for table `qsystem`.`net`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `net` (`id`, `server_port`, `web_server_port`, `client_port`, `finish_time`, `start_time`, `super_site`, `version`) VALUES (1, 3128, 8088, 3129, '18:00:00', '8:45:00', false, '0.9');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`services_users`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `services_users` (`id`, `service_id`, `user_id`, `coefficient`) VALUES (1, 2, 2, 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`reports`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
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
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (12, 'Распредение нагрузки внутри дня для услуги', 'ru.apertum.qsystem.reports.formirovators.DistributionMedDayServices', '/ru/apertum/qsystem/reports/templates/DistributionMedDayServices.jasper', 'distribution_med_services');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (13, 'Отчет по авторизованным персонам за период для пользователя', 'ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodUsers', '/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodUsers.jasper', 'authorized_clients_period_users');
INSERT INTO `reports` (`id`, `name`, `className`, `template`, `href`) VALUES (14, 'Отчет по авторизованным персонам за период для услуги', 'ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodServices', '/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodServices.jasper', 'authorized_clients_period_services');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`sites`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `sites` (`id`, `address`, `server_port`, `web_server_port`, `button_text`, `description`, `name`) VALUES (1, '127.0.0.1', 3128, 8088, 'Локальный сайт', 'Локальный сайт', 'Локальный сайт');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`information`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `information` (`id`, `parent_id`, `name`, `text`) VALUES (1, null, 'Справочная система', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Справвочная информационная система.<br>Для  получения детальной информации обратитесь к менеджеру</span></b>');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`responses`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (1, 'Отлично', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Отлично</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (2, 'Хорошо', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Хорошо</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (3, 'Удовлетворительно', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Удовлетворительно</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (4, 'Плохо', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Плохо</span></b>');
INSERT INTO `responses` (`id`, `name`, `text`) VALUES (5, 'Отвратительно', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Отвратительно</span></b>');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `qsystem`;
INSERT INTO `calendar_out_days` (`id`, `out_day`, `calendar_id`) VALUES (1, '2010-01-01', 1);

COMMIT;
