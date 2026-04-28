<#
.SYNOPSIS
    Campaign 3: Comprehensive analysis of SA vs Tabu with time windows focus
    
.DESCRIPTION
    Runs a targeted campaign on 3 representative instances (data101, data111, data1101)
    with 10 seeds each, testing both TW modes and varying iterations.
    
    Objective: Generate sufficient data for robust comparative analysis with discussion
    on time windows impact, iteration budget vs quality tradeoff, and algorithm robustness.

.PARAMETER Iterations
    Comma-separated list of iteration counts to test (e.g., "10000,30000,100000")
    Default: 10000,30000,100000

.PARAMETER PenaltyWeight
    Penalty weight for constraint violations
    Default: 1000.0

.PARAMETER EstimateMinVehicles
    Whether to estimate minimum vehicles ("oui" or "non")
    Default: "oui"

.PARAMETER MaxVehicles
    Maximum number of vehicles allowed
    Default: 2147483647

.PARAMETER ClassPath
    Path to compiled Java classes
    Default: "bin"

.PARAMETER SkipCompile
    Skip compilation check if already compiled
    Default: $false

.EXAMPLE
    # Run full campaign with default settings (will take 1-2 hours)
    .\run_campaign3.ps1
    
.EXAMPLE
    # Run with custom iterations (smaller campaign)
    .\run_campaign3.ps1 -Iterations "10000,30000" -ClassPath "bin"

#>

param(
    [string]$Iterations = "10000,30000,100000",
    [double]$PenaltyWeight = 1000.0,
    [string]$EstimateMinVehicles = "oui",
    [int]$MaxVehicles = 2147483647,
    [string]$ClassPath = "bin",
    [switch]$SkipCompile
)

$ErrorActionPreference = "Stop"

#==============================================================================
# SETUP & VALIDATION
#==============================================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   VRPTW Campaign 3 - Comprehensive TW Analysis" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Parse iteration list
$iterationsList = @($Iterations -split "," | ForEach-Object { [int]($_.Trim()) })
if ($iterationsList.Count -eq 0) {
    throw "Invalid iterations parameter. Use comma-separated values."
}

# Verify compilation
if (-not $SkipCompile) {
    if (-not (Test-Path (Join-Path $ClassPath "vrptw/Main.class"))) {
        throw @"
Cannot find compiled classes in '$ClassPath'.
Compile first with:
  javac --release 21 -d bin src/vrptw/*.java
Or use -SkipCompile flag if already compiled.
"@
    }
}

#==============================================================================
# CAMPAIGN CONFIGURATION
#==============================================================================

# Instance selection: 3 representative sizes
$instances = @(
    "data/data101.vrp",      # Small reference (10-20 clients)
    "data/data111.vrp",      # Medium (100 clients)
    "data/data1101.vrp"      # Large/complex (1100+ clients)
)

# 10 seeds for statistical robustness
$seeds = @(41, 42, 43, 44, 45, 101, 102, 103, 104, 105)

# Both TW modes: without and with time windows
$twModes = @("non", "oui")

# Best parameters found in campaigns 1 & 2
$saInitialTemp = 1250.0
$saCoolingRate = 0.9993
$tabuTenure = 40

$algos = @("sa", "tabu")

Write-Host "CAMPAIGN CONFIGURATION:" -ForegroundColor Yellow
Write-Host "  Instances: $($instances.Count) files"
foreach ($inst in $instances) {
    Write-Host "    - $inst"
}
Write-Host "  Seeds: $($seeds.Count) ($($seeds -join ', '))"
Write-Host "  TW Modes: $($twModes.Count) ($($twModes -join ', '))"
Write-Host "  Iterations per run: $($iterationsList -join ', ')"
Write-Host "  Algorithms: SA (T=$saInitialTemp, C=$saCoolingRate), Tabu (tenure=$tabuTenure)"
Write-Host ""

#==============================================================================
# GENERATE PLAN
#==============================================================================

$campaignStamp = Get-Date -Format "yyyyMMdd_HHmmss"
$planPath = "campaign3_plan_$campaignStamp.csv"
$progressPath = "campaign3_progress_$campaignStamp.csv"

$planRows = @()

foreach ($instancePath in $instances) {
    foreach ($seed in $seeds) {
        foreach ($tw in $twModes) {
            foreach ($algo in $algos) {
                foreach ($iter in $iterationsList) {
                    $planRows += [pscustomobject]@{
                        instance = $instancePath
                        seed = $seed
                        enforce_time_windows = $tw
                        algo = $algo
                        iterations = $iter
                        neighborhood_family = "inter"
                        inter_type = "relocate"
                        sa_initial_temp = $saInitialTemp
                        sa_cooling_rate = $saCoolingRate
                        tabu_tenure = $tabuTenure
                        penalty_weight = $PenaltyWeight
                    }
                }
            }
        }
    }
}

# Export plan
$planRows | Export-Csv -Path $planPath -NoTypeInformation -Encoding UTF8

$total = $planRows.Count
Write-Host "PLAN GENERATION:" -ForegroundColor Yellow
Write-Host "  Total runs: $total"
Write-Host "  Calculation: $($instances.Count) x $($seeds.Count) x $($twModes.Count) x $($algos.Count) x $($iterationsList.Count)"
Write-Host "  Plan file: $planPath"
Write-Host "  Progress file: $progressPath"
Write-Host ""

#==============================================================================
# EXECUTION FUNCTION
#==============================================================================

function Invoke-VrptwRun {
    param(
        [string]$InstancePath,
        [string]$Algo,
        [int]$RunIterations,
        [long]$Seed,
        [double]$RunPenaltyWeight,
        [string]$RunEstimateMinVehicles,
        [int]$RunMaxVehicles,
        [string]$EnforceTimeWindows,
        [double]$InitialTemp,
        [double]$CoolingRate,
        [int]$TabuTenure,
        [string]$RunClassPath
    )

    # Fixed: best neighborhood found in campaigns 1-2
    $neighborhoodFamily = "inter"
    $interNeighborhoodType = "relocate"

    $answers = @(
        "non",                           # Interactive mode off
        $InstancePath,                   # Instance file
        $Algo,                           # Algorithm (sa/tabu)
        "$RunIterations",                # Iterations
        "$Seed",                         # Seed
        "$RunPenaltyWeight",             # Penalty weight
        $RunEstimateMinVehicles,         # Estimate min vehicles
        "$RunMaxVehicles",               # Max vehicles
        $EnforceTimeWindows,             # Time windows (oui/non)
        $neighborhoodFamily,             # Neighborhood family
        $interNeighborhoodType           # Inter neighborhood type
    )

    # Algorithm-specific parameters
    if ($Algo -eq "sa") {
        $answers += @("$InitialTemp", "$CoolingRate")
    } elseif ($Algo -eq "tabu") {
        $answers += @("$TabuTenure")
    }

    $stdinPayload = ($answers -join "`r`n") + "`r`n"
    $stdinPayload | & java -cp $RunClassPath vrptw.Main 2>&1 | Out-Null
    
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed: instance=$InstancePath algo=$Algo seed=$Seed tw=$EnforceTimeWindows iterations=$RunIterations"
    }
}

#==============================================================================
# EXECUTION LOOP WITH PROGRESS TRACKING
#==============================================================================

$progressRows = @()
$startTime = Get-Date

Write-Host "EXECUTION STARTED" -ForegroundColor Green
Write-Host "Start time: $startTime"
Write-Host ""

$index = 0
$failureCount = 0

foreach ($row in $planRows) {
    $index++
    $progress = $index / $total * 100
    
    # Extract readable labels
    $instName = Split-Path $row.instance -Leaf
    $twLabel = if ($row.enforce_time_windows -eq "oui") { "TW" } else { "noTW" }
    $algoLabel = $row.algo.ToUpper()
    
    Write-Host "[$index/$total ($([math]::Round($progress, 1))%)] $algoLabel | $instName | $twLabel | iter=$($row.iterations) | seed=$($row.seed)" `
        -ForegroundColor Cyan
    
    try {
        $invokeParams = @{
            InstancePath = $row.instance
            Algo = $row.algo
            RunIterations = [int]$row.iterations
            Seed = [long]$row.seed
            RunPenaltyWeight = $PenaltyWeight
            RunEstimateMinVehicles = $EstimateMinVehicles
            RunMaxVehicles = $MaxVehicles
            EnforceTimeWindows = $row.enforce_time_windows
            InitialTemp = [double]$row.sa_initial_temp
            CoolingRate = [double]$row.sa_cooling_rate
            TabuTenure = [int]$row.tabu_tenure
            RunClassPath = $ClassPath
        }
        
        Invoke-VrptwRun @invokeParams
        
        $status = "ok"
        $message = ""
    } catch {
        $status = "fail"
        $message = $_.Exception.Message
        $failureCount++
        Write-Host "  ERROR: $message" -ForegroundColor Red
    }
    
    $progressRows += [pscustomobject]@{
        timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        status = $status
        index = $index
        total = $total
        instance = $row.instance
        enforce_time_windows = $row.enforce_time_windows
        seed = $row.seed
        algo = $row.algo
        iterations = $row.iterations
        message = $message
    }
    
    # Save progress periodically
    if ($index % 20 -eq 0 -or $index -eq $total) {
        $progressRows | Export-Csv -Path $progressPath -NoTypeInformation -Encoding UTF8 -Force
    }
}

$endTime = Get-Date
$duration = $endTime - $startTime

#==============================================================================
# SUMMARY
#==============================================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "   CAMPAIGN 3 EXECUTION COMPLETED" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$successCount = $total - $failureCount
$successRate = $successCount / $total * 100

Write-Host "SUMMARY:" -ForegroundColor Yellow
Write-Host "  Total runs: $total"
Write-Host "  Successful: $successCount ($([math]::Round($successRate, 1))%)"
Write-Host "  Failed: $failureCount"
Write-Host "  Duration: $([math]::Round($duration.TotalHours, 2)) hours"
Write-Host "  Start: $startTime"
Write-Host "  End: $endTime"
Write-Host ""

Write-Host "OUTPUT FILES:" -ForegroundColor Yellow
Write-Host "  Plan: $planPath"
Write-Host "  Progress: $progressPath"
Write-Host ""

Write-Host "NEXT STEPS FOR ANALYSIS:" -ForegroundColor Cyan
Write-Host "  1. Consolidate all executions_log.csv from resultsSA/ and resultTABU/"
Write-Host "  2. Generate CSV tables: by instance, by iterations, by TW mode"
Write-Host "  3. Create box plots: distance distribution by config"
Write-Host "  4. Analyze convergence curves: iterations vs quality tradeoff"
Write-Host "  5. Compare robustness: stddev and feasibility rates"
Write-Host ""

if ($failureCount -gt 0) {
    Write-Host "WARNING: $failureCount runs failed. Check $progressPath for details." -ForegroundColor Red
} else {
    Write-Host "All runs completed successfully!" -ForegroundColor Green
}

Write-Host ""
