import pandas as pd
from pathlib import Path
import numpy as np

# Find SA sweep results
sa_logs = list(Path('resultsSA').rglob('executions_log.csv'))

runs = []
for log in sa_logs:
    try:
        df = pd.read_csv(log)
        subset = df[(df['instance'] == 'data101.vrp') & 
                    (df['enforce_time_windows'] == True) &
                    (df['algorithm'] == 'SA')]
        if len(subset) > 0:
            runs.append(subset)
    except:
        pass

if not runs:
    print("No SA results found yet. Run run_sweep_sa.ps1 first.")
    exit(1)

df = pd.concat(runs, ignore_index=True)

# Extract parameters
df['penalty'] = df['parameters'].str.extract(r'penaltyWeight=(\d+)').astype(float)
df['temp'] = df['parameters'].str.extract(r'initialTemp=(\d+\.?\d*)').astype(float)
df['cooling'] = df['parameters'].str.extract(r'coolingRate=(0\.\d+)').astype(float)
df['seed'] = df['parameters'].str.extract(r'seed=(\d+)').astype(int)
df['iterations'] = df['parameters'].str.extract(r'iterations=(\d+)').astype(int)

# Filter: only rows with all parameters extracted (sweep runs)
df_sweep = df[df[['penalty', 'temp', 'cooling', 'iterations']].notna().all(axis=1)].copy()

if len(df_sweep) == 0:
    print("No complete sweep runs found")
    exit(0)

print("="*120)
print("SA PARAMETER SWEEP ANALYSIS - data101.vrp WITH TW")
print("="*120)
print(f"\nTotal runs analyzed: {len(df_sweep)}")
print(f"Unique penalties: {df_sweep['penalty'].nunique()}")
print(f"Unique temps: {df_sweep['temp'].nunique()}")
print(f"Unique coolings: {df_sweep['cooling'].nunique()}")
print(f"Unique iterations: {df_sweep['iterations'].nunique()}")

# Global best
best_idx = df_sweep['best_distance'].idxmin()
best = df_sweep.loc[best_idx]

print("\n" + "="*120)
print("GLOBAL OPTIMUM FOUND")
print("="*120)
print(f"\nDistance:       {best['best_distance']:.2f} km")
print(f"Routes:         {best['routes']:.0f}")
print(f"Penalty:        {best['penalty']:,.0f}")
print(f"InitialTemp:    {best['temp']}")
print(f"CoolingRate:    {best['cooling']}")
print(f"Iterations:     {best['iterations']:.0f}")
print(f"Seed:           {best['seed']:.0f}")
print(f"Runtime:        {best['runtime_ms']:.0f} ms")

prof_ref = 1650.80
diff = best['best_distance'] - prof_ref
pct_diff = 100 * diff / prof_ref

print(f"\nVs Prof (1650.80 km):")
print(f"  Difference:   {diff:+.2f} km ({pct_diff:+.1f}%)")

if best['best_distance'] <= 1700:
    print(f"  ✓ PERTINENT! Within 3% of optimum")
elif best['best_distance'] <= 1750:
    print(f"  ~ ACCEPTABLE range")
else:
    print(f"  ⚠ Still searching for better")

# Top 10 solutions
print("\n" + "="*120)
print("TOP 10 SOLUTIONS")
print("="*120)
top10 = df_sweep.nsmallest(10, 'best_distance')[['best_distance', 'routes', 'penalty', 'temp', 'cooling', 'iterations']]
for idx, (i, row) in enumerate(top10.iterrows(), 1):
    print(f"{idx:2d}. {row['best_distance']:7.2f} km | routes={row['routes']:2.0f} | penalty={row['penalty']:>8,.0f} | temp={row['temp']:>6} | cooling={row['cooling']} | iter={row['iterations']:.0f}")

# Analysis by parameter
print("\n" + "="*120)
print("SENSITIVITY ANALYSIS (Mean distance by parameter value)")
print("="*120)

print("\nBy Penalty Weight:")
by_penalty = df_sweep.groupby('penalty')['best_distance'].agg(['min', 'mean', 'count']).round(2)
print(by_penalty)

print("\nBy Initial Temperature:")
by_temp = df_sweep.groupby('temp')['best_distance'].agg(['min', 'mean', 'count']).round(2)
print(by_temp)

print("\nBy Cooling Rate:")
by_cooling = df_sweep.groupby('cooling')['best_distance'].agg(['min', 'mean', 'count']).round(2)
print(by_cooling)

print("\nBy Iterations:")
by_iter = df_sweep.groupby('iterations')['best_distance'].agg(['min', 'mean', 'count']).round(2)
print(by_iter)

# Interaction analysis: penalty + temp
print("\n" + "="*120)
print("PENALTY vs TEMPERATURE (min distance)")
print("="*120)
interaction = df_sweep.pivot_table(values='best_distance', index='penalty', columns='temp', aggfunc='min').round(2)
print(interaction)

# Best parameters
print("\n" + "="*120)
print("OPTIMAL PARAMETERS RECOMMENDATION")
print("="*120)

best_penalty = df_sweep.loc[df_sweep['best_distance'].idxmin(), 'penalty']
best_temp = df_sweep.loc[df_sweep['best_distance'].idxmin(), 'temp']
best_cooling = df_sweep.loc[df_sweep['best_distance'].idxmin(), 'cooling']
best_iter = df_sweep.loc[df_sweep['best_distance'].idxmin(), 'iterations']

print(f"\nUse for production:")
print(f"  -PenaltyWeight   {best_penalty:,.0f}")
print(f"  -InitialTemp     {best_temp}")
print(f"  -CoolingRate     {best_cooling}")
print(f"  -Iterations      {best_iter:.0f}")

# If not close to prof, suggest direction
if best['best_distance'] > 1700:
    print(f"\n⚠ Still {best['best_distance'] - prof_ref:.0f} km away from prof")
    
    # Check if penalty is maxed out
    if best_penalty == df_sweep['penalty'].max():
        print(f"  → Try even higher penalty (1M+)")
    else:
        print(f"  → Try higher penalty values")
    
    # Check if we need more iterations
    if best_iter == df_sweep['iterations'].max():
        print(f"  → Consider higher iterations (200k+)")
