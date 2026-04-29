import pandas as pd
from pathlib import Path

# Load consolidated WITH TW only
f = list(Path('.').glob('campaign3_consolidated_*.csv'))[0]
df = pd.read_csv(f)
df_tw = df[df['enforce_time_windows'] == True].copy()

print("=" * 100)
print("CAMPAIGN 3 - PERTINENT DATA (WITH TIME WINDOWS ONLY)")
print("=" * 100)
print()

# 1. Overall stats
print("1. OVERALL STATISTICS (ALL RUNS WITH TW)")
print("-" * 100)
overall = df_tw.groupby('algorithm')['best_distance'].agg(['count', 'min', 'max', 'mean', 'std']).round(2)
print(overall)
print()

# 2. By instance
print("2. BY INSTANCE (WITH TW)")
print("-" * 100)
by_inst = df_tw.groupby(['instance', 'algorithm'])['best_distance'].agg(['count', 'min', 'mean', 'std']).round(2)
print(by_inst)
print()

# 3. Best overall solutions
print("3. BEST SOLUTIONS FOUND (WITH TW)")
print("-" * 100)
for algo in ['SA', 'TABU']:
    sub = df_tw[df_tw['algorithm'] == algo]
    best = sub.loc[sub['best_distance'].idxmin()]
    print(f"\n{algo}:")
    print(f"  Distance: {best['best_distance']:.2f} km")
    print(f"  Instance: {best['instance']}")
    print(f"  Routes: {best['routes']:.0f}")
    print(f"  Runtime: {best['runtime_ms']:.0f} ms")
    print(f"  Feasible: {best['time_violation'] < 1e-9 and best['capacity_violation'] < 1e-9}")
    print(f"  Parameters: {best['parameters'][:80]}...")

# 4. Per-instance best
print("\n4. BEST PER INSTANCE (WITH TW)")
print("-" * 100)
for inst in sorted(df_tw['instance'].unique()):
    print(f"\n{inst}:")
    inst_data = df_tw[df_tw['instance'] == inst]
    for algo in ['SA', 'TABU']:
        sub = inst_data[inst_data['algorithm'] == algo]
        if not sub.empty:
            best = sub.loc[sub['best_distance'].idxmin()]
            print(f"  {algo:4s}: dist={best['best_distance']:7.2f}, routes={best['routes']:2.0f}, mean={sub['best_distance'].mean():.2f}±{sub['best_distance'].std():.2f}")

# 5. Convergence with iterations
print("\n5. CONVERGENCE ANALYSIS (WITH TW)")
print("-" * 100)
df_tw['iterations'] = df_tw['parameters'].str.extract(r'iterations=(\d+)').astype(float)
conv = df_tw.groupby(['iterations', 'algorithm'])['best_distance'].agg(['mean', 'min']).round(2)
print(conv)

print("\n" + "=" * 100)
