import pandas as pd
from pathlib import Path

# Load PREVIOUS consolidated data
f_old = list(Path('.').glob('campaign3_consolidated_*.csv'))[0]
df_old = pd.read_csv(f_old)

# Filter data101 WITH TW by iterations
df_old['iterations'] = df_old['parameters'].str.extract(r'iterations=(\d+)').astype(int)
data101_tw_old = df_old[(df_old['instance'] == 'data101.vrp') & 
                        (df_old['enforce_time_windows'] == True) &
                        (df_old['algorithm'] == 'SA')]

# Now check NEW runs (since Test 1)
all_sa = list(Path('resultsSA').rglob('executions_log.csv'))
new_runs = []
for log in all_sa:
    try:
        df = pd.read_csv(log)
        subset = df[(df['instance'] == 'data101.vrp') & 
                    (df['enforce_time_windows'] == True)]
        if len(subset) > 0:
            subset['source_file'] = str(log)
            new_runs.append(subset)
    except:
        pass

if new_runs:
    df_new = pd.concat(new_runs, ignore_index=True)
    df_new['iterations'] = df_new['parameters'].str.extract(r'iterations=(\d+)').astype(int)
    df_new['timestamp'] = pd.to_datetime(df_new['timestamp'], errors='coerce')
    
    print("="*100)
    print("COMPARISON: SA data101 WITH TW - OLD vs NEW")
    print("="*100)
    
    print("\nOLD DATA (from consolidated CSV):")
    for it in sorted(data101_tw_old['iterations'].unique()):
        sub = data101_tw_old[data101_tw_old['iterations'] == it]
        print(f"  {int(it):,} iterations: min={sub['best_distance'].min():.2f}, mean={sub['best_distance'].mean():.2f}±{sub['best_distance'].std():.2f}, count={len(sub)}")
    
    print("\nNEW DATA (from resultsSA logs after Test 1):")
    for it in sorted(df_new['iterations'].unique()):
        sub = df_new[df_new['iterations'] == it]
        print(f"  {int(it):,} iterations: min={sub['best_distance'].min():.2f}, mean={sub['best_distance'].mean():.2f}±{sub['best_distance'].std():.2f}, count={len(sub)}")
    
    print("\n" + "="*100)
    print("ANALYSIS")
    print("="*100)
    
    # Check if Test 1 100k has duplicate results
    test1_100k = df_new[df_new['iterations'] == 100000]
    if len(test1_100k) > 0:
        print(f"\nTest 1 runs (100k iter): {len(test1_100k)} runs")
        print(f"  Min: {test1_100k['best_distance'].min():.2f}")
        print(f"  Unique distances: {test1_100k['best_distance'].nunique()}")
        
        # Compare with old 100k
        old_100k = data101_tw_old[data101_tw_old['iterations'] == 100000]
        if len(old_100k) > 0:
            print(f"\nOld 100k runs: {len(old_100k)} runs")
            print(f"  Min: {old_100k['best_distance'].min():.2f}")
            
            if test1_100k['best_distance'].min() == old_100k['best_distance'].min():
                print(f"\n✓ Test 1 100k results are IDENTICAL to old data (same seeds/params)")
                print("  => Not a regression, just duplicates")
            else:
                print(f"\n✗ Different results! Possible regression or new seeds")
                print("  Comparing specific runs:")
                print("\nOld 100k best:", old_100k.nsmallest(3, 'best_distance')[['best_distance', 'routes', 'parameters']].to_string())
                print("\nNew 100k best:", test1_100k.nsmallest(3, 'best_distance')[['best_distance', 'routes', 'parameters']].to_string())

else:
    print("No new runs found")
