@echo off
set INSTANCE_NUMBER=%1
set INSTANCE_POSTFIX=%2
echo.
echo ===========================================================================
echo Dsn64: mappatura ODBC
echo ===========================================================================
echo Usare solo se:
echo 1) Sistema e' Windows 2008 Server 64 bit
echo 2) DB2 installato in 1)
echo 3) Panthera installato in 1)
echo 4) CRR2008 Server Edition installato in 1)
echo.
echo Lo script funziona solo da una Command Window di DB2
echo.
echo Per maggiori dettagli consultare Dsn64.pdf (o Dsn64.rtf) e la guida di 
echo installazione di panthera.
echo.
echo ===========================================================================
echo.
if "%INSTANCE_POSTFIX%"=="" set INSTANCE_POSTFIX=T
if "%INSTANCE_NUMBER%"=="" goto Help
echo Per l'istanza 
echo     PANTH%INSTANCE_NUMBER% 
echo verra' creata la seguente mappatura ODBC aggiuntiva: 
echo     PANTH%INSTANCE_NUMBER%%INSTANCE_POSTFIX%
echo.
echo CTRL + C (e poi S o Y per terminare); altro tasto per continuare.
echo.
pause
echo.
echo Aggiornamento services...
copy C:\Windows\System32\drivers\etc\services C:\Windows\System32\drivers\etc\services.bak
echo db2c_PANTH%INSTANCE_NUMBER%    500%INSTANCE_NUMBER%/tcp >> C:\Windows\System32\drivers\etc\services
echo.
echo Aggiornamento DB2 protocol... 
set db2instance=panth%INSTANCE_NUMBER%
db2set DB2COMM=TCPIP
db2 update dbm cfg using SVCENAME db2c_PANTH%INSTANCE_NUMBER%
echo.
db2stop force
db2start
echo.
echo Aggiornamento DB2 catalog...
set db2instance=db2
db2 catalog tcpip node panth%INSTANCE_NUMBER%%INSTANCE_POSTFIX% remote localhost server 500%INSTANCE_NUMBER%
db2 catalog database panth%INSTANCE_NUMBER% as panth%INSTANCE_NUMBER%%INSTANCE_POSTFIX% at node panth%INSTANCE_NUMBER%%INSTANCE_POSTFIX%
db2 catalog system ODBC data source PANTH%INSTANCE_NUMBER%%INSTANCE_POSTFIX%
echo.
echo Update ODBC DSN...
C:\Windows\SysWOW64\odbcconf CONFIGSYSDSN "IBM DB2 ODBC DRIVER - DB2PANTHERA" "DSN=PANTH%INSTANCE_NUMBER%%INSTANCE_POSTFIX%"
echo.
goto end
:help
echo.
echo Help parametri
echo.
echo numero_istanza [post-fisso]
echo.
echo Dove:
echo numero_istanza = numero istanza PANTHxx (es.: 01 per PANTH01)
echo post-fisso = opzionale - se omesso = T - postfisso pe la nuova mappatura
echo (es. T per PANTH01 equivale a PANTH01T).
echo.
echo Esempio: 
echo Dsn64.cmd 01
echo.
:end
echo Fine!
echo.

