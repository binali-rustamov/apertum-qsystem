SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `qsystem` ;
CREATE SCHEMA IF NOT EXISTS `qsystem` ;
USE `qsystem` ;

-- -----------------------------------------------------
-- Table `qsystem`.`breaks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`breaks` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`breaks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(245) NOT NULL DEFAULT 'Unknown' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Списки наборов перерывов для привязки к дневному расписанию' ;


-- -----------------------------------------------------
-- Table `qsystem`.`schedule`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`schedule` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`schedule` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(150) NOT NULL DEFAULT '' COMMENT 'Наименование плана' ,
  `type` INT NOT NULL DEFAULT 0 COMMENT 'Тип плана\n0 - недельный\n1 - четные/нечетные дни' ,
  `time_begin_1` TIME NULL DEFAULT NULL COMMENT 'Время начала работы' ,
  `time_end_1` TIME NULL DEFAULT NULL COMMENT 'Время завершения работы' ,
  `time_begin_2` TIME NULL DEFAULT NULL ,
  `time_end_2` TIME NULL DEFAULT NULL ,
  `time_begin_3` TIME NULL DEFAULT NULL ,
  `time_end_3` TIME NULL DEFAULT NULL ,
  `time_begin_4` TIME NULL DEFAULT NULL ,
  `time_end_4` TIME NULL DEFAULT NULL ,
  `time_begin_5` TIME NULL DEFAULT NULL ,
  `time_end_5` TIME NULL DEFAULT NULL ,
  `time_begin_6` TIME NULL DEFAULT NULL ,
  `time_end_6` TIME NULL DEFAULT NULL ,
  `time_begin_7` TIME NULL DEFAULT NULL ,
  `time_end_7` TIME NULL DEFAULT NULL ,
  `breaks_id1` BIGINT NULL ,
  `breaks_id2` BIGINT NULL ,
  `breaks_id3` BIGINT NULL ,
  `breaks_id4` BIGINT NULL ,
  `breaks_id5` BIGINT NULL ,
  `breaks_id6` BIGINT NULL ,
  `breaks_id7` BIGINT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_schedule_breaks1`
    FOREIGN KEY (`breaks_id1` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks2`
    FOREIGN KEY (`breaks_id2` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks3`
    FOREIGN KEY (`breaks_id7` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks4`
    FOREIGN KEY (`breaks_id3` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks5`
    FOREIGN KEY (`breaks_id4` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks6`
    FOREIGN KEY (`breaks_id5` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_schedule_breaks7`
    FOREIGN KEY (`breaks_id6` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
COMMENT = 'Справочник расписаний для услуг' ;

CREATE INDEX `idx_schedule_breaks1` ON `qsystem`.`schedule` (`breaks_id1` ASC) ;

CREATE INDEX `idx_schedule_breaks2` ON `qsystem`.`schedule` (`breaks_id2` ASC) ;

CREATE INDEX `idx_schedule_breaks3` ON `qsystem`.`schedule` (`breaks_id7` ASC) ;

CREATE INDEX `idx_schedule_breaks4` ON `qsystem`.`schedule` (`breaks_id3` ASC) ;

CREATE INDEX `idx_schedule_breaks5` ON `qsystem`.`schedule` (`breaks_id4` ASC) ;

CREATE INDEX `idx_schedule_breaks6` ON `qsystem`.`schedule` (`breaks_id5` ASC) ;

CREATE INDEX `idx_schedule_breaks7` ON `qsystem`.`schedule` (`breaks_id6` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`calendar`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`calendar` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`calendar` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Название календаря' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Календарь услуг на год' ;


-- -----------------------------------------------------
-- Table `qsystem`.`services`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`services` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`services` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(2000) NOT NULL COMMENT 'Наименование услуги' ,
  `description` VARCHAR(2000) NULL DEFAULT NULL COMMENT 'Описание услуги.' ,
  `service_prefix` VARCHAR(10) NULL DEFAULT '' ,
  `button_text` VARCHAR(2500) NOT NULL DEFAULT '' COMMENT 'HTML-текст для вывода на кнопки регистрации.' ,
  `status` INT NOT NULL DEFAULT 1 COMMENT 'Состояние услуги. 1 - доступна, 0 - недоступна, -1 - невидима.' ,
  `enable` INT NOT NULL DEFAULT 1 COMMENT 'Способ вызова клиента юзером\n1 - стандартно\n2 - backoffice, т.е. вызов следующего без табло и звука, запершение только редиректом' ,
  `prent_id` BIGINT NULL DEFAULT NULL COMMENT 'Групповое подчинение.' ,
  `day_limit` INT NOT NULL DEFAULT 0 COMMENT 'ограничение выданных билетов в день. 0-нет ограничения' ,
  `person_day_limit` INT NOT NULL DEFAULT 0 COMMENT 'ограничение выданных билетов в день клиентам с одинаковыми введенными данными. 0-нет ограничения' ,
  `advance_limit` INT NOT NULL DEFAULT 1 COMMENT 'Ограничение по количеству предварительно регистрировшихся в час' ,
  `advance_limit_period` INT NULL DEFAULT 14 COMMENT 'ограничение в днях, в пределах которого можно записаться вперед. может быть null или 0 если нет ограничения' ,
  `advance_time_period` INT NOT NULL DEFAULT 60 COMMENT 'периоды, на которые делится день, для записи предварительно' ,
  `schedule_id` BIGINT NULL DEFAULT NULL COMMENT 'План работы услуги' ,
  `input_required` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Обязывать кастомера вводить что-то перед постоновкой в очередь' ,
  `input_caption` VARCHAR(200) NOT NULL DEFAULT 'Введите номер документа' COMMENT 'Текст над полем ввода обязательного ввода' ,
  `result_required` TINYINT(1) NOT NULL DEFAULT false COMMENT 'Требовать ввод пользователем результата работы с клиентом' ,
  `calendar_id` BIGINT NULL DEFAULT NULL ,
  `pre_info_html` TEXT NOT NULL COMMENT 'html текст информационного сообщения перед постановкой в очередь' ,
  `pre_info_print_text` TEXT NOT NULL COMMENT 'текст для печати при необходимости перед постановкой в очередь' ,
  `point` INT NOT NULL DEFAULT 0 COMMENT 'указание для какого пункта регистрации услуга, 0-для всех, х-для киоска х.' ,
  `ticket_text` VARCHAR(1500) NULL COMMENT 'Текст напечатается на талоне.' ,
  `seq_id` INT NOT NULL DEFAULT 0 COMMENT 'порядок следования кнопок услуг на пункте регистрации' ,
  `but_x` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки' ,
  `but_y` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки' ,
  `but_b` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки' ,
  `but_h` INT NOT NULL DEFAULT 0 COMMENT 'позиция кнопки' ,
  `deleted` DATE NULL COMMENT 'признак удаления с проставленим даты' ,
  `duration` INT NOT NULL DEFAULT '1' COMMENT 'Норматив. Среднее время оказания этой услуги.  Пока для маршрутизации при медосмотре' ,
  `sound_template` VARCHAR(45) NULL COMMENT 'шаблон звукового приглашения. null или 0... - использовать родительский.' ,
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
ENGINE = InnoDB, 
COMMENT = 'Дерево услуг' ;

CREATE INDEX `idx_servises_parent_id_servises_id` ON `qsystem`.`services` (`prent_id` ASC) ;

CREATE INDEX `idx_services_shedule` ON `qsystem`.`services` (`schedule_id` ASC) ;

CREATE INDEX `idx_services_calendar` ON `qsystem`.`services` (`calendar_id` ASC) ;


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
  `point_ext` VARCHAR(1045) NOT NULL DEFAULT '' COMMENT 'Вывод в третью колонку на главном табло. html + клиент ###  окно @@@' ,
  `deleted` DATE NULL COMMENT 'признак удаления с проставлением даты' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Пользователи системы.' ;


-- -----------------------------------------------------
-- Table `qsystem`.`streets`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`streets` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`streets` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(100) NOT NULL COMMENT 'Наименование улицы.' ,
  PRIMARY KEY (`id`, `name`) )
ENGINE = InnoDB, 
COMMENT = 'Словарь улиц' ;


-- -----------------------------------------------------
-- Table `qsystem`.`clients_authorization`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`clients_authorization` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`clients_authorization` (
  `id` BIGINT NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Имя' ,
  `surname` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Фамилие' ,
  `otchestvo` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'Отчество, иногда может отсутствовать.' ,
  `birthday` DATE NULL DEFAULT NULL COMMENT 'Дата рождения' ,
  `streets_id` BIGINT NULL DEFAULT NULL COMMENT 'Связь со словарем улиц. Проживание.' ,
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
ENGINE = InnoDB, 
COMMENT = 'Словарь клиентов для авторизации.' ;

CREATE INDEX `idx_clients_authorization_streets` ON `qsystem`.`clients_authorization` (`streets_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`results`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`results` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`results` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(150) NOT NULL DEFAULT '' COMMENT 'Текст результата' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Справочник результатов работы с клиентом' ;


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
  `clients_authorization_id` BIGINT NULL DEFAULT NULL COMMENT 'Определено если клиент авторизовался' ,
  `result_id` BIGINT NULL DEFAULT NULL COMMENT 'Если выбрали результат работы' ,
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
ENGINE = InnoDB, 
COMMENT = 'Таблица регистрации статистических событий клиентов.' ;

CREATE INDEX `idx_сlients_service_id_services_id` ON `qsystem`.`clients` (`service_id` ASC) ;

CREATE INDEX `idx_сlients_user_id_users_id` ON `qsystem`.`clients` (`user_id` ASC) ;

CREATE INDEX `idx_clients_clients_authorization` ON `qsystem`.`clients` (`clients_authorization_id` ASC) ;

CREATE INDEX `idx_clients_results` ON `qsystem`.`clients` (`result_id` ASC) ;


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
  `version` VARCHAR(25) NOT NULL DEFAULT 'Не присвоена' COMMENT 'Версия БД' ,
  `first_number` INT NOT NULL DEFAULT 1 ,
  `last_number` INT NOT NULL DEFAULT 999 ,
  `numering` TINYINT(1) NOT NULL DEFAULT true COMMENT '0 общая нумерация, 1 для каждой услуги своя нумерация' ,
  `point` INT NOT NULL DEFAULT 0 COMMENT '0 кабинет, 1 окно, 2 стойка' ,
  `sound` INT NOT NULL DEFAULT 2 COMMENT '0 нет оповещения, 1 только сигнал, 2 сигнал+голос' ,
  `branch_id` BIGINT NOT NULL DEFAULT -1 ,
  `sky_server_url` VARCHAR(145) NOT NULL DEFAULT '' ,
  `zone_board_serv_addr` VARCHAR(145) NOT NULL DEFAULT '' ,
  `zone_board_serv_port` BIGINT NOT NULL DEFAULT 0 ,
  `voice` INT NOT NULL DEFAULT 0 COMMENT '0 - по умолчанию, ну и т.д. по набору звуков' ,
  `black_time` INT NOT NULL DEFAULT 0 COMMENT 'Время нахождения в блеклисте в минутах. 0 - попавшие в блекслист не блокируются' ,
  `limit_recall` INT NOT NULL DEFAULT 0 COMMENT 'Количество повторных вызовов перед отклонением неявившегося посетителя, 0-бесконечно' ,
  `button_free_design` TINYINT(1)  NOT NULL DEFAULT false COMMENT 'авторасстановка кнопок на киоске' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Сетевые настройки сервера.' ;


-- -----------------------------------------------------
-- Table `qsystem`.`services_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`services_users` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`services_users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `service_id` BIGINT NOT NULL ,
  `user_id` BIGINT NOT NULL ,
  `coefficient` INT NOT NULL DEFAULT 1 COMMENT 'Коэффициент участия. 0/1/2' ,
  `flexible_coef` TINYINT(1)  NOT NULL DEFAULT false COMMENT 'возможность изменить коэф участия юзерами' ,
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
ENGINE = InnoDB, 
COMMENT = 'Таблица соответствий услуга - пользователь.' ;

CREATE INDEX `idx_services_id_su_service_id` ON `qsystem`.`services_users` (`service_id` ASC) ;

CREATE INDEX `idx_userss_id_su_user_id` ON `qsystem`.`services_users` (`user_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`statistic`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`statistic` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`statistic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `user_id` BIGINT NOT NULL ,
  `client_id` BIGINT NOT NULL ,
  `service_id` BIGINT NOT NULL ,
  `results_id` BIGINT NULL ,
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
    ON UPDATE CASCADE,
  CONSTRAINT `fk_statistic_results`
    FOREIGN KEY (`results_id` )
    REFERENCES `qsystem`.`results` (`id` )
    ON DELETE SET NULL
    ON UPDATE SET NULL)
ENGINE = InnoDB, 
COMMENT = 'События работы пользователя с клиентом.Формируется триггером' ;

CREATE INDEX `idx_work_user_id_users_id` ON `qsystem`.`statistic` (`user_id` ASC) ;

CREATE INDEX `idx_work_сlient_id_сlients_id` ON `qsystem`.`statistic` (`client_id` ASC) ;

CREATE INDEX `idx_work_service_id_services_id` ON `qsystem`.`statistic` (`service_id` ASC) ;

CREATE INDEX `idx_statistic_results` ON `qsystem`.`statistic` (`results_id` ASC) ;


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
ENGINE = InnoDB, 
COMMENT = 'Зарегистрированные аналитические отчеты.' ;


-- -----------------------------------------------------
-- Table `qsystem`.`advance`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`advance` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`advance` (
  `id` BIGINT NOT NULL ,
  `service_id` BIGINT NOT NULL COMMENT 'Услуга предварительной записи' ,
  `advance_time` DATETIME NOT NULL COMMENT 'Время предварительной записи' ,
  `priority` INT NOT NULL DEFAULT 2 COMMENT 'Приоритет заранее записавшегося клиента.' ,
  `clients_authorization_id` BIGINT NULL DEFAULT NULL COMMENT 'Определено если клиент авторизовался' ,
  `input_data` VARCHAR(150) NULL COMMENT 'Введеные при предвариловке данные клиента если услуга этого требует' ,
  `comments` VARCHAR(345) NULL DEFAULT '' COMMENT 'Коментарии при записи предварительно оператором удаленно' ,
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
ENGINE = InnoDB, 
COMMENT = 'Таблица предварительной записи' ;

CREATE INDEX `idx_scenario_services` ON `qsystem`.`advance` (`service_id` ASC) ;

CREATE INDEX `idx_advance_clients_authorization` ON `qsystem`.`advance` (`clients_authorization_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`information`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`information` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`information` (
  `id` BIGINT NOT NULL ,
  `parent_id` BIGINT NULL DEFAULT NULL ,
  `name` VARCHAR(100) NOT NULL COMMENT 'Наименование узла справки' ,
  `text` TEXT NOT NULL COMMENT 'html-текст справки' ,
  `text_print` TEXT NOT NULL COMMENT 'Текст для печати информационного узла' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_information_information`
    FOREIGN KEY (`parent_id` )
    REFERENCES `qsystem`.`information` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB, 
COMMENT = 'Таблица справочной информации древовидной структуры' ;

CREATE INDEX `idx_information_information` ON `qsystem`.`information` (`parent_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`responses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`responses` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`responses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(100) NOT NULL DEFAULT '' ,
  `text` VARCHAR(5000) NOT NULL DEFAULT '' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB, 
COMMENT = 'Список отзывов в отратной связи' ;


-- -----------------------------------------------------
-- Table `qsystem`.`response_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`response_event` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`response_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `resp_date` DATETIME NOT NULL COMMENT 'Дата отклика' ,
  `response_id` BIGINT NOT NULL ,
  `services_id` BIGINT NULL ,
  `users_id` BIGINT NULL ,
  `clients_id` BIGINT NULL COMMENT 'Клиент оставивший отзыв' ,
  `client_data` VARCHAR(245) NOT NULL DEFAULT '' ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_response_date_responses`
    FOREIGN KEY (`response_id` )
    REFERENCES `qsystem`.`responses` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_response_event_services`
    FOREIGN KEY (`services_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_response_event_users`
    FOREIGN KEY (`users_id` )
    REFERENCES `qsystem`.`users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_response_event_clients`
    FOREIGN KEY (`clients_id` )
    REFERENCES `qsystem`.`clients` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB, 
COMMENT = 'Даты оставленных отзывов.' ;

CREATE INDEX `idx_response_date_responses` ON `qsystem`.`response_event` (`response_id` ASC) ;

CREATE INDEX `idx_response_event_services` ON `qsystem`.`response_event` (`services_id` ASC) ;

CREATE INDEX `idx_response_event_users` ON `qsystem`.`response_event` (`users_id` ASC) ;

CREATE INDEX `idx_response_event_clients` ON `qsystem`.`response_event` (`clients_id` ASC) ;


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
ENGINE = InnoDB, 
COMMENT = 'Дни неработы услуг' ;

CREATE INDEX `idx_calendar_out_days_calendar` ON `qsystem`.`calendar_out_days` (`calendar_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`users_services_users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`users_services_users` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`users_services_users` (
  `planServices_id` BIGINT NOT NULL ,
  `users_id` BIGINT NOT NULL ,
  PRIMARY KEY (`planServices_id`) ,
  CONSTRAINT `fk_planserv`
    FOREIGN KEY (`planServices_id` )
    REFERENCES `qsystem`.`services_users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user`
    FOREIGN KEY (`users_id` )
    REFERENCES `qsystem`.`users` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB, 
COMMENT = 'Услуги юзеров с приоритетами.' ;

CREATE INDEX `idx_planserv` ON `qsystem`.`users_services_users` (`planServices_id` ASC) ;

CREATE INDEX `idx_user` ON `qsystem`.`users_services_users` (`users_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`break`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`break` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`break` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `breaks_id` BIGINT NULL ,
  `from_time` TIME NOT NULL ,
  `to_time` TIME NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_break_breaks1`
    FOREIGN KEY (`breaks_id` )
    REFERENCES `qsystem`.`breaks` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB, 
COMMENT = 'Перерывы в работе для предвариловки' ;

CREATE INDEX `idx_break_breaks1` ON `qsystem`.`break` (`breaks_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`services_langs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`services_langs` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`services_langs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `services_id` BIGINT NULL ,
  `lang` VARCHAR(45) NOT NULL ,
  `name` VARCHAR(2000) NOT NULL DEFAULT '' ,
  `description` VARCHAR(20) NULL ,
  `button_text` VARCHAR(2500) NOT NULL DEFAULT '' ,
  `input_caption` VARCHAR(200) NOT NULL DEFAULT '' ,
  `ticket_text` VARCHAR(1500) NULL ,
  `pre_info_html` TEXT NOT NULL ,
  `pre_info_print_text` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_services_langs_services`
    FOREIGN KEY (`services_id` )
    REFERENCES `qsystem`.`services` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

CREATE INDEX `idx_services_langs_services` ON `qsystem`.`services_langs` (`services_id` ASC) ;


-- -----------------------------------------------------
-- Table `qsystem`.`standards`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `qsystem`.`standards` ;

CREATE  TABLE IF NOT EXISTS `qsystem`.`standards` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `wait_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное время ожидания, в минутах' ,
  `work_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное время работы с одним клиентом, в минутах' ,
  `downtime_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное время простоя при наличии очереди, в минутах' ,
  `line_service_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальная длинна очереди к одной услуге' ,
  `line_total_max` INT NOT NULL DEFAULT 0 COMMENT 'Максимальное количество клиентов ко всем услугам' ,
  `relocation` INT NOT NULL DEFAULT 1 COMMENT 'типа параметр если есть перемещение, например между корпусами или ходьба до оператора' ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;

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


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `qsystem`.`schedule`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`schedule` (`id`, `name`, `type`, `time_begin_1`, `time_end_1`, `time_begin_2`, `time_end_2`, `time_begin_3`, `time_end_3`, `time_begin_4`, `time_end_4`, `time_begin_5`, `time_end_5`, `time_begin_6`, `time_end_6`, `time_begin_7`, `time_end_7`, `breaks_id1`, `breaks_id2`, `breaks_id3`, `breaks_id4`, `breaks_id5`, `breaks_id6`, `breaks_id7`) VALUES (1, 'План работы с 8.00 до 17.00', 0, '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', '08:00:00', '17:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `qsystem`.`schedule` (`id`, `name`, `type`, `time_begin_1`, `time_end_1`, `time_begin_2`, `time_end_2`, `time_begin_3`, `time_end_3`, `time_begin_4`, `time_end_4`, `time_begin_5`, `time_end_5`, `time_begin_6`, `time_end_6`, `time_begin_7`, `time_end_7`, `breaks_id1`, `breaks_id2`, `breaks_id3`, `breaks_id4`, `breaks_id5`, `breaks_id6`, `breaks_id7`) VALUES (2, 'План работы по четным/нечетным', 1, '08:00:00	', '13:00:00', '12:00:00', '17:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`calendar` (`id`, `name`) VALUES (1, 'Общий календарь');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`services`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`services` (`id`, `name`, `description`, `service_prefix`, `button_text`, `status`, `enable`, `prent_id`, `day_limit`, `person_day_limit`, `advance_limit`, `advance_limit_period`, `advance_time_period`, `schedule_id`, `input_required`, `input_caption`, `result_required`, `calendar_id`, `pre_info_html`, `pre_info_print_text`, `point`, `ticket_text`, `seq_id`, `but_x`, `but_y`, `but_b`, `but_h`, `deleted`, `duration`, `sound_template`) VALUES (1, 'Дерево услуг', 'Дерево услуг', '-', '<html><p align=center><span style=\'font-size:55.0;color:#DC143C\'>Система управления очередью</span><br><span style=\'font-size:45.0;color:#DC143C\'><i>выберите требуемую услугу</i>', 1, 1, NULL, 0, 0, 1, 14, 60, NULL, 0, '', 0, NULL, '', '', 0, NULL, 0, 100, 100, 200, 100, NULL, 1, '120000');
INSERT INTO `qsystem`.`services` (`id`, `name`, `description`, `service_prefix`, `button_text`, `status`, `enable`, `prent_id`, `day_limit`, `person_day_limit`, `advance_limit`, `advance_limit_period`, `advance_time_period`, `schedule_id`, `input_required`, `input_caption`, `result_required`, `calendar_id`, `pre_info_html`, `pre_info_print_text`, `point`, `ticket_text`, `seq_id`, `but_x`, `but_y`, `but_b`, `but_h`, `deleted`, `duration`, `sound_template`) VALUES (2, 'Услуга', 'Описание услуги', 'А', '<html><b><p align=center><span style=\'font-size:20.0pt;color:blue\'>Некая услуга', 1, 1, 1, 0, 0, 1, 14, 60, 1, 0, '', 0, 1, '', '', 0, NULL, 0, 100, 100, 200, 100, NULL, 1, NULL);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`users`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`users` (`id`, `name`, `password`, `point`, `adress_rs`, `enable`, `admin_access`, `report_access`, `point_ext`, `deleted`) VALUES (1, 'Администратор', '', '1', 32, 1, 1, 1, '\'\'', NULL);
INSERT INTO `qsystem`.`users` (`id`, `name`, `password`, `point`, `adress_rs`, `enable`, `admin_access`, `report_access`, `point_ext`, `deleted`) VALUES (2, 'Пользователь', '', '2', 33, 1, 0, 0, '<html><span style=\'font-size:26.0pt;color:blue\'>Этаж 1<br>Кабинет 1', NULL);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`results`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`results` (`id`, `name`) VALUES (1, 'Обращение отработано');
INSERT INTO `qsystem`.`results` (`id`, `name`) VALUES (2, 'Невозможно отработать');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`net`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`net` (`id`, `server_port`, `web_server_port`, `client_port`, `finish_time`, `start_time`, `version`, `first_number`, `last_number`, `numering`, `point`, `sound`, `branch_id`, `sky_server_url`, `zone_board_serv_addr`, `zone_board_serv_port`, `voice`, `black_time`, `limit_recall`, `button_free_design`) VALUES (1, 3128, 8088, 3129, '18:00:00', '08:45:00', '2.5', 1, 999, 0, 0, 1, -1, '', '127.0.0.1', 27007, 0, 0, 0, 0);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`services_users`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`services_users` (`id`, `service_id`, `user_id`, `coefficient`, `flexible_coef`) VALUES (1, 2, 2, 1, 0);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`reports`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (1, 'Статистический отчет в разрезе услуг за период', 'ru.apertum.qsystem.reports.formirovators.StatisticServices', '/ru/apertum/qsystem/reports/templates/statisticServicesPeriod.jasper', 'statistic_period_services');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (2, 'Статистический отчет в разрезе персонала за период', 'ru.apertum.qsystem.reports.formirovators.StatisticUsers', '/ru/apertum/qsystem/reports/templates/statisticUsersPeriod.jasper', 'statistic_period_users');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (3, 'Отчет по распределению клиентов по виду услуг за период', 'ru.apertum.qsystem.reports.formirovators.RatioServices', '/ru/apertum/qsystem/reports/templates/ratioServicesPeriod.jasper', 'ratio_period_services');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (4, 'Распределение нагрузки внутри дня', 'ru.apertum.qsystem.reports.formirovators.DistributionJobDay', '/ru/apertum/qsystem/reports/templates/DistributionJobDay.jasper', 'distribution_job_day');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (5, 'Распределение нагрузки внутри дня для услуги', 'ru.apertum.qsystem.reports.formirovators.DistributionJobDayServices', '/ru/apertum/qsystem/reports/templates/DistributionJobDayServices.jasper', 'distribution_job_services');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (6, 'Распределение нагрузки внутри дня для пользователя', 'ru.apertum.qsystem.reports.formirovators.DistributionJobDayUsers', '/ru/apertum/qsystem/reports/templates/DistributionJobDayUsers.jasper', 'distribution_job_users');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (7, 'Распределение среднего времени ожидания внутри дня', 'ru.apertum.qsystem.reports.formirovators.DistributionWaitDay', '/ru/apertum/qsystem/reports/templates/DistributionWaitDay.jasper', 'distribution_wait_day');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (8, 'Распределение среднего времени ожидания внутри дня для услуги', 'ru.apertum.qsystem.reports.formirovators.DistributionWaitDayServices', '/ru/apertum/qsystem/reports/templates/DistributionWaitDayServices.jasper', 'distribution_wait_services');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (9, 'Распределение среднего времени ожидания внутри дня для пользователя', 'ru.apertum.qsystem.reports.formirovators.DistributionWaitDayUsers', '/ru/apertum/qsystem/reports/templates/DistributionWaitDayUsers.jasper', 'distribution_wait_users');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (10, 'Статистический отчет по отзывам клиентов за период', 'ru.apertum.qsystem.reports.formirovators.ResponsesReport', '/ru/apertum/qsystem/reports/templates/responsesReport.jasper', 'statistic_period_responses');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (11, 'Статистический отчет распределения активности клиентов за период', 'ru.apertum.qsystem.reports.formirovators.ResponsesDateReport', '/ru/apertum/qsystem/reports/templates/responsesDateReport.jasper', 'statistic_period_date_responses');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (12, 'Отчет предварительно зарегистрированных клиентов по услуге на дату', 'ru.apertum.qsystem.reports.formirovators.DistributionMedDayServices', '/ru/apertum/qsystem/reports/templates/DistributionMedDayServices.jasper', 'distribution_med_services');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (13, 'Отчет по авторизованным персонам за период для пользователя', 'ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodUsers', '/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodUsers.jasper', 'authorized_clients_period_users');
INSERT INTO `qsystem`.`reports` (`id`, `name`, `className`, `template`, `href`) VALUES (14, 'Отчет по авторизованным персонам за период для услуги', 'ru.apertum.qsystem.reports.formirovators.AuthorizedClientsPeriodServices', '/ru/apertum/qsystem/reports/templates/AuthorizedClientsPeriodServices.jasper', 'authorized_clients_period_services');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`information`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`information` (`id`, `parent_id`, `name`, `text`, `text_print`) VALUES (1, NULL, 'Справочная система', '<html><p align=center><span style=\'font-size:55.0;color:#DC143C\'>Справочная информация<br><span style=\'font-size:45.0;color:#DC143C\'><i>Прочитайте и распечатайте памятку</i></span></p>', 'Для  получения детальной информации обратитесь к менеджеру');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`responses`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`responses` (`id`, `name`, `text`) VALUES (1, 'Отлично', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Отлично</span></b>');
INSERT INTO `qsystem`.`responses` (`id`, `name`, `text`) VALUES (2, 'Хорошо', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Хорошо</span></b>');
INSERT INTO `qsystem`.`responses` (`id`, `name`, `text`) VALUES (3, 'Удовлетворительно', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Удовлетворительно</span></b>');
INSERT INTO `qsystem`.`responses` (`id`, `name`, `text`) VALUES (4, 'Плохо', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Плохо</span></b>');
INSERT INTO `qsystem`.`responses` (`id`, `name`, `text`) VALUES (5, 'Отвратительно', '<html><b><p align=center><span style=\'font-size:20.0pt;color:green\'>Отвратительно</span></b>');

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`calendar_out_days`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`calendar_out_days` (`id`, `out_day`, `calendar_id`) VALUES (1, '2010-01-01', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`users_services_users`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`users_services_users` (`planServices_id`, `users_id`) VALUES (1, 2);

COMMIT;

-- -----------------------------------------------------
-- Data for table `qsystem`.`standards`
-- -----------------------------------------------------
START TRANSACTION;
USE `qsystem`;
INSERT INTO `qsystem`.`standards` (`id`, `wait_max`, `work_max`, `downtime_max`, `line_service_max`, `line_total_max`, `relocation`) VALUES (1, 10, 20, 10, 10, 20, 1);

COMMIT;
