# Prepara la ponencia: APK en la red local (codigo QR) y espejo del celular en el computador.
param(
    [int]$Puerto = 8765,
    [switch]$Compilar,
    [switch]$SinEspejo
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Web = Join-Path $Root "ponencia"
$Dist = Join-Path $Web "dist"
$ApkName = "MiCarro.apk"
$BuiltApk = Join-Path $Root "app\build\outputs\apk\release\app-release.apk"

function Find-Python {
    $candidates = @(
        "$env:LOCALAPPDATA\Programs\Python\Python313\python.exe",
        "$env:LOCALAPPDATA\Programs\Python\Python312\python.exe",
        "$env:LOCALAPPDATA\Programs\Python\Python311\python.exe",
        "C:\miniconda\python.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) { return $candidate }
    }
    foreach ($name in @("py", "python")) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($cmd -and $cmd.Source -and $cmd.Source -notlike "*WindowsApps*") {
            return $cmd.Source
        }
    }
    return $null
}

function Get-LanIPv4 {
    $configs = @(Get-NetIPConfiguration -ErrorAction SilentlyContinue | Where-Object {
            $_.NetAdapter.Status -eq "Up" -and $_.IPv4DefaultGateway -and $_.IPv4Address
        })
    $preferred = $configs | Where-Object { $_.InterfaceAlias -match "Wi-Fi|WLAN|Wireless" } | Select-Object -First 1
    if (-not $preferred) { $preferred = $configs | Select-Object -First 1 }
    if ($preferred) {
        $addr = $preferred.IPv4Address | Where-Object { $_.IPAddress -notlike "127.*" } | Select-Object -First 1
        if ($addr) { return $addr.IPAddress }
    }
    $fallback = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
        Where-Object { $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*" } |
        Select-Object -First 1
    if ($fallback) { return $fallback.IPAddress }
    return $null
}

function Find-FreePort([int]$Start) {
    for ($port = $Start; $port -lt ($Start + 20); $port++) {
        $listener = $null
        try {
            $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $port)
            $listener.Start()
            return $port
        } catch {
            continue
        } finally {
            if ($listener) { $listener.Stop() }
        }
    }
    throw "No hay un puerto libre cerca de $Start."
}

function Find-Adb {
    $cmd = Get-Command adb -ErrorAction SilentlyContinue
    if ($cmd -and $cmd.Source -and $cmd.Source -notlike "*WindowsApps*") { return $cmd.Source }
    $sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $sdk) { return $sdk }
    if ($env:ANDROID_HOME) {
        $home = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
        if (Test-Path $home) { return $home }
    }
    return $null
}

function Find-Scrcpy {
    $cmd = Get-Command scrcpy -ErrorAction SilentlyContinue
    if ($cmd -and $cmd.Source -and $cmd.Source -notlike "*WindowsApps*") { return $cmd.Source }
    $roots = @(
        "$env:LOCALAPPDATA\Microsoft\WinGet\Links",
        "$env:LOCALAPPDATA\Microsoft\WinGet\Packages",
        "$env:ProgramFiles\scrcpy",
        "$env:LOCALAPPDATA\scrcpy"
    )
    foreach ($root in $roots) {
        if (-not (Test-Path $root)) { continue }
        $hit = Get-ChildItem $root -Recurse -Filter scrcpy.exe -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($hit) { return $hit.FullName }
    }
    return $null
}

function Get-AdbDevices([string]$Adb) {
    $lines = @(& $Adb devices)
    $serials = @()
    foreach ($line in $lines) {
        if ($line -match "^(\S+)\s+device$") { $serials += $Matches[1] }
    }
    return $serials
}

Write-Host ""
Write-Host "My Car — ponencia" -ForegroundColor Cyan
Write-Host ""

if ($Compilar -or -not (Test-Path $BuiltApk)) {
    Write-Host "Compilando el APK instalable. La primera vez puede tardar varios minutos..."
    & (Join-Path $Root "gradlew.bat") ":app:assembleRelease" "--console=plain"
    if ($LASTEXITCODE -ne 0) { throw "La compilacion fallo. Revisa el mensaje de Gradle." }
}

if (-not (Test-Path $BuiltApk)) {
    throw "No esta el APK en $BuiltApk"
}

New-Item -ItemType Directory -Force -Path $Dist | Out-Null
Copy-Item (Join-Path $Web "index.html") (Join-Path $Dist "index.html") -Force
Copy-Item (Join-Path $Web "qrcode.min.js") (Join-Path $Dist "qrcode.min.js") -Force
Copy-Item $BuiltApk (Join-Path $Dist $ApkName) -Force

$python = Find-Python
if (-not $python) { throw "No se encontro Python. Instala Python 3 y vuelve a ejecutar presentar.bat." }

$Puerto = Find-FreePort $Puerto
$ip = Get-LanIPv4
if (-not $ip) { throw "No hay una IP de red. Conecta el computador al Wi-Fi del salon." }
$url = "http://${ip}:$Puerto/"

Write-Host "Si Windows pregunta, permite Python solo en redes privadas."

if (-not $SinEspejo) {
    $adb = Find-Adb
    $scrcpy = Find-Scrcpy
    if (-not $scrcpy) {
        Write-Host "Instalando scrcpy para mostrar el celular en el proyector..."
        winget install --id Genymobile.scrcpy -e --accept-package-agreements --accept-source-agreements
        $scrcpy = Find-Scrcpy
    }
    if (-not $adb) {
        Write-Host "No esta adb. Abre Android Studio una vez o instala platform-tools."
    } else {
        $serials = @(Get-AdbDevices $adb)
        if ($serials.Count -eq 0) {
            Write-Host ""
            Write-Host "No hay un celular conectado. Para transmitir la pantalla:" -ForegroundColor Yellow
            Write-Host "  1. En el telefono: Ajustes, Acerca del telefono, pulsa 7 veces el numero de compilacion."
            Write-Host "  2. Ajustes, Opciones de desarrollador, activa Depuracion USB."
            Write-Host "  3. Conecta el cable y acepta este computador."
            Write-Host "  4. Vuelve a ejecutar presentar.bat."
            Write-Host ""
        } else {
            $serial = $serials[0]
            $path = & $adb -s $serial shell pm path com.micarro
            if ($path -notmatch "package:") {
                Write-Host "Instalando My Car en el celular conectado..."
                & $adb -s $serial install -r (Join-Path $Dist $ApkName)
            } else {
                Write-Host "El celular ya tiene My Car. No la reinstalo, para no borrar los datos de la demo."
            }
            if ($scrcpy) {
                Write-Host "Abriendo la pantalla del celular. Arrastra esa ventana al proyector."
                Start-Process -FilePath $scrcpy -WorkingDirectory (Split-Path $scrcpy) -ArgumentList @(
                    "--serial=$serial",
                    "--window-title=MyCar",
                    "--max-size=1920"
                )
            } else {
                Write-Host "scrcpy no quedo instalado. Ejecuta: winget install --id Genymobile.scrcpy -e"
            }
        }
    }
}

Write-Host ""
Write-Host "Descarga para el publico: $url" -ForegroundColor Green
Write-Host "Deja esta ventana abierta. El navegador muestra el codigo QR."
Write-Host "Celulares y computador en el mismo Wi-Fi. Si el salon aisla los equipos, usa el cable para tu telefono."
Write-Host "Ctrl+C cierra solo la descarga."
Write-Host ""

Start-Process $url
& $python (Join-Path $Web "servir.py") "--dir" $Dist "--port" "$Puerto"
