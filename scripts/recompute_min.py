import pandas as pd
from pathlib import Path

f = list(Path('.').glob('campaign3_consolidated_*.csv'))
if not f:
    raise SystemExit('No consolidated file found')
df = pd.read_csv(f[0])

print('Using consolidated file:', f[0].name)

grp = df.groupby(['instance','enforce_time_windows','algorithm'])['best_distance'].min().reset_index()
print(grp.to_string(index=False))

print('\nAll minima by instance/algorithm:')
mins = df.groupby(['instance','algorithm'])['best_distance'].min().reset_index()
print(mins.to_string(index=False))
