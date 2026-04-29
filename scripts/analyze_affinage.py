import pandas as pd
from pathlib import Path
import json

# Find affinage results
affinage_logs = list(Path('resultsSA').rglob('executions_log.csv')) + list(Path('resultTABU').rglob('executions_log.csv'))

runs = []
for log in affinage_logs:
    try:
        df = pd.read_csv(log)
        subset = df[(df['instance'] == 'data101.vrp') & 
                    (df['enforce_time_windows'] == True)]
        if len(subset) > 0:
            runs.append(subset)
    except:
        pass

if not runs:
    print("No affinage results found yet. Run run_affinage.ps1 first.")
    exit(1)

df_all = pd.concat(runs, ignore_index=True)
df_all['penalty'] = df_all['parameters'].str.extract(r'penaltyWeight=(\d+)').astype(float)
df_all['seed'] = df_all['parameters'].str.extract(r'seed=(\d+)').astype(int)
df_all['timestamp'] = pd.to_datetime(df_all['timestamp'], errors='coerce')

# Filter RECENT runs (last 2 hours) and WITH TW and 100k iterations
df_all['iterations'] = df_all['parameters'].str.extract(r'iterations=(\d+)').astype(int)
df_recent = df_all[(df_all['iterations'] == 100000) & 
                   (df_all['penalty'].notna())].copy()

if len(df_recent) == 0:
    print("⚠ No affinage runs with 100k iterations found")
    print(f"Available: {len(df_all)} total data101 WITH TW runs")
    exit(0)

print("="*100)
print("AFFINAGE RESULTS - data101.vrp WITH TW, 100k iterations")
print("="*100)
print(f"Runs analyzed: {len(df_recent)}\n")

# Summary by penalty and algo
summary = df_recent.groupby(['penalty', 'algorithm'])['best_distance'].agg(['count', 'min', 'mean', 'std', 'min']).round(2)
print("SUMMARY BY PENALTY WEIGHT & ALGORITHM:")
print(summary)
print()

# Rankings
print("="*100)
print("BEST SOLUTIONS BY PENALTY")
print("="*100)

for penalty in sorted(df_recent['penalty'].unique()):
    sub = df_recent[df_recent['penalty'] == penalty]
    best = sub.loc[sub['best_distance'].idxmin()]
    
    print(f"\nPenalty Weight = {penalty:,.0f}")
    print(f"  ├─ SA   min: {sub[sub['algorithm']=='SA']['best_distance'].min():.2f} km")
    print(f"  ├─ TABU min: {sub[sub['algorithm']=='TABU']['best_distance'].min():.2f} km")
    print(f"  ├─ Best algo: {best['algorithm']}")
    print(f"  ├─ Best dist: {best['best_distance']:.2f} km")
    print(f"  ├─ Routes:   {best['routes']:.0f}")
    print(f"  └─ Vs Prof (1650.8): {best['best_distance'] - 1650.8:+.2f} km ({100*(best['best_distance']-1650.8)/1650.8:+.1f}%)")

print("\n" + "="*100)
print("GLOBAL OPTIMUM")
print("="*100)

best_global = df_recent.loc[df_recent['best_distance'].idxmin()]
print(f"\nDistance:        {best_global['best_distance']:.2f} km")
print(f"Routes:          {best_global['routes']:.0f}")
print(f"Algorithm:       {best_global['algorithm']}")
print(f"Penalty weight:  {best_global['penalty']:,.0f}")
print(f"Seed:            {best_global['seed']:.0f}")
print(f"\nVs Prof (1650.80 km): {best_global['best_distance'] - 1650.80:+.2f} km ({100*(best_global['best_distance']-1650.80)/1650.80:+.1f}%)")

if best_global['best_distance'] <= 1700:
    print("✓ PERTINENT! Close to prof's solution")
elif best_global['best_distance'] <= 1750:
    print("~ ACCEPTABLE range")
else:
    print("✗ Still far from optimum")

print("\n" + "="*100)
print("RECOMMENDATION")
print("="*100)

best_penalty = df_recent.loc[df_recent['best_distance'].idxmin(), 'penalty']
best_algo = df_recent.loc[df_recent['best_distance'].idxmin(), 'algorithm']

if best_global['best_distance'] <= 1700:
    print(f"\n✓ Found good solution with penalty={best_penalty:,.0f}")
    print(f"  Use: {best_algo} with penalty_weight={best_penalty:,.0f}")
elif best_penalty == df_recent['penalty'].max():
    print(f"\n⚠ Best found at max penalty={best_penalty:,.0f}")
    print(f"  Try even higher penalty (200k-500k) or force MaxVehicles=19")
else:
    print(f"\n~ Current best at penalty={best_penalty:,.0f}")
    print(f"  Try higher penalties or different approach")
