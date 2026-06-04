#!/usr/bin/env pwsh
# test-ci-local.ps1
# Script to simulate the CI pipeline locally (Lint, Unit Tests, & Build)

$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🚀 Starting Local CI Pipeline 🚀" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Spotless Check
Write-Host "`n[1/4] Running Spotless Format & Lint Check..." -ForegroundColor Yellow
./gradlew spotlessCheck
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Spotless check failed! Please run './gradlew spotlessApply' to fix formatting." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Spotless check passed!" -ForegroundColor Green

# 2. Detekt Check
Write-Host "`n[2/4] Running Detekt Code Analysis..." -ForegroundColor Yellow
./gradlew detekt
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Detekt check failed! Please fix the reported issues." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Detekt check passed!" -ForegroundColor Green

# 3. Unit Tests
Write-Host "`n[3/4] Running Unit Tests..." -ForegroundColor Yellow
./gradlew test
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Unit tests failed! Please check the test reports." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Unit tests passed!" -ForegroundColor Green

# 4. Build Android Debug
Write-Host "`n[4/4] Building Android Debug APK..." -ForegroundColor Yellow
./gradlew :composeApp:assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build Android Debug failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Build Android Debug passed!" -ForegroundColor Green

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "🎉 Success! All CI checks passed locally." -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
