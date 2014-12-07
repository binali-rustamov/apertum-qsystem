1. Debian 7 install Java Oracle 1.7(6, 7, 8) via apt-get
можно почитать тут http://www.linuxrussia.com/2013/04/oracle-java-7-ubuntu-1304-1204-1210.html

// установим часовой пояс dpkg-reconfigure tzdata
// проверим время date

**********************************************
** Java
**********************************************

открыть терминал с root правами и поехали

проверим текущую версию Java:

java -version

если там начнет гулять какой-никакой Openjdk или предыдущие установки, то от них нужно избавится. В случае с OpenJDK это делается командой:

sudo apt-get remove openjdk*


Теперь непосредственно установка:

Просто бесхитростно выполнить 5 команд простым копипастом отсюда:

 echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list  
 echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list  
 apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886  
 apt-get update  
 apt-get install oracle-java7-installer  
 
Этого в принципе достаточно если ставите с нуля.

для установки 6 версии:
sudo apt-get install oracle-java6-installer

для установки 8 версии:
sudo apt-get install oracle-java8-installer

!!!Перед установкой обязательно нужно согласится с лицензионным соглашением.!!!

устанавливает как основную jvm в системе  и настраивает окружение:

sudo apt-get install oracle-java7-set-default 

проверим все ли вышло у нас удачно:

java -version

должно быть что-то вроде:

java version "1.7.0_45"
Java(TM) SE Runtime Environment (build 1.7.0_45-b18)
Java HotSpot(TM) Server VM (build 24.45-b08, mixed mode)


**********************************************
** GlassFish
**********************************************



2. Downloading and installing GlassFish

Switch to user 'root' and get GlassFish
sudo -i
cd /srv
# You may or may not need to set the environment variable http_proxy first.
wget http://dlc.sun.com.edgesuite.net/glassfish/4.0/release/glassfish-4.0.zip  # Скачали архивчик
unzip glassfish-4.0.zip      # распоковали архивчик This creates /srv/glassfish4/*


3. Тут всякая ботва, которая не дает войти в админку и прочие траблы при этом.
Все просто:
-админу задать пароль
-админу разрешить быть секурным
-ребут гласфиша.
Теперь в админку не зайдем т.к. пароль пуст, а с таким паролем только локально. Запустим GF4 ./startserv

Q: When I try to login to Glassfish administration application, it says "Secure Admin must be enabled to access the DAS remotely"
A: Включим возможность секьюрного админа, но пароль предварительно сменим. You can enable remote access with the following command:
asadmin --host www.yourdomain.com --port 4848 enable-secure-admin
Then stop/start glassfish immediately after enabling secure admin.
In case your private Glassfish runs on shared server you will need to use your custom port (assume 15123 for the example) in asadmin command. Check Java Control Panel for your Glassfish console port. Use the port with your asadmin commands. The below was tested with Glassfish 3.1.2 and 4.0.

[~]# asadmin --port 15123 enable-secure-admin
remote failure: At least one admin user has an empty password, which secure admin does not permit. Use the change-admin-password command or the admin console to create non-empty passwords for admin accounts.
Command enable-secure-admin failed.
Set the password as prompted

Вот так мы админу сменим пароль
[~]# asadmin --port 15123 change-admin-password
Enter admin user name [default: admin]> 
Enter admin password> 
Enter new admin password> secret
Enter new admin password again> secret
Command change-admin-password executed successfully.
Now retry enable-secure-admin

# asadmin --port 15123 enable-secure-admin
Enter admin user name> admin
Enter admin password for user "admin"> secret
You must restart all running servers for the change in secure admin to take effect.
Command enable-secure-admin executed successfully.
And restart default domain

[~]# asadmin --port 15123 restart-domain
Successfully restarted the domain
Access your Glassfish console URL this time over SSL (so accept certificate mismatch warning in your browser e.g. 'this connection is untrusted' in Firefox) and login with the credentials you set. Glassfish Admin console URL is also shown in Java Control Panel.


Спецзаметка:  как решать проблему с включением блокировки єкрана в линукс http://www.linuxrussia.com/2013/05/caffeine-ubuntu.html

**********************************************
** MySQL
**********************************************

* To start mysql server:
/etc/init.d/mysqld start
* To stop mysql server:
/etc/init.d/mysqld stop
* To restart mysql server
/etc/init.d/mysqld restart


mysql <-h ip_remoute_server> -u root -p

mysql> SHOW DATABASES;
mysql> use mysql;
mysql> drop database qsystem;

If you are already running mysql, you can execute an SQL script file using the 'source' command or '\.' command:
mysql> source file_name
mysql> \. file_name

Back up From the Command Line (using mysqldump)

command:  $ mysqldump --opt -u [uname] -p[pass] [dbname] > [backupfile.sql]
[uname] Your database username
[pass] The password for your database (note there is no space between -p and the password)
[dbname] The name of your database
[backupfile.sql] The filename for your database backup
[--opt] The mysqldump option
examples:
$ mysqldump -u root -p Tutorials > tut_backup.sql
$ mysqldump -u root -p database_name > backup.sql

Restoring your MySQL Database
1.Create an appropriately named database on the target machine
2.Load the file using the mysql command: $ mysql -u [uname] -p[pass] [db_to_restore] < [backupfile.sql]
example: $ mysql -u root -p Tutorials < tut_backup.sql



FOR UTF8

[mysql]
default-character-set=utf8

[mysqld]

collation_server=utf8_unicode_ci
character_set_server=utf8

**********************************************
** LINUX
**********************************************
Список процессов
ps ax | grep 'ищем процесс по куску имени'

Киляем процесс по его id
kill PID[ PID2 PID3]
kill -9 PID

Киляем все процессы по имени
killall PIDNAME
