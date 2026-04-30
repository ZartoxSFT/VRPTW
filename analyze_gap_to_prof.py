import pandas as pd
from pathlib import Path
import re

# Charger tous les résultats
all_runs = []
for exp_dir in sorted(Path('resultsSA').glob('Exp*')):
    log_file = exp_dir / 'executions_log.csv'
    if log_file.exists():
        try:
            df = pd.read_csv(log_file)
            df = df[df['instance'] == 'data101.vrp'].copy()
            if len(df) > 0:
                all_runs.append(df)
        except:
            pass

df = pd.concat(all_runs, ignore_index=True)

# Focus sur penalty >= 5000 (réalistes)
df_realistic = df[df['penalty_weight'] >= 5000].copy()

print("\n" + "="*100)
print("ANALYSE APPROFONDIE: PENALTY >= 5000 (configs réalistes)")
print("="*100)

print(f"\nTotal runs: {len(df_realistic)}")
print(f"Distance min: {df_realistic['best_distance'].min():.2f} km")
print(f"Distance mean: {df_realistic['best_distance'].mean():.2f} km")
print(f"Distance std: {df_realistic['best_distance'].std():.2f} km")
print(f"Véhicules moyen: {df_realistic['routes'].mean():.1f}")

# Extraire les paramètres
def extract_params(params_str):
    try:
        temp = re.search(r'initialTemp=([\d.]+)', params_str).group(1)
        cooling = re.search(r'coolingRate=([\d.]+)', params_str).group(1)
        iters = re.search(r'iterations=(\d+)', params_str).group(1)
        return float(temp), float(cooling), int(iters)
    except:
        return None, None, None

df_realistic['temp'] = df_realistic['parameters'].apply(lambda x: extract_params(x)[0])
df_realistic['cooling'] = df_realistic['parameters'].apply(lambda x: extract_params(x)[1])
df_realistic['iterations'] = df_realistic['parameters'].apply(lambda x: extract_params(x)[2])

# TOP 30 solutions
print("\n" + "="*100)
print("TOP 30 SOLUTIONS (penalty >= 5k)")
print("="*100)
print("\nRank │ Distance │ Veh │ Penalty │ Temp │ Cool  │ Iter")
print("─"*100)

top30 = df_realistic.nsmallest(30, 'best_distance')
for i, (idx, row) in enumerate(top30.iterrows(), 1):
    print(f"{i:3d}  │ {row['best_distance']:7.2f} km │ {row['routes']:2.0f} │ {row['penalty_weight']:>7.0f} │ {row['temp']:5.0f} │ {row['cooling']:.4f} │ {row['iterations']/1000:5.0f}k")

# Analyse par température pour penalty >= 5k
print("\n" + "="*100)
print("SENSIBILITÉ TEMPÉRATURE (penalty >= 5k)")
print("="*100)

by_temp = df_realistic.groupby('temp')['best_distance'].agg(['min', 'mean', 'count']).sort_values('min')
print("\nTemp │  MIN (km) │ MEAN (km) │ Runs")
print("─"*50)
for temp, row in by_temp.iterrows():
    print(f"{temp:5.0f} │ {row['min']:9.2f} │ {row['mean']:9.2f} │ {row['count']:3.0f}")

# Analyse par cooling pour penalty >= 5k  
print("\n" + "="*100)
print("SENSIBILITÉ COOLING RATE (penalty >= 5k)")
print("="*100)

by_cooling = df_realistic.groupby('cooling')['best_distance'].agg(['min', 'mean', 'count']).sort_values('min')
print("\nCooling │  MIN (km) │ MEAN (km) │ Runs")
print("─"*50)
for cooling, row in by_cooling.iterrows():
    print(f"{cooling:.4f} │ {row['min']:9.2f} │ {row['mean']:9.2f} │ {row['count']:3.0f}")

# Interaction Temperature x Cooling pour penalty >= 5k
print("\n" + "="*100)
print("INTERACTION: TEMPERATURE × COOLING (penalty >= 5k, min distance)")
print("="*100)

interaction = df_realistic.pivot_table(values='best_distance', index='temp', columns='cooling', aggfunc='min')
print("\n" + interaction.round(2).to_string())

# Meilleure config par penalty
print("\n" + "="*100)
print("MEILLEURE CONFIG PAR PENALTY")
print("="*100)

for penalty in sorted(df_realistic['penalty_weight'].unique()):
    subset = df_realistic[df_realistic['penalty_weight'] == penalty]
    best = subset.loc[subset['best_distance'].idxmin()]
    print(f"\nPenalty {penalty:>8.0f}: {best['best_distance']:7.2f} km")
    print(f"  → Temp={best['temp']:5.0f}, Cool={best['cooling']:.4f}, Iter={best['iterations']:.0f}, Routes={best['routes']:.0f}")

# Comparaison vs Prof
prof_ref = 1650.80
best_realistic = df_realistic['best_distance'].min()
gap = best_realistic - prof_ref

print("\n" + "="*100)
print("VERDICT")
print("="*100)
print(f"\nMeilleur trouvé (penalty >= 5k): {best_realistic:.2f} km")
print(f"Référence prof:                   {prof_ref:.2f} km")
print(f"Écart:                            {gap:+.2f} km ({100*gap/prof_ref:+.1f}%)")

if gap < 100:
    print("\n✓ À distance raisonnable du prof (+6% maximum)")
else:
    print(f"\n⚠ Gap de {gap:.0f} km - chercher d'autres approches")

# Suggestion d'amélioration
print("\n" + "="*100)
print("OBSERVATIONS ET SUGGESTIONS")
print("="*100)

# Vérifier si plateau total
unique_best = df_realistic['best_distance'].min()
count_at_best = len(df_realistic[df_realistic['best_distance'] == unique_best])
print(f"\n1. Solutions identiques trouvées: {count_at_best} runs au même {unique_best:.2f} km")
print("   → Plateau STABLE atteint (pas de variation)")

# Vérifier si temperature extrême aide
hot_runs = df_realistic[df_realistic['temp'] >= 1500]
cold_runs = df_realistic[df_realistic['temp'] <= 750]
if len(hot_runs) > 0:
    print(f"\n2. Températures élevées (≥1500): min = {hot_runs['best_distance'].min():.2f} km")
if len(cold_runs) > 0:
    print(f"   Températures basses (≤750):    min = {cold_runs['best_distance'].min():.2f} km")

# Vérifier iterations
print(f"\n3. Itérations testées pour penalty >= 5k: {sorted(df_realistic['iterations'].unique())}")
print(f"   → Pas d'itérations > 100k testées pour penalty >= 5k")

print("\n" + "="*100)
