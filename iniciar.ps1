# Levanta todo el sistema: cada servicio se abre en su propia ventana de PowerShell.
# Uso:  powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
param(
    [string]$RedisUrl = $env:REDIS_URL,
    # Proyecto Spring Boot de donde copiar el Maven Wrapper si falta
    [string]$WrapperDesde = "$env:USERPROFILE\OneDrive\Escritorio\demo-app-estudiantes"
)

$raiz = $PSScriptRoot
$servicios = "ms-contribuyentes", "ms-vehiculos", "ms-licencias", "api-gateway"

# 1. Maven Wrapper en cada microservicio
foreach ($s in $servicios) {
    if (-not (Test-Path "$raiz\$s\mvnw.cmd")) {
        if (-not (Test-Path "$WrapperDesde\mvnw.cmd")) {
            Write-Host "Falta mvnw.cmd. Indica un proyecto Spring Boot que lo tenga:" -ForegroundColor Red
            Write-Host "  .\iniciar.ps1 -WrapperDesde 'C:\ruta\a\un\proyecto-spring'" -ForegroundColor Yellow
            exit 1
        }
        Copy-Item "$WrapperDesde\mvnw", "$WrapperDesde\mvnw.cmd" "$raiz\$s" -Force
        Copy-Item "$WrapperDesde\.mvn" "$raiz\$s" -Recurse -Force
        Write-Host "Maven Wrapper copiado en $s"
    }
}

# 2. URL de la cache Cloud (Upstash)
if (-not $RedisUrl) {
    $RedisUrl = Read-Host "Pega la URL de Redis de Upstash (rediss://default:...@...upstash.io:6379)"
}

# 3. Backend: un proceso por servicio
foreach ($s in $servicios) {
    $cmd = "`$host.UI.RawUI.WindowTitle='$s'; "
    if ($s -eq "ms-licencias") { $cmd += "`$env:REDIS_URL='$RedisUrl'; " }
    $cmd += "Set-Location '$raiz\$s'; .\mvnw.cmd spring-boot:run"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
}

# 4. Frontend React
$front = "`$host.UI.RawUI.WindowTitle='frontend'; Set-Location '$raiz\frontend'; " +
         "if (-not (Test-Path .env)) { Copy-Item .env.example .env }; " +
         "if (-not (Test-Path node_modules)) { npm install }; npm run dev"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $front

Write-Host ""
Write-Host "Se abrieron 5 ventanas. Cuando todas digan 'Started' o 'Local:', abre http://localhost:5173" -ForegroundColor Green
