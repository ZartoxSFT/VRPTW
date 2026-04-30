param(
    [int]$MaxRuns = 500,
    [string]$ClassPath = "bin"
)

$ErrorActionPreference = "Stop"

function Invoke-VrptwInteractiveRun {
    param(
        [string]$InstancePath,
        [int]$RunIterations,
        [long]$Seed,
        [double]$RunPenaltyWeight,
        [string]$RunEstimateMinVehicles,
        [int]$RunMaxVehicles,
        [string]$EnforceTimeWindows,
        [double]$InitialTemp,
        [double]$CoolingRate,
        [string]$RunClassPath
    )

    $neighborhoodFamily = "inter"
    $interNeighborhoodType = "relocate"

    $answers = @(
        "non",
        $InstancePath,
        "sa",
        "$RunIterations",
        "$Seed",
        "$RunPenaltyWeight",
        $RunEstimateMinVehicles,
        "$RunMaxVehicles",
        $EnforceTimeWindows,
        $neighborhoodFamily,
        $interNeighborhoodType,
        "$InitialTemp",
        "$CoolingRate"
    )

    $stdinPayload = ($answers -join "`r`n") + "`r`n"
    $stdinPayload | & java -cp $RunClassPath vrptw.Main
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed"
    }
}

if (-not (Test-Path (Join-Path $ClassPath "vrptw/Main.class"))) {
    throw "Cannot find compiled classes in '$ClassPath'"
}

# === PARAMETER SWEEP FOR SA ===
# Goal: Find the best natural feasible solution for SA with TW enabled only.

$instance = "data/data101.vrp"
$tw = "oui"

# Exploratory ranges: keep the search natural, do not target a specific vehicle count.
$penaltyWeights = @(250, 500, 1000, 2500, 5000, 10000, 50000)     # 7 levels
$initialTemps = @(250, 400, 500, 750, 1000, 1250, 1500, 2000)     # 8 levels
$coolingRates = @(0.995, 0.9975, 0.998, 0.999, 0.9993, 0.9995)    # 6 levels
$iterations = @(30000, 50000, 100000, 150000)                     # 4 levels
$seeds = @(101, 102, 103, 104, 105)                               # 5 seeds

# Generate the full factorial design, then sample it if MaxRuns is lower.
$allRows = @()
foreach ($penalty in $penaltyWeights) {
    foreach ($temp in $initialTemps) {
        foreach ($cooling in $coolingRates) {
            foreach ($iter in $iterations) {
                foreach ($seed in $seeds) {
                    $allRows += [pscustomobject]@{
                        penalty = $penalty
                        temp = $temp
                        cooling = $cooling
                        iterations = $iter
                        seed = $seed
                    }
                }
            }
        }
    }
}

$planRows = $allRows | Get-Random -Count ([math]::Min($MaxRuns, $allRows.Count))
$runCount = $planRows.Count

$total = $planRows.Count
$designTotal = $allRows.Count

Write-Host ""
Write-Host "========================================================================"
Write-Host "SA PARAMETER SWEEP - Natural TW-feasible search"
Write-Host "========================================================================"
Write-Host "Total runs planned: $total"
Write-Host "Total design combinations: $designTotal"
Write-Host "PenaltyWeights: $($penaltyWeights.Count) levels"
Write-Host "InitialTemps:   $($initialTemps.Count) levels"
Write-Host "CoolingRates:   $($coolingRates.Count) levels"
Write-Host "Iterations:     $($iterations.Count) levels"
Write-Host "Seeds:          $($seeds.Count) seeds"
Write-Host ""
Write-Host "Design: $($penaltyWeights.Count) × $($initialTemps.Count) × $($coolingRates.Count) × $($iterations.Count) × $($seeds.Count) = $designTotal combinations"
Write-Host "Sampled: $total combinations (MaxRuns=$MaxRuns)"
Write-Host "Est. time: $([math]::Ceiling($total * 0.2)) minutes"
Write-Host "Note: all runs keep enforce_time_windows = oui and max_vehicles unrestricted"
Write-Host "========================================================================"
Write-Host ""

$campaignStamp = Get-Date -Format "yyyyMMdd_HHmmss"
$planPath = "sweep_plan_$campaignStamp.csv"
$progressPath = "sweep_progress_$campaignStamp.csv"

$planRows | Export-Csv -Path $planPath -NoTypeInformation

$index = 0
foreach ($row in $planRows) {
    $index++
    Write-Host "[$($index)/$total] penalty=$($row.penalty) temp=$($row.temp) cooling=$($row.cooling) iter=$($row.iterations) seed=$($row.seed)" -ForegroundColor Cyan

    try {
        $invokeParams = @{
            InstancePath = $instance
            RunIterations = $row.iterations
            Seed = [long]$row.seed
            RunPenaltyWeight = [double]$row.penalty
            RunEstimateMinVehicles = "oui"
            RunMaxVehicles = 2147483647
            EnforceTimeWindows = $tw
            InitialTemp = [double]$row.temp
            CoolingRate = [double]$row.cooling
            RunClassPath = $ClassPath
        }

        Invoke-VrptwInteractiveRun @invokeParams

        [pscustomobject]@{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            status = "ok"
            index = $index
            penalty = $row.penalty
            temp = $row.temp
            cooling = $row.cooling
            iterations = $row.iterations
            seed = $row.seed
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append -Encoding UTF8
    }
    catch {
        Write-Host "  ERROR: $($_.Exception.Message)" -ForegroundColor Red
        [pscustomobject]@{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            status = "error"
            index = $index
            penalty = $row.penalty
            temp = $row.temp
            cooling = $row.cooling
            iterations = $row.iterations
            seed = $row.seed
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append -Encoding UTF8
        # Continue anyway
    }
}

Write-Host ""
Write-Host "========================================================================"
Write-Host "SWEEP COMPLETED!"
Write-Host "Plan:     $planPath ($total runs)"
Write-Host "Progress: $progressPath"
Write-Host ""
Write-Host "Next: python scripts\analyze_sweep.py"
Write-Host "========================================================================"
