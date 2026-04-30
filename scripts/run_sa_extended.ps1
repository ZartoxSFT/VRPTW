# Run SA with large number of iterations across all instances in data/
# Usage: Open PowerShell in repository root and run: .\scripts\run_sa_extended.ps1

$iterations = 200000
$seeds = @(66571993098, 15032385634, 42)
$initialTemp = 1250
$coolingRate = 0.9993
$penaltyWeight = 1000
$enforceTW = "oui"
$neighborhoodFamily = "inter"
$interType = "relocate"

New-Item -ItemType Directory -Path logs -Force | Out-Null

$instances = Get-ChildItem -Path data -Filter *.vrp
foreach ($inst in $instances) {
    $stem = [System.IO.Path]::GetFileNameWithoutExtension($inst.Name)
    foreach ($seed in $seeds) {
        Write-Host "Running SA on $($inst.Name) seed=$seed iterations=$iterations"
        $stdin = @"
non
$($inst.FullName)
sa
$iterations
$seed
$penaltyWeight
non

$enforceTW
$neighborhoodFamily
$interType
$initialTemp
$coolingRate
"@

        $logFile = "logs/sa_${stem}_${seed}.log"
        $proc = Start-Process -FilePath java -ArgumentList '-cp','bin','vrptw.Main' -NoNewWindow -RedirectStandardInput "-" -RedirectStandardOutput $logFile -RedirectStandardError $logFile -PassThru
        $proc.StandardInput.Write($stdin)
        $proc.StandardInput.Close()
        $proc.WaitForExit()
        Write-Host "Done -> $logFile"
    }
}

Write-Host "All SA jobs finished." 
