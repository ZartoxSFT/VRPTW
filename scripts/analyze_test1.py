import pandas as pd
from pathlib import Path

# Load all new runs (Exp191-195)
new_logs = []
for exp in range(191, 196):
    log_path = Path(f'resultsSA/Exp{exp}/executions_log.csv')
    if log_path.exists():
        df = pd.read_csv(log_path)
        new_logs.append(df)

if new_logs:
    df_new = pd.concat(new_logs, ignore_index=True)
    
    print("="*100)
    print("TEST 1 RESULTS - SA 100k Iterations (Exp191-195)")
    print("="*100)
    print()
    
    # Check iterations
    df_new['iterations'] = df_new['parameters'].str.extract(r'iterations=(\d+)').astype(int)
    
    print(f"Total runs: {len(df_new)}")
    print(f"Iterations range: {df_new['iterations'].min()}-{df_new['iterations'].max()}")
    print(f"Instances: {sorted(df_new['instance'].unique())}")
    print(f"TW mode: {sorted(df_new['enforce_time_windows'].unique())}")
    print()
    
    # Filter WITH TW + 100k iterations
    df_test1 = df_new[(df_new['enforce_time_windows'] == True) & (df_new['iterations'] == 100000)]
    
    print("="*100)
    print("FILTERED: enforce_time_windows=True AND iterations=100000")
    print("="*100)
    print(f"Runs matching: {len(df_test1)}")
    print()
    
    if len(df_test1) > 0:
        # Group by instance
        for inst in sorted(df_test1['instance'].unique()):
            sub = df_test1[df_test1['instance'] == inst]
            print(f"\n{inst}:")
            print(f"  Min distance: {sub['best_distance'].min():.2f} km")
            print(f"  Max distance: {sub['best_distance'].max():.2f} km")
            print(f"  Mean distance: {sub['best_distance'].mean():.2f} km (±{sub['best_distance'].std():.2f})")
            print(f"  Runs: {len(sub)}")
            print(f"  Seed: {sub.iloc[0]['parameters'].split('seed=')[1].split(';')[0]}")
        
        print()
        print("="*100)
        print("COMPARISON WITH PROF")
        print("="*100)
        
        # data101 comparison
        data101 = df_test1[df_test1['instance'] == 'data101.vrp']
        if len(data101) > 0:
            min_sa = data101['best_distance'].min()
            print(f"\ndata101.vrp WITH TW (100k iter):")
            print(f"  SA best found:  {min_sa:.2f} km")
            print(f"  Prof optimum:   1650.80 km")
            print(f"  Difference:     {abs(min_sa - 1650.80):.2f} km ({100*abs(min_sa - 1650.80)/1650.80:.1f}%)")
            
            if min_sa <= 1700:
                print(f"  ✓ PERTINENT! Close to prof")
            elif min_sa <= 1800:
                print(f"  ~ ACCEPTABLE but needs more tuning")
            else:
                print(f"  ✗ Not converging yet")
    else:
        print("⚠ No runs with enforce_time_windows=True AND iterations=100000 found")
        print("\nAll runs in Test 1:")
        print(df_new[['instance', 'enforce_time_windows', 'iterations', 'best_distance', 'routes']].to_string())

else:
    print("No new logs found in Exp191-195")
