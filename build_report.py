import math
from pathlib import Path
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

BASE = Path('.')
REPORT_DIR = BASE / 'report_assets'
FIG_DIR = REPORT_DIR / 'figures'
REPORT_DIR.mkdir(parents=True, exist_ok=True)
FIG_DIR.mkdir(parents=True, exist_ok=True)

viol_thresh = 1e-9
required_cols = [
    'timestamp','instance','algorithm','best_objective','best_distance',
    'time_violation','capacity_violation','vehicle_violation','routes',
    'runtime_ms','solutions_evaluated','penalty_weight','enforce_time_windows','parameters'
]

missing_by_file = {}
frames = []
for top in ['resultsSA', 'resultTABU']:
    for log in sorted((BASE / top).glob('Exp*/executions_log.csv')):
        try:
            df = pd.read_csv(log)
        except Exception as e:
            missing_by_file[str(log)] = [f'read_error:{e}']
            continue
        miss = [c for c in required_cols if c not in df.columns]
        if miss:
            missing_by_file[str(log)] = miss
        for c in required_cols:
            if c not in df.columns:
                if c in ['vehicle_violation','time_violation','capacity_violation']:
                    df[c] = 0.0
                elif c in ['routes','runtime_ms','solutions_evaluated']:
                    df[c] = np.nan
                elif c == 'enforce_time_windows':
                    df[c] = False
                else:
                    df[c] = np.nan
        df['source_dir'] = log.parent.as_posix()
        frames.append(df)

if not frames:
    raise SystemExit('No executions_log.csv files found')

all_df = pd.concat(frames, ignore_index=True)

for c in ['best_objective','best_distance','time_violation','capacity_violation','vehicle_violation','routes','runtime_ms','solutions_evaluated','penalty_weight']:
    all_df[c] = pd.to_numeric(all_df[c], errors='coerce')

all_df['timestamp'] = pd.to_datetime(all_df['timestamp'], errors='coerce')
all_df['algorithm'] = all_df['algorithm'].astype(str).str.lower().str.strip()
all_df['instance'] = all_df['instance'].astype(str).str.strip()
all_df['enforce_time_windows'] = all_df['enforce_time_windows'].astype(str).str.lower().map({'true':True,'false':False}).fillna(all_df['enforce_time_windows'])
all_df['enforce_time_windows'] = all_df['enforce_time_windows'].astype(bool)

all_df['feasible'] = (
    all_df['time_violation'].fillna(0).abs() <= viol_thresh
) & (
    all_df['capacity_violation'].fillna(0).abs() <= viol_thresh
) & (
    all_df['vehicle_violation'].fillna(0).abs() <= viol_thresh
)

consolidated_cols = [
    'source_dir','timestamp','instance','algorithm','best_objective','best_distance',
    'time_violation','capacity_violation','vehicle_violation','routes','runtime_ms',
    'solutions_evaluated','penalty_weight','enforce_time_windows','parameters','feasible'
]
consolidated = all_df[consolidated_cols].copy()
consolidated.to_csv(REPORT_DIR / 'consolidated_runs.csv', index=False)


def stats_table(df, group_cols=None):
    g = df.groupby(group_cols, dropna=False) if group_cols else [((), df)]
    rows = []
    for key, sub in g:
        row = {}
        if group_cols:
            if len(group_cols) == 1:
                row[group_cols[0]] = key
            else:
                for k, col in zip(key, group_cols):
                    row[col] = k
        row['count'] = len(sub)
        row['feasible_count'] = int(sub['feasible'].sum())
        row['feasibility_rate_pct'] = (100.0 * row['feasible_count'] / row['count']) if row['count'] else np.nan
        row['best_distance_min'] = sub['best_distance'].min()
        row['best_distance_mean'] = sub['best_distance'].mean()
        row['best_distance_std'] = sub['best_distance'].std(ddof=1)
        row['runtime_ms_mean'] = sub['runtime_ms'].mean()
        row['runtime_ms_median'] = sub['runtime_ms'].median()
        row['solutions_evaluated_mean'] = sub['solutions_evaluated'].mean()
        rows.append(row)
    out = pd.DataFrame(rows)
    if group_cols:
        out = out.sort_values(group_cols).reset_index(drop=True)
    return out

summary_overall = stats_table(consolidated, ['algorithm'])
summary_overall = pd.concat([
    pd.DataFrame([{'algorithm':'all', **stats_table(consolidated).iloc[0].to_dict()}]),
    summary_overall
], ignore_index=True)
summary_overall.to_csv(REPORT_DIR / 'summary_overall.csv', index=False)

summary_by_instance = stats_table(consolidated, ['instance'])
summary_by_instance.to_csv(REPORT_DIR / 'summary_by_instance.csv', index=False)

summary_by_tw = stats_table(consolidated, ['enforce_time_windows'])
summary_by_tw.to_csv(REPORT_DIR / 'summary_by_tw.csv', index=False)

summary_by_instance_tw_algo = stats_table(consolidated, ['instance','enforce_time_windows','algorithm'])
summary_by_instance_tw_algo.to_csv(REPORT_DIR / 'summary_by_instance_tw_algo.csv', index=False)

inst_rows = []
for vrp in sorted((BASE / 'data').glob('*.vrp')):
    text = vrp.read_text(encoding='utf-8', errors='ignore').splitlines()
    nb_clients = np.nan
    capacity = np.nan
    total_demand = 0.0
    in_clients = False
    for line in text:
        s = line.strip()
        if not s:
            continue
        if s.upper().startswith('NB_CLIENTS:'):
            try:
                nb_clients = int(s.split(':',1)[1].strip())
            except Exception:
                pass
        if s.upper().startswith('MAX_QUANTITY:'):
            try:
                capacity = float(s.split(':',1)[1].strip())
            except Exception:
                pass
        if s.upper().startswith('DATA_CLIENTS'):
            in_clients = True
            continue
        if in_clients:
            parts = s.split()
            if len(parts) >= 7 and parts[0].lower().startswith('c'):
                try:
                    total_demand += float(parts[5])
                except Exception:
                    pass
    lb = math.ceil(total_demand / capacity) if (pd.notna(capacity) and capacity > 0) else np.nan
    inst_rows.append({'instance': vrp.name,'clients': nb_clients,'capacity': capacity,'total_demand': total_demand,'capacity_lower_bound': lb})

pd.DataFrame(inst_rows).to_csv(REPORT_DIR / 'instance_characteristics.csv', index=False)

plot_df = consolidated.dropna(subset=['best_distance']).copy()

plt.figure(figsize=(8,5))
for algo, sub in plot_df.groupby('algorithm'):
    x = np.log10(sub['runtime_ms'].clip(lower=0) + 1)
    plt.scatter(x, sub['best_distance'], s=18, alpha=0.7, label=algo)
plt.xlabel('log10(runtime_ms + 1)')
plt.ylabel('best_distance')
plt.title('Quality vs Time')
plt.legend()
plt.tight_layout()
plt.savefig(FIG_DIR / 'fig_quality_vs_time_scatter.png', dpi=150)
plt.close()

feas_algo = consolidated.groupby('algorithm', dropna=False)['feasible'].mean().mul(100)
plt.figure(figsize=(6,4))
feas_algo.plot(kind='bar', color=['#4c78a8','#f58518','#54a24b','#e45756'])
plt.ylabel('Feasibility rate (%)')
plt.title('Feasibility by algorithm')
plt.tight_layout()
plt.savefig(FIG_DIR / 'fig_feasibility_by_algo.png', dpi=150)
plt.close()

plt.figure(figsize=(7,5))
box_data = [sub['best_distance'].dropna().values for _, sub in consolidated.groupby('algorithm')]
labels = [str(a) for a, _ in consolidated.groupby('algorithm')]
if box_data:
    plt.boxplot(box_data, tick_labels=labels)
plt.ylabel('best_distance')
plt.title('Distance distribution by algorithm')
plt.tight_layout()
plt.savefig(FIG_DIR / 'fig_distance_boxplot_by_algo.png', dpi=150)
plt.close()

plt.figure(figsize=(7,5))
rt_data = [sub['runtime_ms'].dropna().clip(lower=1).values for _, sub in consolidated.groupby('algorithm')]
labels_rt = [str(a) for a, _ in consolidated.groupby('algorithm')]
if rt_data:
    plt.boxplot(rt_data, tick_labels=labels_rt)
    plt.yscale('log')
plt.ylabel('runtime_ms (log scale)')
plt.title('Runtime distribution by algorithm')
plt.tight_layout()
plt.savefig(FIG_DIR / 'fig_runtime_boxplot_by_algo_log.png', dpi=150)
plt.close()

dist_inst_algo = consolidated.groupby(['instance','algorithm'], dropna=False)['best_distance'].mean().unstack('algorithm')
if dist_inst_algo.shape[0] > 0:
    ax = dist_inst_algo.plot(kind='bar', figsize=(12,5))
    ax.set_ylabel('mean best_distance')
    ax.set_title('Distance by instance and algorithm')
    plt.tight_layout()
    plt.savefig(FIG_DIR / 'fig_distance_by_instance_algo.png', dpi=150)
    plt.close()

feas_inst_algo = consolidated.groupby(['instance','algorithm'], dropna=False)['feasible'].mean().mul(100).unstack('algorithm')
if feas_inst_algo.shape[0] > 0:
    ax = feas_inst_algo.plot(kind='bar', figsize=(12,5))
    ax.set_ylabel('feasibility rate (%)')
    ax.set_title('Feasibility by instance and algorithm')
    plt.tight_layout()
    plt.savefig(FIG_DIR / 'fig_feasibility_by_instance_algo.png', dpi=150)
    plt.close()

for algo, fig_name in [('sa','fig_convergence_sa_best.png'), ('tabu','fig_convergence_tabu_best.png')]:
    sub = consolidated[consolidated['algorithm'] == algo].copy()
    if sub.empty:
        continue
    feas_sub = sub[sub['feasible']]
    pick = feas_sub.nsmallest(1, 'best_distance') if not feas_sub.empty else sub.nsmallest(1, 'best_distance')
    if pick.empty:
        continue
    row = pick.iloc[0]
    inst_stem = Path(str(row['instance'])).stem
    source_dir = Path(str(row['source_dir']))
    cands = sorted(source_dir.glob(f'{inst_stem}_{algo}_*_history.csv'))
    if not cands:
        cands = sorted(source_dir.glob('*_history.csv'))
    if not cands:
        continue
    hist_path = cands[0]
    try:
        h = pd.read_csv(hist_path)
    except Exception:
        continue
    ycol = 'best_objective' if 'best_objective' in h.columns else (h.columns[1] if len(h.columns) > 1 else None)
    xcol = 'iter' if 'iter' in h.columns else h.columns[0]
    if ycol is None:
        continue
    plt.figure(figsize=(8,4))
    plt.plot(h[xcol], h[ycol], linewidth=1.2)
    plt.xlabel(xcol)
    plt.ylabel(ycol)
    plt.title(f'Convergence {algo.upper()} - {hist_path.name}')
    plt.tight_layout()
    plt.savefig(FIG_DIR / fig_name, dpi=150)
    plt.close()

runs_by_algo = consolidated['algorithm'].value_counts().to_dict()
period_min = consolidated['timestamp'].min()
period_max = consolidated['timestamp'].max()
instances = sorted(consolidated['instance'].dropna().unique().tolist())
tw_values = sorted(consolidated['enforce_time_windows'].dropna().unique().tolist())

lines = []
lines.append('Analysis notes')
lines.append('==============')
lines.append(f"Total runs: {len(consolidated)}")
for a in sorted(runs_by_algo):
    lines.append(f"Runs {a.upper()}: {runs_by_algo[a]}")
lines.append(f"Time period: {period_min} -> {period_max}")
lines.append(f"Instances covered ({len(instances)}): {', '.join(instances)}")
lines.append(f"TW values: {tw_values}")
lines.append('')
lines.append('Orders of magnitude:')
lines.append(f"best_distance range: {consolidated['best_distance'].min():.3f} -> {consolidated['best_distance'].max():.3f}")
lines.append(f"runtime_ms median: {consolidated['runtime_ms'].median():.3f}, mean: {consolidated['runtime_ms'].mean():.3f}")
lines.append(f"solutions_evaluated mean: {consolidated['solutions_evaluated'].mean():.3f}")
lines.append(f"overall feasibility rate: {100.0 * consolidated['feasible'].mean():.2f}%")
if missing_by_file:
    lines.append('')
    lines.append('Missing columns detected:')
    for k,v in missing_by_file.items():
        lines.append(f"- {k}: {v}")

(REPORT_DIR / 'analysis_notes.txt').write_text('\n'.join(lines), encoding='utf-8')

print('Generated files under', REPORT_DIR.resolve())
print('Rows consolidated:', len(consolidated))
print('Algorithms:', sorted(consolidated['algorithm'].dropna().unique().tolist()))
print('Missing-column files:', len(missing_by_file))
