import pandas as pd
from pathlib import Path

f = list(Path('.').glob('campaign3_consolidated_*.csv'))
if not f:
    raise SystemExit('No consolidated file found')
df = pd.read_csv(f[0])

df101 = df[df['instance'] == 'data101.vrp'].copy()
df101['routes'] = pd.to_numeric(df101['routes'], errors='coerce')
res = df101[df101['routes'] == 19]
print(f'Found {len(res)} runs with routes==19 for data101.vrp')
if not res.empty:
    print(res[['timestamp','algorithm','best_distance','routes','runtime_ms','parameters']].to_string(index=False))
