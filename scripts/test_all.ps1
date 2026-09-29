# SIH26042 Monorepo Unified Test Runner (PowerShell)
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [SIH26042] Running Monorepo Unified Smoke & Unit Tests   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$RepoRoot = Split-Path -Parent $PSScriptRoot
$env:PYTHONPATH = "$RepoRoot;$RepoRoot\packages\contracts\python;$RepoRoot\services\pack-builder;$RepoRoot\services\api;$env:PYTHONPATH"

# Resolve pytest executable
$PytestCmd = "pytest"
$UserPytest = "$env:APPDATA\Python\Python314\Scripts\pytest.exe"
if (Test-Path $UserPytest) {
    $PytestCmd = $UserPytest
}

# 1. Test Language Pack Schema
Write-Host "`n>>> [1/4] Testing Language Pack Schema..." -ForegroundColor Yellow
& $PytestCmd "$RepoRoot/packages/language-pack-schema" -v --tb=short
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAILED: Language Pack Schema tests" -ForegroundColor Red
    exit 1
}

# 2. Test Pack Builder
Write-Host "`n>>> [2/4] Testing Pack Builder Service..." -ForegroundColor Yellow
& $PytestCmd "$RepoRoot/services/pack-builder/tests" -v --tb=short
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAILED: Pack Builder tests" -ForegroundColor Red
    exit 1
}

# 3. Test FastAPI Backend & Alembic Migrations
Write-Host "`n>>> [3/4] Testing FastAPI Backend & Migrations..." -ForegroundColor Yellow
& $PytestCmd "$RepoRoot/services/api/tests" -v --tb=short
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAILED: Backend API tests" -ForegroundColor Red
    exit 1
}

# 4. Test Android Application
Write-Host "`n>>> [4/4] Testing Android Application (JVM Unit Tests)..." -ForegroundColor Yellow
Push-Location "$RepoRoot/apps/android"
try {
    .\gradlew.bat test --no-daemon
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FAILED: Android tests" -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}

# 5. Run Hindi ASR Quality Evaluation Benchmark
Write-Host "`n>>> [5/6] Running Standard Hindi ASR Evaluation..." -ForegroundColor Yellow
python "$RepoRoot/scripts/evaluate_hindi_asr.py"
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAILED: ASR Evaluation" -ForegroundColor Red
    exit 1
}

# 6. Run Hindi->Santali Neural MT Quality Evaluation Benchmark
Write-Host "`n>>> [6/6] Running Held-Out Hindi->Santali MT Evaluation..." -ForegroundColor Yellow
python "$RepoRoot/scripts/evaluate_hindi_santali_mt.py"
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAILED: MT Evaluation" -ForegroundColor Red
    exit 1
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " [SUCCESS] All SIH26042 Smoke, Unit & Benchmark Tests Passed!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

