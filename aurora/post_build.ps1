$file = "C:\Program Files (x86)\Steam\steamapps\common\Thumper\steam_api64.dll"
$backup = "$file.bak"
$newdll = "..\bin\debug\aurora.dll"
$auroraDir = "C:\Program Files (x86)\Steam\steamapps\common\Thumper\aurora"
$workingDir = "..\working"

Write-Host "File: $file"
Write-Host "Backup: $backup"
Write-Host "New DLL to copy: $newdll"

if (-Not (Test-Path $backup)) {
    Write-Host "Creating backup..."
    Copy-Item -Path $file -Destination $backup -Force
    Write-Host "Backup created: $backup"
} else {
    Write-Host "Backup already exists: $backup"
}

Write-Host "Overwriting original file with new DLL..."
Copy-Item -Path $newdll -Destination $file -Force

if (-Not (Test-Path $file)) {
    Write-Error "Failed to copy $newdll to $file"
    exit 1
} else {
    Write-Host "Successfully copied $newdll to $file"
}

if (Test-Path $auroraDir) {
    Write-Host "Deleting directory: $auroraDir"
    Remove-Item -Path $auroraDir -Recurse -Force
    if (Test-Path $auroraDir) {
        Write-Error "Failed to delete $auroraDir"
        exit 1
    } else {
        Write-Host "Successfully deleted $auroraDir"
    }
} else {
    Write-Host "Directory does not exist (nothing to delete): $auroraDir"
}

Write-Host "Creating directory: $auroraDir"
New-Item -Path $auroraDir -ItemType Directory | Out-Null

Write-Host "Copying files from $workingDir to $auroraDir"
Copy-Item -Path "$workingDir\*" -Destination $auroraDir -Recurse -Force

Write-Host "Done."
exit 0