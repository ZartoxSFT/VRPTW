#!/usr/bin/env python3
"""
Campaign 3 - Advanced Analysis
Extracts optimal parameters, best neighborhoods, and key insights for the report.
"""

import pandas as pd
import numpy as np
from pathlib import Path
import json
from datetime import datetime

print("=" * 90)
print("CAMPAIGN 3 - ADVANCED ANALYSIS FOR REPORT")
print("=" * 90)
print()

# Load consolidated data
consolidated_file = list(Path(".").glob("campaign3_consolidated_*.csv"))[0]
print(f"Loading consolidated data from: {consolidated_file.name}")
df = pd.read_csv(consolidated_file)

print(f"Total records: {len(df)}")
print()

# ============================================================================
# 1. NEIGHBORHOOD ANALYSIS
# ============================================================================

print("=" * 90)
print("1. NEIGHBORHOOD ANALYSIS")
print("=" * 90)
print()

# Extract neighborhood type from parameters column
def extract_neighborhood(params_str):
    if pd.isna(params_str):
        return "unknown"
    if "inter" in str(params_str).lower() and "relocate" in str(params_str).lower():
        return "inter_relocate"
    elif "inter" in str(params_str).lower() and "exchange" in str(params_str).lower():
        return "inter_exchange"
    elif "intra" in str(params_str).lower() or "2opt" in str(params_str).lower():
        return "intra_2opt"
    else:
        return "mixed/unknown"

df['neighborhood'] = df['parameters'].apply(extract_neighborhood)

print("Neighborhood types in data:")
print(df['neighborhood'].value_counts())
print()

# Analyze performance by neighborhood
print("Performance by neighborhood (average best_distance):")
neighborhood_perf = df.groupby(['neighborhood', 'algorithm']).agg({
    'best_distance': ['mean', 'std', 'min', 'max', 'count']
}).round(2)
print(neighborhood_perf)
print()

# Find best neighborhood per algorithm
for algo in ['SA', 'TABU']:
    algo_data = df[df['algorithm'] == algo]
    best_neighborhood = algo_data.groupby('neighborhood')['best_distance'].mean().idxmin()
    best_value = algo_data.groupby('neighborhood')['best_distance'].mean().min()
    print(f"{algo} - Best neighborhood: {best_neighborhood} (avg distance: {best_value:.2f})")
print()

# ============================================================================
# 2. OPTIMAL PARAMETERS PER ALGORITHM
# ============================================================================

print("=" * 90)
print("2. OPTIMAL PARAMETERS EXTRACTION")
print("=" * 90)
print()

# Parse parameters column to extract key values
def extract_params_dict(params_str):
    """Extract all parameters as dict from the parameters string"""
    if pd.isna(params_str):
        return {}
    params = {}
    try:
        items = str(params_str).split("; ")
        for item in items:
            if "=" in item:
                key, val = item.split("=", 1)
                params[key.strip()] = val.strip()
    except:
        pass
    return params

df['params_dict'] = df['parameters'].apply(extract_params_dict)

# Extract SA parameters
sa_df = df[df['algorithm'] == 'SA'].copy()
if len(sa_df) > 0:
    print("SA (Simulated Annealing) - Optimal Configuration:")
    best_sa = sa_df.loc[sa_df['best_distance'].idxmin()]
    print(f"  Best distance found: {best_sa['best_distance']:.2f}")
    print(f"  Instance: {best_sa['instance']}")
    print(f"  Enforce TW: {best_sa['enforce_time_windows']}")
    print(f"  Runtime: {best_sa['runtime_ms']:.0f} ms")
    print(f"  Feasible: {best_sa['time_violation'] == 0 and best_sa['capacity_violation'] == 0}")
    print(f"  Parameters: {best_sa['parameters']}")
    print()
    
    # SA statistics
    sa_stats = sa_df.groupby('enforce_time_windows').agg({
        'best_distance': ['mean', 'std', 'min', 'max'],
        'runtime_ms': 'mean',
        'solutions_evaluated': 'mean'
    }).round(2)
    print("SA Statistics by TW mode:")
    print(sa_stats)
    print()

# Extract Tabu parameters
tabu_df = df[df['algorithm'] == 'TABU'].copy()
if len(tabu_df) > 0:
    print("TABU (Tabu Search) - Optimal Configuration:")
    best_tabu = tabu_df.loc[tabu_df['best_distance'].idxmin()]
    print(f"  Best distance found: {best_tabu['best_distance']:.2f}")
    print(f"  Instance: {best_tabu['instance']}")
    print(f"  Enforce TW: {best_tabu['enforce_time_windows']}")
    print(f"  Runtime: {best_tabu['runtime_ms']:.0f} ms ({best_tabu['runtime_ms']/1000:.1f} sec)")
    print(f"  Feasible: {best_tabu['time_violation'] == 0 and best_tabu['capacity_violation'] == 0}")
    print(f"  Parameters: {best_tabu['parameters']}")
    print()
    
    # Tabu statistics
    tabu_stats = tabu_df.groupby('enforce_time_windows').agg({
        'best_distance': ['mean', 'std', 'min', 'max'],
        'runtime_ms': 'mean',
        'solutions_evaluated': 'mean'
    }).round(2)
    print("TABU Statistics by TW mode:")
    print(tabu_stats)
    print()

# ============================================================================
# 3. INSTANCE-BASED ANALYSIS
# ============================================================================

print("=" * 90)
print("3. INSTANCE-BASED ANALYSIS")
print("=" * 90)
print()

instances = df['instance'].unique()
for inst in sorted(instances):
    inst_data = df[df['instance'] == inst]
    print(f"Instance: {inst}")
    print(f"  Total runs: {len(inst_data)}")
    
    for algo in ['SA', 'TABU']:
        algo_data = inst_data[inst_data['algorithm'] == algo]
        if len(algo_data) > 0:
            best = algo_data['best_distance'].min()
            mean = algo_data['best_distance'].mean()
            std = algo_data['best_distance'].std()
            avg_time = algo_data['runtime_ms'].mean()
            print(f"  {algo}: best={best:.2f}, mean={mean:.2f}±{std:.2f}, avg_time={avg_time:.0f}ms")
    print()

# ============================================================================
# 4. TIME WINDOWS IMPACT
# ============================================================================

print("=" * 90)
print("4. TIME WINDOWS IMPACT ANALYSIS")
print("=" * 90)
print()

tw_impact = df.groupby(['algorithm', 'enforce_time_windows']).agg({
    'best_distance': ['mean', 'std', 'count'],
    'time_violation': 'mean',
    'capacity_violation': 'mean',
    'runtime_ms': 'mean'
}).round(2)
print("Performance with vs without Time Windows:")
print(tw_impact)
print()

# Calculate TW impact percentage
for algo in ['SA', 'TABU']:
    no_tw = df[(df['algorithm'] == algo) & (df['enforce_time_windows'] == False)]['best_distance'].mean()
    with_tw = df[(df['algorithm'] == algo) & (df['enforce_time_windows'] == True)]['best_distance'].mean()
    if no_tw > 0:
        impact = ((with_tw - no_tw) / no_tw) * 100
        print(f"{algo} - TW Impact: {impact:+.1f}% (without TW: {no_tw:.2f}, with TW: {with_tw:.2f})")
print()

# ============================================================================
# 5. FEASIBILITY ANALYSIS
# ============================================================================

print("=" * 90)
print("5. FEASIBILITY ANALYSIS")
print("=" * 90)
print()

df['is_feasible'] = (df['time_violation'] == 0) & (df['capacity_violation'] == 0)

feasibility = df.groupby(['algorithm', 'enforce_time_windows']).agg({
    'is_feasible': ['sum', 'count', 'mean']
}).round(3)
feasibility.columns = ['feasible_count', 'total', 'feasibility_rate']
print("Feasibility rates:")
print(feasibility)
print()

# ============================================================================
# 6. SUMMARY TABLE FOR REPORT
# ============================================================================

print("=" * 90)
print("6. SUMMARY TABLE FOR REPORT")
print("=" * 90)
print()

summary_table = []

for inst in sorted(instances):
    for tw_mode in [False, True]:
        tw_label = "with TW" if tw_mode else "no TW"
        inst_tw_data = df[(df['instance'] == inst) & (df['enforce_time_windows'] == tw_mode)]
        
        for algo in ['SA', 'TABU']:
            algo_data = inst_tw_data[inst_tw_data['algorithm'] == algo]
            if len(algo_data) > 0:
                summary_table.append({
                    'Instance': inst.split('/')[-1],
                    'Mode': tw_label,
                    'Algorithm': algo,
                    'Best Distance': f"{algo_data['best_distance'].min():.2f}",
                    'Avg Distance': f"{algo_data['best_distance'].mean():.2f} ± {algo_data['best_distance'].std():.2f}",
                    'Feasible %': f"{(algo_data['is_feasible'].mean() * 100):.1f}%",
                    'Avg Time (s)': f"{algo_data['runtime_ms'].mean() / 1000:.2f}",
                    'Runs': len(algo_data)
                })

summary_df = pd.DataFrame(summary_table)
print(summary_df.to_string(index=False))
print()

# Export summary table
summary_file = f"campaign3_report_summary_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
summary_df.to_csv(summary_file, index=False)
print(f"Summary table exported to: {summary_file}")
print()

# ============================================================================
# 7. KEY RECOMMENDATIONS
# ============================================================================

print("=" * 90)
print("7. KEY RECOMMENDATIONS FOR REPORT")
print("=" * 90)
print()

print("✓ BEST PERFORMING ALGORITHM:")
sa_mean = df[df['algorithm'] == 'SA']['best_distance'].mean()
tabu_mean = df[df['algorithm'] == 'TABU']['best_distance'].mean()
winner = "TABU" if tabu_mean < sa_mean else "SA"
improvement = abs((tabu_mean - sa_mean) / sa_mean * 100)
print(f"  {winner} wins with {improvement:.1f}% better quality on average")
print(f"  SA avg: {sa_mean:.2f}, TABU avg: {tabu_mean:.2f}")
print()

print("✓ BEST NEIGHBORHOOD CONFIGURATION:")
best_neighborhood = df.groupby('neighborhood')['best_distance'].mean().idxmin()
print(f"  {best_neighborhood} is optimal")
print()

print("✓ TIME WINDOWS CRITICAL FINDING:")
tw_impact_sa = ((df[(df['algorithm'] == 'SA') & (df['enforce_time_windows'] == True)]['best_distance'].mean() - 
                 df[(df['algorithm'] == 'SA') & (df['enforce_time_windows'] == False)]['best_distance'].mean()) / 
                df[(df['algorithm'] == 'SA') & (df['enforce_time_windows'] == False)]['best_distance'].mean() * 100)
tw_impact_tabu = ((df[(df['algorithm'] == 'TABU') & (df['enforce_time_windows'] == True)]['best_distance'].mean() - 
                   df[(df['algorithm'] == 'TABU') & (df['enforce_time_windows'] == False)]['best_distance'].mean()) / 
                  df[(df['algorithm'] == 'TABU') & (df['enforce_time_windows'] == False)]['best_distance'].mean() * 100)
print(f"  Time windows increase distance by:")
print(f"    SA: {tw_impact_sa:+.1f}%")
print(f"    TABU: {tw_impact_tabu:+.1f}%")
print()

print("✓ ROBUSTNESS ANALYSIS:")
sa_std = df[df['algorithm'] == 'SA']['best_distance'].std()
tabu_std = df[df['algorithm'] == 'TABU']['best_distance'].std()
more_stable = "SA" if sa_std < tabu_std else "TABU"
print(f"  {more_stable} is more stable (lower std deviation)")
print(f"  SA std: {sa_std:.2f}, TABU std: {tabu_std:.2f}")
print()

print("✓ EXECUTION TIME TRADEOFF:")
sa_time = df[df['algorithm'] == 'SA']['runtime_ms'].mean() / 1000
tabu_time = df[df['algorithm'] == 'TABU']['runtime_ms'].mean() / 1000
ratio = tabu_time / sa_time
print(f"  TABU is {ratio:.0f}x slower than SA")
print(f"  SA: {sa_time:.3f}s avg, TABU: {tabu_time:.1f}s avg")
print()

print("=" * 90)
print("ANALYSIS COMPLETE")
print("=" * 90)
print()
print("Files generated for report:")
print(f"  - {summary_file} (detailed summary table)")
print("  - campaign3_consolidated_*.csv (full raw data)")
print("  - campaign3_summary_*.csv (statistical summary)")
print("  - campaign3_comparison_sa_tabu_*.csv (direct comparison)")
print()
