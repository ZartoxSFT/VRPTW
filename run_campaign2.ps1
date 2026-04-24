param(
    [ValidateSet("comparison", "tuning", "full")]
    [string]$Campaign = "full",
    [ValidateSet("quick", "full")]
    [string]$Scale = "full",
    [int]$Iterations = 30000,
    [double]$PenaltyWeight = 1000.0,
    [string]$EstimateMinVehicles = "oui",
    [int]$MaxVehicles = 2147483647,
    [string]$ClassPath = "bin"
)

$ErrorActionPreference = "Stop"

function Get-ScenarioSet {
    param(
        [string]$CampaignName
    )

    $scenarios = @()

    if ($CampaignName -eq "comparison" -or $CampaignName -eq "full") {
        # Neighborhood comparison with best-known baseline parameters.
        $scenarios += [pscustomobject]@{
            Name = "SA_inter_relocate_baseline"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "SA_inter_exchange_baseline"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "exchange"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "SA_intra_2opt_baseline"
            Algo = "sa"
            NeighborhoodFamily = "intra"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "TABU_inter_relocate_baseline"
            Algo = "tabu"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "TABU_inter_exchange_baseline"
            Algo = "tabu"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "exchange"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "TABU_intra_2opt_baseline"
            Algo = "tabu"
            NeighborhoodFamily = "intra"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
    }

    if ($CampaignName -eq "tuning" -or $CampaignName -eq "full") {
        # SA tuning around best initialTemp/cooling candidates.
        $scenarios += [pscustomobject]@{
            Name = "SA_tuning_T1000_C09995"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1000.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "SA_tuning_T1250_C09993"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9993
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "SA_tuning_T1250_C09995"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "SA_tuning_T1250_C09997"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1250.0
            CoolingRate = 0.9997
            TabuTenure = 50
        }
        $scenarios += [pscustomobject]@{
            Name = "SA_tuning_T1400_C09995"
            Algo = "sa"
            NeighborhoodFamily = "inter"
            InterNeighborhoodType = "relocate"
            IntraNeighborhoodType = "2opt"
            InitialTemp = 1400.0
            CoolingRate = 0.9995
            TabuTenure = 50
        }

        # Tabu tuning around best tenure candidates.
        foreach ($tenure in @(40, 50, 60, 70)) {
            $scenarios += [pscustomobject]@{
                Name = "TABU_tuning_tenure_$tenure"
                Algo = "tabu"
                NeighborhoodFamily = "inter"
                InterNeighborhoodType = "relocate"
                IntraNeighborhoodType = "2opt"
                InitialTemp = 1250.0
                CoolingRate = 0.9995
                TabuTenure = $tenure
            }
        }
    }

    return $scenarios
}

function Invoke-VrptwInteractiveRun {
    param(
        [string]$InstancePath,
        [string]$Algo,
        [int]$RunIterations,
        [long]$Seed,
        [double]$RunPenaltyWeight,
        [string]$RunEstimateMinVehicles,
        [int]$RunMaxVehicles,
        [string]$EnforceTimeWindows,
        [string]$NeighborhoodFamily,
        [string]$InterNeighborhoodType,
        [string]$IntraNeighborhoodType,
        [double]$InitialTemp,
        [double]$CoolingRate,
        [int]$TabuTenure,
        [string]$RunClassPath
    )

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
        $NeighborhoodFamily
    )

    if ($NeighborhoodFamily -eq "inter") {
        $answers += @($InterNeighborhoodType)
    } else {
        $answers += @($IntraNeighborhoodType)
    }

    if ($Algo -eq "sa") {
        $answers += @("$InitialTemp", "$CoolingRate")
    }

    if ($Algo -eq "tabu") {
        $answers += @("$TabuTenure")
    }

    $stdinPayload = ($answers -join "`r`n") + "`r`n"
    $stdinPayload | & java -cp $RunClassPath vrptw.Main
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed for instance=$InstancePath algo=$Algo seed=$Seed"
    }
}

if (-not (Test-Path (Join-Path $ClassPath "vrptw/Main.class"))) {
    throw "Cannot find compiled classes in '$ClassPath'. Compile first (example: javac --release 21 -d bin src/vrptw/*.java)."
}

$instances = if ($Scale -eq "quick") {
    @("data/data101.vrp", "data/data111.vrp", "data/data201.vrp")
} else {
    @(
        "data/data101.vrp",
        "data/data102.vrp",
        "data/data111.vrp",
        "data/data112.vrp",
        "data/data201.vrp",
        "data/data202.vrp"
    )
}

$seeds = if ($Scale -eq "quick") {
    @(41, 42, 43, 44, 45)
} else {
    @(41, 42, 43, 44, 45, 46, 47, 48, 49, 50)
}

$twModes = @("non", "oui")
$scenarios = Get-ScenarioSet -CampaignName $Campaign

if ($scenarios.Count -eq 0) {
    throw "No scenarios defined for campaign '$Campaign'."
}

$campaignStamp = Get-Date -Format "yyyyMMdd_HHmmss"
$planPath = "campaign2_plan_$campaignStamp.csv"
$progressPath = "campaign2_progress_$campaignStamp.csv"

$planRows = @()
foreach ($instancePath in $instances) {
    foreach ($tw in $twModes) {
        foreach ($seed in $seeds) {
            foreach ($scenario in $scenarios) {
                $planRows += [pscustomobject]@{
                    instance = $instancePath
                    enforce_time_windows = $tw
                    seed = $seed
                    scenario = $scenario.Name
                    algo = $scenario.Algo
                    neighborhood_family = $scenario.NeighborhoodFamily
                    inter_type = $scenario.InterNeighborhoodType
                    intra_type = $scenario.IntraNeighborhoodType
                    initial_temp = $scenario.InitialTemp
                    cooling_rate = $scenario.CoolingRate
                    tabu_tenure = $scenario.TabuTenure
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

Write-Host "Campaign: $Campaign | Scale: $Scale"
Write-Host "Instances: $($instances.Count) | Seeds: $($seeds.Count) | TW modes: $($twModes.Count) | Scenarios: $($scenarios.Count)"
Write-Host "Total runs planned: $total"
Write-Host "Plan file: $planPath"
Write-Host "Progress file: $progressPath"

foreach ($row in $planRows) {
    $index++
    Write-Host "[$index/$total] $($row.algo) | $($row.instance) | TW=$($row.enforce_time_windows) | seed=$($row.seed) | $($row.scenario)"

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
            NeighborhoodFamily = $row.neighborhood_family
            InterNeighborhoodType = $row.inter_type
            IntraNeighborhoodType = $row.intra_type
            InitialTemp = [double]$row.initial_temp
            CoolingRate = [double]$row.cooling_rate
            TabuTenure = [int]$row.tabu_tenure
            RunClassPath = $ClassPath
        }
        Invoke-VrptwInteractiveRun @invokeParams

        [pscustomobject]@{
            timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            status = "ok"
            index = $index
            total = $total
            instance = $row.instance
            enforce_time_windows = $row.enforce_time_windows
            seed = $row.seed
            scenario = $row.scenario
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
            enforce_time_windows = $row.enforce_time_windows
            seed = $row.seed
            scenario = $row.scenario
            algo = $row.algo
            message = $_.Exception.Message
        } | Export-Csv -Path $progressPath -NoTypeInformation -Append
        throw
    }
}

Write-Host "Campaign completed."
Write-Host "Plan: $planPath"
Write-Host "Progress: $progressPath"
