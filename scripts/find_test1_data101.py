import pandas as pd
from pathlib import Path

# Find all data101 WITH TW and 100k iter runs
all_sa = list(Path('resultsSA').rglob('executions_log.csv'))

test1_runs = []
for log in all_sa:
    try:
        df = pd.read_csv(log)
        subset = df[(df['instance'] == 'data101.vrp') & 
                    (df['enforce_time_windows'] == True) &
                    (df['parameters'].str.contains('iterations=100000', na=False))]
        if len(subset) > 0:
            subset['source'] = str(log)
            test1_runs.append(subset)
    except:
        pass

if test1_runs:
    df_test1 = pd.concat(test1_runs, ignore_index=True)
    df_test1['timestamp'] = pd.to_datetime(df_test1['timestamp'], errors='coerce')
    df_test1 = df_test1.sort_values('timestamp', ascending=False)
    
    print("="*100)
    print("TEST 1 - data101.vrp WITH TW AND 100k iterations (MOST RECENT)")
    print("="*100)
    print(f"Total matching runs: {len(df_test1)}\n")
    
    # Show top results
    for idx, row in df_test1.head(10).iterrows():
        print(f"Distance: {row['best_distance']:7.2f} km | Routes: {row['routes']:2.0f} | Runtime: {row['runtime_ms']:6.0f}ms | Timestamp: {row['timestamp']}")
    
    print()
    print("="*100)
    print("SUMMARY")
    print("="*100)
    print(f"Min:     {df_test1['best_distance'].min():.2f} km")
    print(f"Mean:    {df_test1['best_distance'].mean():.2f} km (±{df_test1['best_distance'].std():.2f})")
    print(f"Max:     {df_test1['best_distance'].max():.2f} km")
    print()
    print(f"Prof optimum (reference): 1650.80 km")
    print(f"Difference (best vs prof): {df_test1['best_distance'].min() - 1650.80:+.2f} km")
    
    if df_test1['best_distance'].min() <= 1700:
        print("\n✓ TEST 1 PERTINENT! Very close to prof's solution")
    elif df_test1['best_distance'].mean() <= 1700:
        print("\n~ TEST 1 ACCEPTABLE - mean is close, but max variance")
    else:
        print("\n✗ TEST 1 NOT CONVERGING - need other changes")

else:
    print("No Test 1 runs (data101 WITH TW 100k iterations) found")
