import pandas as pd
from pathlib import Path

f = list(Path('.').glob('campaign3_consolidated_*.csv'))
if not f:
    raise SystemExit('No consolidated file found')
df = pd.read_csv(f[0])

df_true = df[df['enforce_time_windows'] == True].copy()
df_true['routes'] = pd.to_numeric(df_true['routes'], errors='coerce')

matches = df_true[(df_true['routes'] == 19) & (df_true['best_distance'].between(1645,1660))]
print(f'Found {len(matches)} matching runs (routes=19, distance~1650.8)')
if not matches.empty:
    print(matches[['timestamp','instance','algorithm','best_distance','routes','runtime_ms','parameters']].to_string(index=False))
else:
    # print closest matches
    closest = df_true.copy()
    closest['dist_diff'] = (closest['best_distance'] - 1650.8).abs()
    closest = closest.nsmallest(10, 'dist_diff')
    print('\nClosest 10 runs to 1650.8:')
    print(closest[['timestamp','instance','algorithm','best_distance','routes','runtime_ms','parameters','dist_diff']].to_string(index=False))
