# ============================================================================
# VRPTW Campaign Diagnostic: 10 runs each (SA + TABU) to start the report
# Targets: data101, data111, data201 (1 seed each)
# Purpose: Quick validation before full Campaign 3
# ============================================================================

$base_dir = "c:\Users\darkf\Desktop\Travail\VRPTW"
Set-Location $base_dir

# Bypass execution policy for this session
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force

# Clean and compile
Write-Host "=== Compiling Java project ===" -ForegroundColor Green
Remove-Item -Recurse -Force bin -ErrorAction SilentlyContinue
$compile_output = javac --release 21 -d bin src/vrptw/*.java 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed:" -ForegroundColor Red
    Write-Host $compile_output
    exit 1
}
Write-Host "Compilation successful" -ForegroundColor Green

# Parameters (best from Campaign 2)
$instances = @("data101.vrp", "data111.vrp", "data201.vrp")
$seed = 41
$penalty_weight = 1000.0
$iterations = 30000
$max_vehicles = 2147483647

# SA parameters (best from Campaign 2)
$sa_temp = 1250
$sa_cooling = 0.9993

# TABU parameters (best from Campaign 2)
$tabu_tenure = 40

# Neighborhood (inter-relocate proved best)
$neighborhood_family = "inter"
$inter_type = "relocate"

$run_count = 0
$start_time = Get-Date

foreach ($instance in $instances) {
    Write-Host "`n=== Testing instance: $instance ===" -ForegroundColor Cyan
    
    # ========== SA RUN ==========
    Write-Host "Running SA..." -ForegroundColor Yellow
    $input_data = @"
non
data/$instance
sa
$iterations
$seed
$penalty_weight
non
$max_vehicles
oui
$neighborhood_family
$inter_type
$sa_temp
$sa_cooling
n
"@ -replace "`r`n", "`n"
    
    $output = $input_data | java -cp bin vrptw.Main 2>&1
    $run_count++
    Write-Host "  SA run $run_count completed" -ForegroundColor Green
    
    # ========== TABU RUN ==========
    Write-Host "Running TABU..." -ForegroundColor Yellow
    $input_data = @"
non
data/$instance
tabu
$iterations
$seed
$penalty_weight
non
$max_vehicles
oui
$neighborhood_family
$inter_type
$tabu_tenure
n
"@ -replace "`r`n", "`n"
    
    $output = $input_data | java -cp bin vrptw.Main 2>&1
    $run_count++
    Write-Host "  TABU run $run_count completed" -ForegroundColor Green
}

# Summary
$end_time = Get-Date
$elapsed = $end_time - $start_time
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Campaign Diagnostic Complete!" -ForegroundColor Green
Write-Host "Total runs: $run_count (SA + TABU per instance)" -ForegroundColor Green
Write-Host "Elapsed time: $($elapsed.TotalSeconds) seconds" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nNext: Analyze results and start report" -ForegroundColor Cyan
