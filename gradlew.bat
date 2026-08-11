@rem Gradle startup script for Windows
@echo off
setlocal
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if defined JAVA_HOME goto findJavaFromJavaHome
set JAVACMD=java.exe
%JAVACMD% -version >NUL 2>&1
if %ERRORLEVEL% EQU 0 goto execute

echo ERROR: JAVA_HOME is not set and java was not found in PATH. 1>&2
exit /b 1

:findJavaFromJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVACMD%" goto execute
echo ERROR: JAVA_HOME points to an invalid directory: %JAVA_HOME% 1>&2
exit /b 1

:execute
"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
