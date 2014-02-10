#!/bin/sh

# init
pause()
{
   read -p "$@" nothing
}
# other stuff

pause "Admin tools for changing DB cont. Press any key ..."

java -cp dist/QSystem.jar ru.apertum.qsystem.server.ChangeContext

pause "Context is changed. Press any key ..."
