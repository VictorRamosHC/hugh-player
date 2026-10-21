@echo off
REM Hugh Player // Script de Reorganização de Pastas via ADB

"C:\Users\Victo\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell mkdir -p "/sdcard/Music/Musicas Cristas/Cantado"
"C:\Users\Victo\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell mkdir -p "/sdcard/Music/Musicas Cristas/Playback"
"C:\Users\Victo\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell mkdir -p "/sdcard/Music/Musicas Dia a Dia"
"C:\Users\Victo\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell mkdir -p "/sdcard/Music/Audios Reunioes"

echo Pastas fisicas criadas com sucesso no celular!
