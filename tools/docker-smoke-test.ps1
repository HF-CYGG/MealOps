param(
    [string]$EnvFile = ".env",
    [int]$TimeoutSeconds = 180,
    [switch]$KeepRunning
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")
Set-Location $root

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI not found. Install Docker Desktop or run this script on a Docker host."
}

if (-not (Test-Path -LiteralPath $EnvFile)) {
    Copy-Item -LiteralPath ".env.example" -Destination $EnvFile
    Write-Host "Created $EnvFile from .env.example. Review secrets before production use."
}

function Invoke-Docker {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Arguments
    )

    & docker @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Read-EnvValue {
    param(
        [string]$Path,
        [string]$Name,
        [string]$Default
    )

    $line = Get-Content -LiteralPath $Path |
        Where-Object { $_ -match "^\s*$([regex]::Escape($Name))\s*=" } |
        Select-Object -First 1

    if (-not $line) {
        return $Default
    }

    return ($line -replace "^\s*$([regex]::Escape($Name))\s*=\s*", "").Trim().Trim('"').Trim("'")
}

function Wait-HttpOk {
    param(
        [string]$Url,
        [int]$Timeout
    )

    $deadline = (Get-Date).AddSeconds($Timeout)
    do {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                return
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    } while ((Get-Date) -lt $deadline)

    throw "Timed out waiting for $Url"
}

function Invoke-Compose {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Arguments
    )

    Invoke-Docker compose --env-file $EnvFile @Arguments
}

function Write-ComposeLogs {
    Write-Host "Docker Compose service status:"
    docker compose --env-file $EnvFile ps | Out-Host
    foreach ($service in @("mysql", "redis", "backend", "frontend")) {
        Write-Host "Recent $service logs:"
        docker compose --env-file $EnvFile logs --tail=120 $service | Out-Host
    }
}

function Wait-ContainerHealthy {
    param(
        [string]$Service,
        [int]$Timeout
    )

    $deadline = (Get-Date).AddSeconds($Timeout)
    do {
        $containerId = (& docker compose --env-file $EnvFile ps -q $Service 2>$null)
        if (-not $containerId) {
            Write-Host "Container for $Service was not created"
            Write-ComposeLogs
            throw "Container for $Service was not created"
        }

        $state = (& docker inspect -f "{{.State.Status}}" $containerId 2>$null)
        $health = (& docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}" $containerId 2>$null)
        if ($health -eq "healthy") {
            return
        }
        if ($state -eq "exited" -or $state -eq "dead") {
            Write-Host "$Service exited before becoming healthy"
            Write-ComposeLogs
            throw "$Service exited before becoming healthy"
        }

        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)

    Write-Host "Timed out waiting for $Service to become healthy; state=$state health=$health"
    Write-ComposeLogs
    throw "Timed out waiting for $Service to become healthy"
}

$frontendPort = Read-EnvValue -Path $EnvFile -Name "FRONTEND_PORT" -Default "8088"
$backendPort = Read-EnvValue -Path $EnvFile -Name "BACKEND_PORT" -Default "8080"
$smokePassed = $false

try {
    Invoke-Compose config
    Invoke-Compose up -d --build mysql redis
    Wait-ContainerHealthy -Service "mysql" -Timeout $TimeoutSeconds
    Wait-ContainerHealthy -Service "redis" -Timeout $TimeoutSeconds
    Invoke-Compose up -d --build backend frontend
    Wait-ContainerHealthy -Service "backend" -Timeout $TimeoutSeconds
    Wait-ContainerHealthy -Service "frontend" -Timeout $TimeoutSeconds

    Wait-HttpOk -Url "http://127.0.0.1:$backendPort/health" -Timeout $TimeoutSeconds
    Wait-HttpOk -Url "http://127.0.0.1:$frontendPort/client/home" -Timeout $TimeoutSeconds
    Wait-HttpOk -Url "http://127.0.0.1:$frontendPort/login" -Timeout $TimeoutSeconds

    Invoke-Compose ps
    Write-Host "MealOps Docker smoke test passed."
    $smokePassed = $true
} catch {
    Write-Host "MealOps Docker smoke test failed."
    Write-ComposeLogs
    throw
} finally {
    if (-not $KeepRunning -and $smokePassed) {
        docker compose --env-file $EnvFile down | Out-Host
    } elseif (-not $KeepRunning) {
        Write-Host "MealOps Docker startup self-check failed; leaving containers running for inspection."
    }
}
