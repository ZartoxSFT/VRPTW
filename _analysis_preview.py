import csv, json, math, statistics, re
from pathlib import Path
from datetime import datetime
from collections import defaultdict, Counter

root = Path(r'c:/Users/darkf/Desktop/Travail/VRPTW')
paths = list(root.glob('resultsSA/**/executions_log.csv')) + list(root.glob('resultTABU/**/executions_log.csv'))

def parse_float(s):
    if s is None or s == '':
        return None
    s = s.strip().replace(' ', '')
    # locale-aware fallback for comma decimals
    if ',' in s and '.' not in s:
        s = s.replace(',', '.')
    try:
        return float(s)
    except ValueError:
        return None

def parse_int(s):
    if s is None or s == '':
        return None
    try:
        return int(float(s.replace(',', '.')))
    except Exception:
        return None

def parse_bool(s):
    return str(s).strip().lower() in {'true','1','yes','y'}

def parse_params(p):
    out = {}
    for part in str(p).split(';'):
        part = part.strip()
        if not part or '=' not in part:
            continue
        k,v = part.split('=',1)
        out[k.strip()] = v.strip()
    return out

rows = []
for path in paths:
    algo_dir = path.parts[-3].lower()
    campaign = 'campaign2' if any(seg == 'Exp' for seg in []) else None
    with path.open(newline='', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f)
        for r in reader:
            dt = datetime.fromisoformat(r['timestamp'])
            params = parse_params(r.get('parameters',''))
            row = {
                'file': str(path),
                'algorithm': r['algorithm'].strip().lower(),
                'instance': r['instance'].strip(),
                'timestamp': dt,
                'date': dt.date().isoformat(),
                'best_objective': parse_float(r['best_objective']),
                'best_distance': parse_float(r['best_distance']),
                'time_violation': parse_float(r['time_violation']),
                'capacity_violation': parse_float(r['capacity_violation']),
                'vehicle_violation': parse_float(r['vehicle_violation']),
                'routes': parse_int(r['routes']),
                'runtime_ms': parse_int(r['runtime_ms']),
                'solutions_evaluated': parse_int(r['solutions_evaluated']),
                'penalty_weight': parse_float(r['penalty_weight']),
                'enforce_time_windows': parse_bool(r['enforce_time_windows']),
                'max_vehicles': parse_int(r['max_vehicles']),
                'seed': parse_int(params.get('seed')),
                'interNeighborhoodType': params.get('interNeighborhoodType'),
                'intraNeighborhoodType': params.get('intraNeighborhoodType'),
                'neighborhoodFamily': f"{params.get('interNeighborhoodType')}__{params.get('intraNeighborhoodType')}",
                'campaign': 'campaign1' if dt.date().isoformat() == '2026-04-22' else 'campaign2',
            }
            rows.append(row)

print('rows', len(rows))
print('algos', Counter(r['algorithm'] for r in rows))
print('campaigns', Counter(r['campaign'] for r in rows))
print('unique seeds', sorted(set(r['seed'] for r in rows)))
print('family counts', Counter(r['neighborhoodFamily'] for r in rows))
print('tw counts', Counter(r['enforce_time_windows'] for r in rows))
print('feasible', sum(1 for r in rows if abs(r['time_violation'] or 0)<1e-12 and abs(r['capacity_violation'] or 0)<1e-12 and abs(r['vehicle_violation'] or 0)<1e-12))

# exact matched comparisons on same campaign/seed/family/tw
by_key = defaultdict(dict)
for r in rows:
    k = (r['campaign'], r['seed'], r['neighborhoodFamily'], r['enforce_time_windows'])
    by_key[k][r['algorithm']] = r
matched = [v for v in by_key.values() if 'sa' in v and 'tabu' in v]
print('matched cells', len(matched))
if matched:
    sa_better = sum(1 for v in matched if v['sa']['best_distance'] < v['tabu']['best_distance'])
    tabu_better = sum(1 for v in matched if v['tabu']['best_distance'] < v['sa']['best_distance'])
    ties = len(matched) - sa_better - tabu_better
    print('sa better', sa_better, 'tabu better', tabu_better, 'ties', ties)
    print('mean dist sa', sum(v['sa']['best_distance'] for v in matched)/len(matched))
    print('mean dist tabu', sum(v['tabu']['best_distance'] for v in matched)/len(matched))
    print('mean runtime sa', sum(v['sa']['runtime_ms'] for v in matched)/len(matched))
    print('mean runtime tabu', sum(v['tabu']['runtime_ms'] for v in matched)/len(matched))

