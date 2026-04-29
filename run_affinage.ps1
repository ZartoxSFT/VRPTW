param(
    [string]$ClassPath = "bin"
)

$ErrorActionPreference = "Stop"

function Invoke-VrptwInteractiveRun {
    param(
        [string]$InstancePath,
        [ValidateSet("sa", "tabu")]
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

    $neighborhoodFamily = "inter"
    $interNeighborhoodType = "relocate"

    $answers = @(
        "non",
        $InstancePath,
        $Algo,
        "$RunIterations",
        "$Seed",
        "$RunPenaltyWeight",
        $RunEstimateMinVehicles,
        "$RunMaxVehicles",
        $EnforceTimeWindows,
        $neighborhoodFamily,
        $interNeighborhoodType
    )

    if ($Algo -eq "sa") {
        $answers += @("$InitialTemp", "$CoolingRate")
    }

    if ($Algo -eq "tabu") {
        $answers += @("$TabuTenure")
    }

    $stdinPayload = ($answers -join "`r`n") + "`r`n"
    $stdinPayload | & java -cp $RunClassPath vrptw.Main
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed for algo=$Algo seed=$Seed penalty=$RunPenaltyWeight"
    }
}

if (-not (Test-Path (Join-Path $ClassPath "vrptw/Main.class"))) {
    throw "Cannot find compiled classes in '$ClassPath'. Compile first."
}

# === AFFINAGE RAPIDE: Test data101 WITH TW ===
# 4 penalty levels x 2 algos x 2 seeds = 16 runs

$instance = "data/data101.vrp"
$penaltyWeights = @(1000, 10000, 50000, 100000)
$seeds = @(42, 43)  # 2 seeds only
$tw = "oui"
$algos = @("sa", "tabu")
$iterations = 100000

$saInitialTemp = 1250.0
$saCoolingRate = 0.9993
$tabuTenure = 40

$campaignStamp = Get-Date -Format "yyyyMMdd_HHmmss"
$planPath = "affinage_plan_$campaignStamp.csv"
$progressPath = "affinage_progress_$campaignStamp.csv"

$planRows = @()
foreach ($penalty in $penaltyWeights) {
    foreach ($seed in $seeds) {
        foreach ($algo in $algos) {
            $planRows += [pscustomobject]@{
                instance = $instance
                seed = $seed
                penalty_weight = $penalty
                algo = $algo
                iterations = $iterations
            }
        }
    }
}

$planRows | Export-Csv -Path $planPath -NoTypeInformation
$total = $planRows.Count

Write-Host ""
Write-Host "========================================================================"
Write-Host "AFFINAGE RAPIDE - Test penalty_weight for data101 WITH TW"
Write-Host "========================================================================"
Write-Host "Total runs: $total"
Write-Host "Penalties: $($penaltyWeights -join ', ')"
Write-Host "Algos: $($algos -join ', ')"
Write-Host "Seeds: $($seeds -join ', ')"
Write-Host ""

$index = 0

foreach ($row in $planRows) {
    $index++
    Write-Host "[$index/$total] $($row.algo) | penalty=$($row.penalty_weight) | seed=$($row.seed)"

    try {
        $invokeParams = @{
            InstancePath = $row.instance
            Algo = $row.algo
            RunIterations = $iterations
            Seed = [long]$row.seed
            RunPenaltyWeight = [double]$row.penalty_weight
            RunEstimateMinVehicles = "oui"
            RunMaxVehicles = 2147483647
            EnforceTimeWindows = $tw
            InitialTemp = [double]$saInitialTemp
            CoolingRate = [double]$saCoolingRate
            TabuTenure = [int]$tabuTenure
            RunClassPath = $ClassPath
        }

        Invoke-VrptwInteractiveRun @invokeParams

        [pscustomobject]@{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            status = "ok"
            index = $index
            algo = $row.algo
            penalty = $row.penalty_weight
            seed = $row.seed
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append
    }
    catch {
        [pscustomobject]@{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            status = "error"
            index = $index
            algo = $row.algo
            penalty = $row.penalty_weight
            seed = $row.seed
            error = $_.Exception.Message
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append
        throw
    }
}

Write-Host ""
Write-Host "========================================================================"
Write-Host "Affinage completed!"
Write-Host "Plan: $planPath"
Write-Host "Progress: $progressPath"
Write-Host "========================================================================"
