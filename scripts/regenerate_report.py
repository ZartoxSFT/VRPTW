import pandas as pd
from pathlib import Path

f = list(Path('.').glob('campaign3_consolidated_*.csv'))[0]
df = pd.read_csv(f)
df_tw = df[df['enforce_time_windows'] == True].copy()

report = """# CAMPAIGN 3 ANALYSIS REPORT - Final Results (WITH TIME WINDOWS ONLY)

**Date:** April 28, 2026  
**Total Runs (WITH TW):** """ + str(len(df_tw)) + """ runs (out of 186 total)  
**Instances:** data101, data111, data201  
**Focus:** Time-windowed vehicle routing (realistic constraints)

---

## Executive Summary

Campagne 3 a généré **""" + str(len(df_tw)) + """ runs avec fenêtres temporelles** sur 3 instances VRPTW réalistes. Les résultats montrent que :

- **TABU surpasse SA** en qualité moyenne
- **data111 est plus facile** que data101  
- **Convergence:** SA s'améliore avec + d'itérations (100k meilleur); TABU converge tôt (10k acceptable)
- **Optimum trouvé:** 1136.01 km (SA, data111, 14 routes)

---

## 1. RÉSULTATS GLOBAUX (AVEC FENÊTRES DE TEMPS)

### Statistiques Globales

"""

# Global stats
for algo in ['SA', 'TABU']:
    sub = df_tw[df_tw['algorithm'] == algo]
    report += f"\n**{algo}**\n"
    report += f"- Meilleure distance: {sub['best_distance'].min():.2f} km\n"
    report += f"- Distance moyenne: {sub['best_distance'].mean():.2f} km (±{sub['best_distance'].std():.2f})\n"
    report += f"- Pire distance: {sub['best_distance'].max():.2f} km\n"
    report += f"- Nombre de runs: {len(sub)}\n"

report += "\n---\n\n## 2. PERFORMANCE PAR INSTANCE (AVEC TW)\n\n"

# Per-instance analysis
for inst in sorted(df_tw['instance'].unique()):
    report += f"\n### {inst}\n\n"
    inst_data = df_tw[df_tw['instance'] == inst]
    for algo in ['SA', 'TABU']:
        sub = inst_data[inst_data['algorithm'] == algo]
        if not sub.empty:
            best_idx = sub['best_distance'].idxmin()
            best_row = sub.loc[best_idx]
            report += f"**{algo}**\n"
            report += f"- Meilleure distance: {best_row['best_distance']:.2f} km\n"
            report += f"- Nombre de routes: {best_row['routes']:.0f}\n"
            report += f"- Distance moyenne: {sub['best_distance'].mean():.2f} km (±{sub['best_distance'].std():.2f})\n"
            report += f"- Seed: {best_row['parameters'].split('seed=')[1].split(';')[0] if 'seed=' in best_row['parameters'] else 'N/A'}\n"
            report += f"- Runtime (best): {best_row['runtime_ms']:.0f} ms\n\n"

report += "\n---\n\n## 3. MEILLEURE SOLUTION GLOBALE (AVEC TW)\n\n"

# Global best
for algo in ['SA', 'TABU']:
    sub = df_tw[df_tw['algorithm'] == algo]
    best_idx = sub['best_distance'].idxmin()
    best_row = sub.loc[best_idx]
    report += f"### {algo}\n\n"
    report += f"```\n"
    report += f"Instance:         {best_row['instance']}\n"
    report += f"Distance:         {best_row['best_distance']:.2f} km\n"
    report += f"Routes:           {best_row['routes']:.0f}\n"
    report += f"Runtime:          {best_row['runtime_ms']:.0f} ms\n"
    report += f"Seed:             {best_row['parameters'].split('seed=')[1].split(';')[0] if 'seed=' in best_row['parameters'] else 'N/A'}\n"
    report += f"```\n\n"

# Convergence
report += "\n---\n\n## 4. CONVERGENCE AVEC ITÉRATIONS\n\n"

df_tw['iterations'] = df_tw['parameters'].str.extract(r'iterations=(\d+)').astype(float)
conv_data = df_tw.groupby(['iterations', 'algorithm'])['best_distance'].agg(['mean', 'min', 'count']).round(2)

report += "| Itérations | SA Moyenne | SA Min | TABU Moyenne | TABU Min |\n"
report += "|-----------|-----------|--------|-------------|----------|\n"

for it in sorted(df_tw['iterations'].unique()):
    sa_row = df_tw[(df_tw['iterations'] == it) & (df_tw['algorithm'] == 'SA')]
    tabu_row = df_tw[(df_tw['iterations'] == it) & (df_tw['algorithm'] == 'TABU')]
    
    sa_mean = sa_row['best_distance'].mean() if len(sa_row) > 0 else None
    sa_min = sa_row['best_distance'].min() if len(sa_row) > 0 else None
    tabu_mean = tabu_row['best_distance'].mean() if len(tabu_row) > 0 else None
    tabu_min = tabu_row['best_distance'].min() if len(tabu_row) > 0 else None
    
    report += f"| {int(it):,} | "
    report += f"{sa_mean:.2f} | {sa_min:.2f} | " if sa_mean else "— | — | "
    report += f"{tabu_mean:.2f} | {tabu_min:.2f} |\n" if tabu_mean else "— | — |\n"

report += "\n✓ **SA bénéficie d'itérations longues** (convergence progressive)\n"
report += "✓ **TABU converge vite** (10k-30k suffisent)\n"

report += """

---

## 5. RECOMMANDATIONS

### Pour Amélioration
1. **Augmenter pénalité véhicule:** `penalty_weight = 100000` (vs 1000) pour minimiser agressivement
2. **Forcer véhicules:** `MaxVehicles = 19` pour reproduire solution prof
3. **Recherche bi-étape:** (1) min véhicules, (2) min distance à véhicules fixés

### Paramètres Confirmés
- **SA:** 100k itérations, T=1250, cooling=0.9993, inter_relocate
- **TABU:** 30k itérations, tenure=40, inter_relocate

---

## DONNÉES EXTRAITES

✓ 106 runs WITH time windows  
✓ 3 instances (data101, data111, data201)  
✓ 2 algorithmes (SA, TABU)  
✓ Convergence analysis (10k, 30k, 100k itérations)
"""

# Write report
with open('ANALYSIS_REPORT_CAMPAIGN3.md', 'w', encoding='utf-8') as out:
    out.write(report)

print("OK Rapport regenere: ANALYSIS_REPORT_CAMPAIGN3.md")
print(f"OK Runs WITH TW: {len(df_tw)}")
