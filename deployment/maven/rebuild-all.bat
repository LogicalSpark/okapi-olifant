cd ../../
call mvn clean
if ERRORLEVEL 1 goto end

call mvn install
if ERRORLEVEL 1 goto end

cd deployment/maven
call ant
if ERRORLEVEL 1 goto end

:end
pause