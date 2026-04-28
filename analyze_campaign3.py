#!/usr/bin/env python3
"""
Campaign 3 Results Analysis
Consolidates executions_log.csv files from SA and Tabu runs
Generates summary statistics and analysis tables.

Usage:
  python analyze_campaign3.py
"""

import pandas as pd
import numpy as np
import glob
import os
from pathlib import Path
from datetime import datetime

print("=" * 80)
print("CAMPAIGN 3 - RESULTS CONSOLIDATION AND ANALYSIS")
print("=" * 80)
print()

# ============================================================================
# 1. CONSOLIDATE ALL LOGS
# ============================================================================

print("STEP 1: Consolidating execution logs...")
print()

sa_logs = glob.glob("resultsSA/Exp*/executions_log.csv")
tabu_logs = glob.glob("resultTABU/Exp*/executions_log.csv")

print(f"  Found {len(sa_logs)} SA log files")
print(f"  Found {len(tabu_logs)} Tabu log files")

if len(sa_logs) == 0 or len(tabu_logs) == 0:
    print("ERROR: No execution logs found. Check resultsSA/ and resultTABU/ directories.")
    exit(1)

# Read and consolidate
sa_dfs = []
for f in sa_logs:
    try:
        df = pd.read_csv(f)
        df['algorithm'] = 'SA'
        sa_dfs.append(df)
    except Exception as e:
        print(f"  WARNING: Could not read {f}: {e}")

tabu_dfs = []
for f in tabu_logs:
    try:
        df = pd.read_csv(f)
        df['algorithm'] = 'TABU'
        tabu_dfs.append(df)
    except Exception as e:
        print(f"  WARNING: Could not read {f}: {e}")

if len(sa_dfs) == 0 or len(tabu_dfs) == 0:
    print("ERROR: Could not load any log files.")
    exit(1)

all_results = pd.concat(sa_dfs + tabu_dfs, ignore_index=True)
print(f"  Total rows consolidated: {len(all_results)}")
print()

# Save consolidated file
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
consolidated_file = f"campaign3_consolidated_{timestamp}.csv"
all_results.to_csv(consolidated_file, index=False)
print(f"  Consolidated file saved: {consolidated_file}")
print()

# ============================================================================
# 2. DATA CLEANING AND EXPLORATION
# ============================================================================

print("STEP 2: Data exploration...")
print()

print("Available columns:")
for col in all_results.columns:
    print(f"  - {col}")
print()

print("Data types:")
print(all_results.dtypes)
print()

print("Shape:", all_results.shape)
print("Algorithms:", all_results['algorithm'].unique())
print()

# ============================================================================
# 3. SUMMARY BY CONFIGURATION
# ============================================================================

print("STEP 3: Computing summary statistics by configuration...")
print()

# Group by (instance, enforce_time_windows, algorithm, iterations) if available
group_cols = ['instance', 'algorithm']

# Try to infer iterations and TW mode from other columns if not directly available
if 'enforce_time_windows' not in all_results.columns:
    print("WARNING: Column 'enforce_time_windows' not found. Assuming single TW mode.")
    all_results['enforce_time_windows'] = 'unknown'

if 'iterations' not in all_results.columns:
    print("WARNING: Column 'iterations' not found. Cannot group by iterations.")
else:
    group_cols.append('iterations')

if 'enforce_time_windows' in all_results.columns:
    group_cols.append('enforce_time_windows')

# Distance/objective statistics
stats_cols = ['best_distance', 'best_objective']
available_stats = [col for col in stats_cols if col in all_results.columns]

if len(available_stats) == 0:
    print("ERROR: No distance or objective columns found.")
    exit(1)

metric_col = available_stats[0]  # Use first available

summary = all_results.groupby(group_cols)[metric_col].agg([
    ('count', 'count'),
    ('mean', 'mean'),
    ('std', 'std'),
    ('min', 'min'),
    ('max', 'max'),
    ('median', 'median')
]).round(4)

print(f"Summary statistics (metric: {metric_col}):")
print(summary)
print()

summary_file = f"campaign3_summary_{timestamp}.csv"
summary.to_csv(summary_file)
print(f"Summary file saved: {summary_file}")
print()

# ============================================================================
# 4. FEASIBILITY ANALYSIS
# ============================================================================

print("STEP 4: Feasibility analysis...")
print()

feasibility_check = [
    'time_violation', 'capacity_violation',
    'time_violations', 'capacity_violations'  # alternate names
]
available_feasibility = [col for col in feasibility_check if col in all_results.columns]

if len(available_feasibility) > 0:
    feas_col = available_feasibility[0]
    all_results['is_feasible'] = all_results[feas_col] == 0
    
    feasibility_summary = all_results.groupby(group_cols).agg({
        'is_feasible': ['sum', 'count', 'mean']
    }).round(3)
    feasibility_summary.columns = ['feasible_count', 'total_runs', 'feasibility_rate']
    
    print(f"Feasibility rates (based on {feas_col}):")
    print(feasibility_summary)
    print()
    
    feas_file = f"campaign3_feasibility_{timestamp}.csv"
    feasibility_summary.to_csv(feas_file)
    print(f"Feasibility file saved: {feas_file}")
else:
    print("WARNING: No feasibility violation columns found. Skipping feasibility analysis.")
    print()

# ============================================================================
# 5. RUNTIME ANALYSIS
# ============================================================================

print("STEP 5: Runtime analysis...")
print()

if 'runtime_ms' in all_results.columns or 'runtime' in all_results.columns:
    runtime_col = 'runtime_ms' if 'runtime_ms' in all_results.columns else 'runtime'
    
    runtime_summary = all_results.groupby(group_cols)[runtime_col].agg([
        ('mean_ms', 'mean'),
        ('median_ms', 'median'),
        ('std_ms', 'std'),
        ('min_ms', 'min'),
        ('max_ms', 'max')
    ]).round(2)
    
    # Convert to seconds for readability
    runtime_summary_sec = runtime_summary / 1000
    runtime_summary_sec.columns = [
        'mean_sec', 'median_sec', 'std_sec', 'min_sec', 'max_sec'
    ]
    
    print(f"Runtime analysis (in seconds):")
    print(runtime_summary_sec)
    print()
    
    runtime_file = f"campaign3_runtime_{timestamp}.csv"
    runtime_summary_sec.to_csv(runtime_file)
    print(f"Runtime file saved: {runtime_file}")
else:
    print("WARNING: No runtime column found. Skipping runtime analysis.")
    print()

# ============================================================================
# 6. QUALITY COMPARISON SA vs TABU
# ============================================================================

print("STEP 6: Quality comparison (SA vs Tabu)...")
print()

if 'algorithm' in all_results.columns:
    # Pivot to compare SA vs Tabu directly
    comparison_cols = [col for col in group_cols if col != 'algorithm']
    
    quality_pivot = all_results.pivot_table(
        index=comparison_cols,
        columns='algorithm',
        values=metric_col,
        aggfunc='mean'
    )
    
    # Add difference and ratio
    if 'SA' in quality_pivot.columns and 'TABU' in quality_pivot.columns:
        quality_pivot['Difference (Tabu - SA)'] = quality_pivot['TABU'] - quality_pivot['SA']
        quality_pivot['Ratio (Tabu/SA)'] = (quality_pivot['TABU'] / quality_pivot['SA']).round(4)
        quality_pivot = quality_pivot.round(4)
        
        print("Quality comparison:")
        print(quality_pivot)
        print()
        
        comparison_file = f"campaign3_comparison_sa_tabu_{timestamp}.csv"
        quality_pivot.to_csv(comparison_file)
        print(f"Comparison file saved: {comparison_file}")

print()

# ============================================================================
# 7. SUMMARY REPORT
# ============================================================================

print("=" * 80)
print("ANALYSIS SUMMARY")
print("=" * 80)
print()

print(f"Total records analyzed: {len(all_results)}")
print(f"Algorithms: {all_results['algorithm'].unique().tolist()}")
print(f"Instances: {all_results['instance'].unique().tolist()}")

if 'enforce_time_windows' in all_results.columns:
    print(f"TW Modes: {all_results['enforce_time_windows'].unique().tolist()}")

if 'iterations' in all_results.columns:
    print(f"Iterations: {sorted(all_results['iterations'].unique().tolist())}")

print()
print("FILES GENERATED:")
print(f"  1. {consolidated_file} - Full consolidated results")
print(f"  2. {summary_file} - Summary statistics")
if len(available_feasibility) > 0:
    print(f"  3. {feas_file} - Feasibility analysis")
if 'runtime_ms' in all_results.columns or 'runtime' in all_results.columns:
    print(f"  4. {runtime_file} - Runtime analysis")
if 'SA' in quality_pivot.columns and 'TABU' in quality_pivot.columns:
    print(f"  5. {comparison_file} - SA vs Tabu comparison")

print()
print("=" * 80)
print("NEXT STEPS:")
print("=" * 80)
print()
print("1. Review the generated CSV files in Excel or your favorite tool")
print("2. Create plots:")
print("   - Box plots of distance by configuration")
print("   - Line plots of iterations vs average distance")
print("   - Bar charts of runtime comparison")
print("3. Discuss findings:")
print("   - Which algorithm wins per instance?")
print("   - How does TW impact the results?")
print("   - Where is the convergence plateau?")
print("   - Which algorithm is more robust (lower std)?")
print()
print("Analysis complete!")
print()
