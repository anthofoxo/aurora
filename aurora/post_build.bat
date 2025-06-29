@echo off

rem Original file and backup paths
set "file=C:\Program Files (x86)\Steam\steamapps\common\Thumper\steam_api64.dll"
set "backup=C:\Program Files (x86)\Steam\steamapps\common\Thumper\steam_api64.dll.bak"
set "newdll=..\bin\debug\aurora.dll"
set "auroraDir=C:\Program Files (x86)\Steam\steamapps\common\Thumper\aurora"

echo File: %file%
echo Backup: %backup%
echo New DLL to copy: %newdll%

rem Backup
if exist "%backup%" (
    echo Backup already exists: %backup%
) else (
    echo Creating backup...
    copy "%file%" "%backup%"
    echo Backup created: %backup%
)

rem Overwrite original
echo Overwriting original file with new DLL...
copy /Y "%newdll%" "%file%"
if errorlevel 1 (
    echo ERROR: Failed to copy %newdll% to %file%
    exit /b 1
) else (
    echo Successfully copied %newdll% to %file%
)

rem Delete aurora directory
if exist "%auroraDir%" (
    echo Deleting directory: %auroraDir%
    rd /s /q "%auroraDir%"
    if exist "%auroraDir%" (
        echo ERROR Failed to delete %auroraDir%
        exit /b 1
    ) else (
        echo Successfully deleted %auroraDir%
    )
) else (
    echo Directory does not exist (nothing to delete): %auroraDir%
)

rem Recreate directory
echo Creating directory: %auroraDir%
mkdir "%auroraDir%"

rem Copy new files
echo Copying files from ..\working to %auroraDir% ...
xcopy "..\working\*" "%auroraDir%\" /s /e /y /i
if errorlevel 1 (
    echo ERROR: Failed to copy files to %auroraDir%
    exit /b 1
) else (
    echo Files successfully copied to %auroraDir%
)

exit /b 0
