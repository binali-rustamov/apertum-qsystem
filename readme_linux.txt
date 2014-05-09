1. Debian 7 install Java Oracle 1.7 via apt-get

установим часовой пояс dpkg-reconfigure tzdata
проверим время date



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