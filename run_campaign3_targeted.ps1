param(
    [int]$Iterations = 30000,
    [double]$PenaltyWeight = 1000.0,
    [string]$EstimateMinVehicles = "oui",
    [int]$MaxVehicles = 2147483647,
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

    # Campaign 3 is fixed to the best observed neighborhood setup.
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
        throw "Run failed for instance=$InstancePath algo=$Algo seed=$Seed tw=$EnforceTimeWindows"
    }
}

if (-not (Test-Path (Join-Path $ClassPath "vrptw/Main.class"))) {
    throw "Cannot find compiled classes in '$ClassPath'. Compile first (example: javac --release 21 -d bin src/vrptw/*.java)."
}

# Targeted 60-run campaign:
# 3 instances x 5 seeds x 2 TW modes x 2 algos = 60
$instances = @(
    "data/data101.vrp", # small/reference
    "data/data111.vrp", # medium
    "data/data201.vrp"  # larger/difficult
)
$seeds = @(41, 42, 43, 44, 45)
$twModes = @("non", "oui")
$algos = @("sa", "tabu")

# Fixed best observed parameters from previous analysis.
$saInitialTemp = 1250.0
$saCoolingRate = 0.9993
$tabuTenure = 40

$campaignStamp = Get-Date -Format "yyyyMMdd_HHmmss"
$planPath = "campaign3_plan_$campaignStamp.csv"
$progressPath = "campaign3_progress_$campaignStamp.csv"

$planRows = @()
foreach ($instancePath in $instances) {
    foreach ($seed in $seeds) {
        foreach ($tw in $twModes) {
            foreach ($algo in $algos) {
                $planRows += [pscustomobject]@{
                    instance = $instancePath
                    seed = $seed
                    enforce_time_windows = $tw
                    algo = $algo
                    neighborhood_family = "inter"
                    inter_type = "relocate"
                    sa_initial_temp = $saInitialTemp
                    sa_cooling_rate = $saCoolingRate
                    tabu_tenure = $tabuTenure
                    iterations = $Iterations
                    penalty_weight = $PenaltyWeight
                }
            }
        }
    }
}

$planRows | Export-Csv -Path $planPath -NoTypeInformation

$total = $planRows.Count
$index = 0

Write-Host "Campaign 3 targeted"
Write-Host "Instances: $($instances.Count) | Seeds: $($seeds.Count) | TW modes: $($twModes.Count) | Algos: $($algos.Count)"
Write-Host "Total runs planned: $total"
Write-Host "Plan file: $planPath"
Write-Host "Progress file: $progressPath"

foreach ($row in $planRows) {
    $index++
    Write-Host "[$index/$total] $($row.algo) | $($row.instance) | seed=$($row.seed) | TW=$($row.enforce_time_windows)"

    try {
        $invokeParams = @{
            InstancePath = $row.instance
            Algo = $row.algo
            RunIterations = $Iterations
            Seed = [long]$row.seed
            RunPenaltyWeight = $PenaltyWeight
            RunEstimateMinVehicles = $EstimateMinVehicles
            RunMaxVehicles = $MaxVehicles
            EnforceTimeWindows = $row.enforce_time_windows
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
            total = $total
            instance = $row.instance
            seed = $row.seed
            enforce_time_windows = $row.enforce_time_windows
            algo = $row.algo
            message = ""
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append
    }
    catch {
        [pscustomobject]@{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            status = "error"
            index = $index
            total = $total
            instance = $row.instance
            seed = $row.seed
            enforce_time_windows = $row.enforce_time_windows
            algo = $row.algo
            message = $_.Exception.Message
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append
        throw
    }
}

Write-Host "Campaign completed."
Write-Host "Plan: $planPath"
Write-Host "Progress: $progressPath"
