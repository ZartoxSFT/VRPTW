param(
    [string]$InstancePath = "data/data101.vrp",
    [int]$Iterations = 30000,
    [long]$Seed = 42,
    [double]$PenaltyWeight = 1000.0,
    [double]$CoolingRate = 0.9995,
    [ValidateSet("inter", "intra")]
    [string]$NeighborhoodFamily = "inter",
    [ValidateSet("relocate", "exchange")]
    [string]$InterNeighborhoodType = "relocate",
    [ValidateSet("2opt")]
    [string]$IntraNeighborhoodType = "2opt",
    [string]$EstimateMinVehicles = "oui",
    [string]$EnforceTimeWindows = "oui",
    [int]$MaxVehicles = 2147483647,
    [string]$ClassPath = "bin"
)

$ErrorActionPreference = "Stop"

function Invoke-VrptwInteractiveRun {
    param(
        [ValidateSet("sa", "tabu")]
        [string]$Algo,
        [double]$InitialTemp,
        [int]$TabuTenure
    )

    $answers = @(
        "non",                  # Reinitialiser les parametres par defaut ?
        $InstancePath,           # Fichier d'instance
        $Algo,                   # Algo
        "$Iterations",          # Iterations
        "$Seed",                # Seed
        "$PenaltyWeight",       # Penalty
        $EstimateMinVehicles,    # Estimer min vehicules
        "$MaxVehicles",         # Max vehicles
        $EnforceTimeWindows,     # Appliquer TW
        $NeighborhoodFamily      # Famille de voisinage
    )

    if ($NeighborhoodFamily -eq "inter") {
        $answers += @(
            $InterNeighborhoodType  # Voisinage inter-groupe
        )
    } else {
        $answers += @(
            $IntraNeighborhoodType   # Voisinage intra-groupe
        )
    }

    if ($Algo -eq "sa") {
        $answers += @(
            "$InitialTemp",     # SA initial temp
            "$CoolingRate"      # SA cooling rate
        )
    }

    if ($Algo -eq "tabu") {
        $answers += @(
            "$TabuTenure"       # Tabu tenure
        )
    }

    $stdinPayload = ($answers -join "`r`n") + "`r`n"
    $stdinPayload | & java -cp $ClassPath vrptw.Main
    if ($LASTEXITCODE -ne 0) {
        throw "Run failed for algo=$Algo"
    }
}

if (-not (Test-Path (Join-Path $ClassPath "vrptw/Main.class"))) {
    throw "Cannot find compiled classes in '$ClassPath'. Compile first (example: javac -d bin src/vrptw/*.java)."
}

$saTemperatures = @(500, 750, 1000, 1250, 1500)
$tabuTenures = @(10, 20, 30, 40, 50)

Write-Host "=== SA sweep ==="
foreach ($temp in $saTemperatures) {
    Write-Host "[SA] initialTemp=$temp"
    Invoke-VrptwInteractiveRun -Algo "sa" -InitialTemp $temp -TabuTenure 0
}

Write-Host ""
Write-Host "=== TABU sweep ==="
foreach ($tenure in $tabuTenures) {
    Write-Host "[TABU] tabuTenure=$tenure"
    Invoke-VrptwInteractiveRun -Algo "tabu" -InitialTemp 0 -TabuTenure $tenure
}

Write-Host ""
Write-Host "All sweeps completed."