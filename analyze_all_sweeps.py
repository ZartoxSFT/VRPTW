#!/usr/bin/env python3
import pandas as pd
from pathlib import Path
import numpy as np
import re

# Collecte tous les logs d'exécution
print("=" * 130)
print("COLLECTION DES RÉSULTATS DU SWEEP")
print("=" * 130)

all_runs = []
exp_dirs = list(Path('resultsSA').glob('Exp*'))
print(f"\nTraitement de {len(exp_dirs)} expériences...")

for exp_dir in sorted(exp_dirs):
    log_file = exp_dir / 'executions_log.csv'
    if log_file.exists():
        try:
            df = pd.read_csv(log_file)
            # Filter: data101.vrp only
            df = df[df['instance'] == 'data101.vrp'].copy()
            if len(df) > 0:
                all_runs.append(df)
        except Exception as e:
            print(f"Erreur lecture {exp_dir}: {e}")

if not all_runs:
    print("ERREUR: Aucun résultat trouvé!")
    exit(1)

df = pd.concat(all_runs, ignore_index=True)
print(f"✓ {len(df)} runs chargés")

# Parse parameters from parameters column
def extract_param(row, param_name):
    try:
        match = re.search(rf'{param_name}=([^;]+)', row['parameters'])
        if match:
            val = match.group(1)
            if param_name == 'initialTemp':
                return float(val)
            elif param_name == 'coolingRate':
                return float(val)
            elif param_name in ['iterations', 'seed']:
                return int(float(val))
        return None
    except:
        return None

df['iterations'] = df.apply(lambda row: extract_param(row, 'iterations'), axis=1)
df['temp'] = df.apply(lambda row: extract_param(row, 'initialTemp'), axis=1)
df['cooling'] = df.apply(lambda row: extract_param(row, 'coolingRate'), axis=1)
df['seed'] = df.apply(lambda row: extract_param(row, 'seed'), axis=1)
df['penalty'] = df['penalty_weight'].astype(int)

# Filter: only sweep runs (with all parameters extracted)
df_sweep = df[df[['penalty', 'temp', 'cooling', 'iterations']].notna().all(axis=1)].copy()
print(f"✓ {len(df_sweep)} runs de sweep identifiés")

if len(df_sweep) == 0:
    print("ERREUR: Aucun run de sweep trouvé!")
    exit(1)

print("\n" + "=" * 130)
print("RÉSUMÉ DES RÉSULTATS")
print("=" * 130)

print(f"\nDistance: min={df_sweep['best_distance'].min():.2f}, max={df_sweep['best_distance'].max():.2f}, mean={df_sweep['best_distance'].mean():.2f}")
print(f"Véhicules: min={df_sweep['routes'].min():.0f}, max={df_sweep['routes'].max():.0f}, mean={df_sweep['routes'].mean():.1f}")
print(f"Penalties testées: {sorted(df_sweep['penalty'].unique())}")
print(f"Temperatures testées: {sorted(df_sweep['temp'].unique())}")
print(f"CoolingRates testées: {sorted(df_sweep['cooling'].unique())}")
print(f"Iterations testées: {sorted(df_sweep['iterations'].unique())}")

# GLOBAL BEST
print("\n" + "=" * 130)
print("🏆 MEILLEUR RÉSULTAT GLOBAL")
print("=" * 130)

best_idx = df_sweep['best_distance'].idxmin()
best = df_sweep.loc[best_idx]

print(f"\nDistance:       {best['best_distance']:.2f} km")
print(f"Véhicules:      {best['routes']:.0f}")
print(f"Penalty:        {best['penalty']:,.0f}")
print(f"InitialTemp:    {best['temp']}")
print(f"CoolingRate:    {best['cooling']}")
print(f"Iterations:     {best['iterations']:.0f}")
print(f"Seed:           {best['seed']:.0f}")
print(f"Runtime:        {best['runtime_ms']:.0f} ms")

prof_ref = 1650.80
diff = best['best_distance'] - prof_ref
pct_diff = 100 * diff / prof_ref

print(f"\nComparaison avec prof (1650.80 km):")
print(f"  Différence:   {diff:+.2f} km ({pct_diff:+.1f}%)")

if best['best_distance'] <= 1700:
    print(f"  ✓ EXCELLENT! Très proche de l'optimum")
elif best['best_distance'] <= 1750:
    print(f"  ~ BON - Acceptable")
else:
    print(f"  ⚠ À améliorer")

# TOP 10
print("\n" + "=" * 130)
print("🥇 TOP 10 DES MEILLEURES SOLUTIONS")
print("=" * 130)

top10 = df_sweep.nsmallest(10, 'best_distance')[['best_distance', 'routes', 'penalty', 'temp', 'cooling', 'iterations']]
print("\n")
for idx, (i, row) in enumerate(top10.iterrows(), 1):
    dist_diff = row['best_distance'] - prof_ref
    print(f"{idx:2d}. {row['best_distance']:7.2f} km (Δ{dist_diff:+6.2f}) | {row['routes']:2.0f} routes | penalty={row['penalty']:>8,.0f} | T={row['temp']:>4.0f} | cool={row['cooling']} | iter={row['iterations']:.0f}")

# ANALYSE PAR ITÉRATIONS (PRIMARY QUESTION)
print("\n" + "=" * 130)
print("📊 ANALYSE PAR NOMBRE D'ITÉRATIONS ⭐ (QUESTION CLÉ)")
print("=" * 130)

by_iter = df_sweep.groupby('iterations').agg({
    'best_distance': ['min', 'mean', 'max', 'std', 'count'],
    'routes': ['mean']
}).round(2)

print("\n")
for iter_val in sorted(df_sweep['iterations'].unique()):
    subset = df_sweep[df_sweep['iterations'] == iter_val]
    min_dist = subset['best_distance'].min()
    mean_dist = subset['best_distance'].mean()
    max_dist = subset['best_distance'].max()
    std_dist = subset['best_distance'].std()
    count = len(subset)
    
    improvement = "─"
    if iter_val == 50000 and 100000 in df_sweep['iterations'].values:
        next_iter_min = df_sweep[df_sweep['iterations'] == 100000]['best_distance'].min()
        improvement = f"{next_iter_min - min_dist:+.2f} km"
    elif iter_val == 100000 and 50000 in df_sweep['iterations'].values:
        prev_iter_min = df_sweep[df_sweep['iterations'] == 50000]['best_distance'].min()
        improvement = f"{min_dist - prev_iter_min:+.2f} km"
    
    print(f"{int(iter_val):>7d} iter: MIN={min_dist:7.2f} km, MEAN={mean_dist:7.2f} km, STDEV={std_dist:6.2f}, runs={count:3d}, Δ vs prev={improvement}")

# Check convergence: does doubling iterations help?
if 50000 in df_sweep['iterations'].values and 100000 in df_sweep['iterations'].values:
    min_50k = df_sweep[df_sweep['iterations'] == 50000]['best_distance'].min()
    min_100k = df_sweep[df_sweep['iterations'] == 100000]['best_distance'].min()
    improvement_100k = min_50k - min_100k
    
    print(f"\n✓ Comparaison 50k vs 100k:")
    print(f"  50k  iterations: best = {min_50k:.2f} km")
    print(f"  100k iterations: best = {min_100k:.2f} km")
    print(f"  Amélioration:    {improvement_100k:+.2f} km")
    
    if abs(improvement_100k) < 10:
        print(f"  → PLATEAU ATTEINT: doubler les itérations n'améliore que {improvement_100k:.2f} km")
        print(f"  → 50k itérations suffisent pour convergence")
    else:
        print(f"  → AMÉLIORATION SIGNIFICATIVE: continuer à augmenter les itérations")

# SENSITIVITY BY PENALTY
print("\n" + "=" * 130)
print("📊 SENSIBILITÉ: IMPACT DE PENALTY WEIGHT")
print("=" * 130)

print("\n")
for penalty in sorted(df_sweep['penalty'].unique()):
    subset = df_sweep[df_sweep['penalty'] == penalty]
    min_dist = subset['best_distance'].min()
    mean_dist = subset['best_distance'].mean()
    count = len(subset)
    
    print(f"Penalty {penalty:>8,.0f}: MIN={min_dist:7.2f} km, MEAN={mean_dist:7.2f} km, ({count:3d} runs)")

# Best configuration by penalty
print("\n" + "=" * 130)
print("📊 MEILLEURE CONFIG PAR PENALTY WEIGHT")
print("=" * 130)

print("\n")
for penalty in sorted(df_sweep['penalty'].unique()):
    subset = df_sweep[df_sweep['penalty'] == penalty]
    best_in_penalty = subset.loc[subset['best_distance'].idxmin()]
    
    print(f"Penalty {penalty:>8,.0f}: {best_in_penalty['best_distance']:7.2f} km | T={best_in_penalty['temp']:>4.0f} cool={best_in_penalty['cooling']} iter={best_in_penalty['iterations']:.0f}")

# INTERACTION PENALTY x ITERATIONS
print("\n" + "=" * 130)
print("📊 INTERACTION: PENALTY × ITÉRATIONS")
print("=" * 130)

interaction = df_sweep.pivot_table(
    values='best_distance', 
    index='penalty', 
    columns='iterations', 
    aggfunc='min'
).round(2)

print("\n")
print(interaction)

# RECOMMANDATIONS
print("\n" + "=" * 130)
print("💡 RECOMMANDATIONS FINALES")
print("=" * 130)

best_penalty = best['penalty']
best_temp = best['temp']
best_cooling = best['cooling']
best_iter = best['iterations']

print(f"\n✓ Paramètres OPTIMAUX trouvés:")
print(f"  PenaltyWeight   = {int(best_penalty):,}")
print(f"  InitialTemp     = {int(best_temp)}")
print(f"  CoolingRate     = {best_cooling}")
print(f"  Iterations      = {int(best_iter)}")
print(f"\nRésultat attendu: {best['best_distance']:.2f} km avec {best['routes']:.0f} véhicules")

# Verdict sur le nombre d'itérations
print(f"\n📌 CONCERNANT LE NOMBRE D'ITÉRATIONS:")
if 50000 in df_sweep['iterations'].values and 100000 in df_sweep['iterations'].values:
    min_50k = df_sweep[df_sweep['iterations'] == 50000]['best_distance'].min()
    min_100k = df_sweep[df_sweep['iterations'] == 100000]['best_distance'].min()
    delta = min_50k - min_100k
    
    if abs(delta) < 5:
        print(f"  → **50,000 itérations suffisent** (amélioration de seulement {delta:.2f} km avec 100k)")
    else:
        print(f"  → Recommandé: {int(best_iter):,} itérations (amélioration de {delta:.2f} km)")
else:
    print(f"  → Recommandé: {int(best_iter):,} itérations")

print("\n" + "=" * 130)
