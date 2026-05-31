#!/usr/bin/env pwsh
# test-ci-local.ps1
# Script to simulate the CI pipeline locally (Lint & Unit Tests)

$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🚀 Starting Local CI Pipeline 🚀" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Spotless Check
Write-Host "`n[1/3] Running Spotless Format & Lint Check..." -ForegroundColor Yellow
./gradlew spotlessCheck
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Spotless check failed! Please run './gradlew spotlessApply' to fix formatting." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Spotless check passed!" -ForegroundColor Green

# 2. Unit Tests
Write-Host "`n[2/3] Running Unit Tests..." -ForegroundColor Yellow
./gradlew testDebugUnitTest
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Unit tests failed! Please check the test reports." -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Unit tests passed!" -ForegroundColor Green

# 3. Kover Coverage Report
Write-Host "`n[3/3] Running Kover Coverage Report..." -ForegroundColor Yellow
./gradlew koverXmlReport
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Coverage generation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "✅ Code coverage report generated successfully!" -ForegroundColor Green

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "🎉 Success! All CI checks passed locally." -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
