Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Starting AI-Based Recommendation System (Java + Web)" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

$jdkBin = "C:\Users\asus\.jdks\jdk-17.0.20.1+1\bin"
if (Test-Path "$jdkBin\javac.exe") {
    $env:Path = "$jdkBin;" + $env:Path
}

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java classes..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path src/main/java -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
& javac -encoding UTF-8 -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "[2/2] Launching server on http://localhost:8080 ..." -ForegroundColor Green
& java -cp bin com.recommendation.Main 8080
