# Build Release AAB (Android App Bundle)
# This script builds a signed release AAB for Google Play Store

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Building To-Do App Release AAB" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project directory
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

# Check if keystore exists
if (-not (Test-Path "todoapp-release-key.jks")) {
    Write-Host "ERROR: Keystore file not found!" -ForegroundColor Red
    Write-Host "Please generate the keystore first." -ForegroundColor Yellow
    Write-Host "See DEPLOYMENT_GUIDE.md for instructions." -ForegroundColor Yellow
    exit 1
}

# Check if keystore.properties exists
if (-not (Test-Path "keystore.properties")) {
    Write-Host "ERROR: keystore.properties file not found!" -ForegroundColor Red
    Write-Host "Please create keystore.properties file." -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Keystore found" -ForegroundColor Green
Write-Host "✓ Configuration file found" -ForegroundColor Green
Write-Host ""

# Clean previous builds
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
./gradlew clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Clean failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Clean completed" -ForegroundColor Green
Write-Host ""

# Build release AAB
Write-Host "Building release AAB..." -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Gray
./gradlew bundleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Write-Host "Check the error messages above." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""

# Show output location
$aabPath = "app\build\outputs\bundle\release\app-release.aab"
if (Test-Path $aabPath) {
    $aabSize = (Get-Item $aabPath).Length / 1MB
    Write-Host "AAB Location: $aabPath" -ForegroundColor Cyan
    Write-Host "AAB Size: $([math]::Round($aabSize, 2)) MB" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Yellow
    Write-Host "  1. Go to Google Play Console" -ForegroundColor White
    Write-Host "  2. Create a new release" -ForegroundColor White
    Write-Host "  3. Upload: $aabPath" -ForegroundColor White
    Write-Host "  4. Fill in release notes" -ForegroundColor White
    Write-Host "  5. Submit for review" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "WARNING: AAB file not found at expected location" -ForegroundColor Yellow
}

Write-Host "ProGuard mapping file saved at:" -ForegroundColor Gray
Write-Host "  app\build\outputs\mapping\release\mapping.txt" -ForegroundColor Gray
Write-Host "  (Save this file! You'll need it to decode crash reports)" -ForegroundColor Gray
Write-Host ""
Write-Host "IMPORTANT: Upload the mapping file to Play Console!" -ForegroundColor Red
Write-Host "  Play Console > Release > Mapping file > Upload" -ForegroundColor Gray
Write-Host ""
