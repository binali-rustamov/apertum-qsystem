Instrucción de instalación del sistema de automatización del trabajo con clientes.

Paso 1. Preparando la instalación.
Antes de instalar el sistema, necesita asegurarse que ya tiene instalado el MySQL de versión no menos que 5.5 y OracleJRE no menos de 1.7. Puede averiguar la instalación de JRE así:
El menú Inicio –>Cumplir->cmd-> java –version.

También está recomendado instalar utilidades gráficas para trabajar con MySQL.

Paso 2. Inicio de la instalación del sistema.
Microsoft Windows:
Hace 2 clics sobre el archivo install.jar.
Linux(el método universal para cualquier SO):
Inicio de la instalación de la línea de órdenes:
Abre la carpeta con archivo install.jar y cumple el orden
java –cp install.jar com.izforge.izpack.installer.Installer
 !!!  java -jar install.jar

Paso 3. Instalación del sistema.
En la pantalla de bienvenida cliquea “Luego”, en la pantalla de presentación del sistema también cliquea “Luego”, en la pantalla siguiente mostrarán la versión de montaje de sistema, fecha de montaje y otra información sobre el producto, también cliquea “Luego”.

En la pantalla siguiente ofrecerán elegir el cátalogo para instalar el sistema, predeterminadamente el cátalogo es el disco con el SO instalado en carpeta Program Files y cliquea “Luego”. El installador creará el cátalogo automaticamente si él no existe.

Luego ofrecerán elegir instalar los componentes del sistema:
- archivos de aplicación y biblioteca (predeterminado, imposible cambiar)
- puesto del trabajo del operador (predeterminado)
- servidor
- administración
- punto de registración
- documentación*
*el componente “Documentación” incluye: manual de usuario y manual de administrativo, ayuda contextual siempre está presentada.
Después de elegir los componentes necesarios cliquea “Luego”.

Paso 4. Configuración del sistema.
Después de elegir los componentes, ofrecerán introducir direcciones y puertos para interacción de los componentes del sistema:
- dirección de servidor
- puerto de servidor
-dirección de punto de registración
- puerto de punto de registración
- puerto de clientes
- indicar el punto de registración multilingüe.
Anotación! En caso de introducir los datos incorrectos en direcciones y puertos de servidores, puede corregirlos en *.bat archivos de instalados componentes del sistema.
Después de introducir los datos cliquea “Luego”. Mostrarán la lista de los componentes elegidos, si está de acuerdo cliquea “Luego”.

Paso 5. Terminación de instalación.
Empezó la instalación de los componentes programáticos del sistema. Después de que se cumplan cliquea “Luego”. Aparecerá la ventana donde ofrecerán poner los accesos directos en el escritorio. Creación de los accesos directos en la carpeta instalada con sistema está predeterminado.

Después de que se cumpla el trabajo del paquete de instalación necesita instalar algunos productos programátivos y controladores de dispositivo si, por supuesto, todavía no están instalados.

Paso 6. Adición de biblioteca de JavaFX.
Empezando con la versión 1.3.1 necesita conectar biblioteca JavaFX. Esta biblioteca está distribuida en conjunto de OracleJRE, pero predeterminadamente no está utilizada. Para conectarla necesita copiar un archivo de carpeta a carpeta. Copia archivo <jre>\lib\jfxrt.jar a la carpeta<jre>\lib\ext. Es todo.

Para instalar el módulo de clientes estas acciones son suficientes.

Ahora todos los componenetes necesarios están instalados en el ordenador. Ahora necesita implementar el base de datos y configurar el sistema para utilizar este base de datos.

Paso 7. Implementación de BD.
*Necesita utilizar codificación utf8. En administración de MySQL o en my.ini(my.cnf) indica las configuraciones necesarias. Por ejemplo:
[mysql]
default-character-set=utf8
[mysqld]
collation_server=utf8_unicode_ci
character_set_server=utf8

En carpeta DB encuentra el script de implementación qsystem.sql. Este script creará el base mismo, las tablas necesarias y rellenará las tablas con datos basicos. Si ya tiene el BD y pasa a nueva versión de BD, necesita utilizar sql-script para renovar el base hasta la versión necesaria. Para cumplir sql-scripts puede utilizar la consola de MySQL, pero es mejor utilizar la aplicación MySQL Query Browser, la puede bajar gratis de Internet e instalar en el ordenador. Añade el usuario de BD y permitele el acceso al creado BD.

En las configuraciones de BD MySQL huy parámetro wait_timeout, es el tiempo en secundos, en la duración de él el servidor observe desactividad en la conexión desinteractiva antes de cerrarlo.
La significación predeterminada es 28800 secundos. Si el servidor del sistema se queda trabajando, por ejemplo, durante toda la noche o las fiestas largas, entonces cuando empiece a trabajar va a ocurrir el error, porque el BD no va a trabajar la demanda. Este parámetro puede indicar en archivo de configuración de MySQL. A menudo en Windows este archivo es my.ini, puede ver ciertemente en configuraciones de servidor de aplicación de MySQL Administrator. Añadir a este archivo la línea"wait_timeout=хххх". El servidor tiene el funcional de mantener la conexión consultando BD una vez a la hora. Tiene que tomarlo en cuenta si tiene las configuraciones más duras.

Paso 8. configuración del sistema para utilizar el BD.
En carpeta de aplicación inicia admdbcom.bat para SO Windows o admdbcom.sh para SO Linux. admspt.bat para configuración consolar. Introduce los datos correctos acerca de conexión con BD. Conserva los parámetros. Puede tener más de ún servidor de BD. Indica uno de ellos como el corriente y se utilizarán las configuraciones del servidor corriente.

Paso 9. Relleno de configuración y ajuste de servidor.
Inicia StartAdmin.bat. Al principio entra en programa de administración como “Administrador” con contraseña vacía. Rellena la lista de operadores, compone variedad de servicios, indica los servicios para operadores. No olvide de encargarse del horario de hacer servicios y conservar los cambios.

Paso 10. Posicionamiento de tablero principal y de tablero de los operadores.
Desde la versión 1.3.7 especificar el número de monitor para la placa base en la tabla de editor en la parte central. El "número de monitor adicional para mostrar" parámetros. Eso es suficiente. Si el monitor no se conecte, coordina desde clientboard.xml y mainboard.xml se utilizarán. Cuando el parámetro es 0, el tablero está desactivado.
En carpeta <Qsystem>\config\ hay 2 archivos: clientboard.xml y mainboard.xml (para máquina de clientes y para el servidor respetivamente). En ellos indica las coordenadas de posicionamiento de tableros, allí es donde puede encender/apagarlo. Si el segundo monitor no está conectado, el programa muestra todo en un monitor. Para que el tablero aparezca en el segundo monitor, necesita indicar en clientboard.xml y mainboard.xml las coordenadas que están en el segundo monitor. En los archivos de configuración necesita poner los parámetros X y Y:
<Board visible="1" x="-500" y="10" Denominación="Conservar la configuración de tablero">
Denominación="Conservar la configuración de tablero">

Utilizandolas identificarán la posición del tablero y estará abierto al segundo monitor. Cuando está conectando el segundo monitor necesita ampliar el escritorio. Esta empliación del escritorio va a tener las coordinadores en relación con el principal escritorio. En las configuraciones necesita indicar las coordinadores del punto que toca esta ampliación. Utilizando este punto tendrán el posicionamiento. Esto significa que el tablero estará puesto con su superior ángulo de izquierda en este punto. Recordamos que las coordinadores de superior ángulo de izquierda  de monitor principal son 0,0. Por ejemplo, está conectado el monitor adicional y el escritorio está ampliado a él así como a la izquierda del principal. El monitor adicional tiene la amplificación de 640х480. Con relación al escritorio principal el superior ángulo de izquierda del escritorio adicional tiene las coordinadores (-640, 0). En las configuraciones hay x="-500" y="10", esto significa que el tablero está posicionando al escritorio adicional, pero está 10 pixeles bajo y 140 pixeles lejos del borde de monitor adicional. Después de esto se abrirá a toda la pantalla. 

Después de cumplir todos los pasos el sistema está preparado para la utilización.

 


