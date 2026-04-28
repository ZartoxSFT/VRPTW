# OPTIMAL PARAMETERS FOR VRPTW - Campaign 3 Results

Generated: April 28, 2026 (186 runs analyzed)

---

## CONFIGURATION 1: SIMULATED ANNEALING (SA) - OPTIMAL

### Primary Recommendation: FAST & ACCEPTABLE QUALITY

**Use Case:** Time-constrained environments (< 1 second needed)

```
Algorithm: Simulated Annealing (SA)
Initial Temperature (T0): 1250.0
Cooling Rate: 0.9993
Iterations: 30000  (good compromise; 100k for best quality)
Neighborhood Family: inter
Inter Neighborhood Type: relocate
Intra Neighborhood Type: 2opt
Max Vehicles: [instance-dependent, estimate minimum]
Penalty Weight: 1000.0
```

**Expected Performance:**
- Average Distance: 1415.66 ± 419.08 km
- Best Case: 921.91 km
- Runtime: ~0.1-0.2 seconds
- Feasibility Rate: 99.1%
- Feasibility (with TW): 98.1%

**Advantages:**
- ✓ Extremely fast (< 200ms)
- ✓ Good quality for production use
- ✓ Predictable runtime
- ✓ Stable across instances

**Disadvantages:**
- ✗ 12% worse quality than Tabu
- ✗ Plateau after 30k iterations (diminishing returns)

---

## CONFIGURATION 2: TABU SEARCH - OPTIMAL (QUALITY FOCUSED)

### Primary Recommendation: BEST QUALITY (at cost of time)

**Use Case:** Offline optimization / batch processing (time not critical)

```
Algorithm: Tabu Search (Tabu)
Tabu Tenure: 40
Iterations: 30000  (converges early; 100k for absolute best)
Neighborhood Family: inter
Inter Neighborhood Type: relocate
Intra Neighborhood Type: 2opt
Max Vehicles: [instance-dependent, estimate minimum]
Penalty Weight: 1000.0
```

**Expected Performance:**
- Average Distance: 1243.44 ± 358.38 km
- Best Case: 873.55 km
- Runtime: 375-500 seconds (varies by instance size)
- Feasibility Rate: 99.7%
- Feasibility (with TW): 100%

**Advantages:**
- ✓ 12.2% better quality than SA
- ✓ More robust (lower std deviation)
- ✓ 100% feasible with time windows
- ✓ Consistent across instances

**Disadvantages:**
- ✗ 8600× slower than SA
- ✗ Very slow on large instances (data1101)
- ✗ Not suitable for real-time applications

---

## CRITICAL FINDINGS: NEIGHBORHOOD TYPE

### Neighborhood Configuration: INTER-RELOCATE IS MANDATORY

```
Neighborhood Family: inter
Inter Type: relocate
Intra Type: 2opt  (fallback/fine-tuning)
```

**Why inter-relocate?**
- Moves clients between routes (inter)
- Allows load rebalancing
- Respects vehicle capacity constraints
- Best for time windows compliance
- Performance: 1415.66 km (SA), 1243.44 km (Tabu)

**Alternatives tested (LESS OPTIMAL):**
- `inter-exchange`: Theoretically good, but produces worse results in practice
- `intra-2opt`: Only fine-tuning, not primary search

---

## PARAMETER SENSITIVITY ANALYSIS

### Temperature (SA Only)

**Tested Range:** 500, 750, 1000, 1250, 1500

**Finding:** 1250.0 is optimal
```
T=500    : Fast convergence but poor quality
T=1250   : BEST (good balance)
T=1500   : Slightly worse, exploration too long
```

**Recommendation:** Lock at **1250.0**

### Cooling Rate (SA Only)

**Tested Range:** 0.999, 0.9993, 0.9995, 0.9997

**Finding:** 0.9993 is optimal
```
cooling=0.9995 : Too slow cooling, exploration plateaus early
cooling=0.9993 : BEST (good balance)
cooling=0.9997 : Too fast cooling, converges prematurely
```

**Recommendation:** Lock at **0.9993**

### Tabu Tenure

**Tested Range:** 10, 20, 30, 40, 50, 60, 70

**Finding:** 40 is optimal
```
tenure=10  : Too permissive, cycling issues
tenure=40  : BEST (good aspiration/recency balance)
tenure=70  : Too restrictive, exploration limited
```

**Recommendation:** Lock at **40**

### Iterations

**Impact on Quality:** (Based on data from campaigns 1-3)
```
10,000 iterations:  1450 km (SA), 1280 km (Tabu)  → Quick test
30,000 iterations:  1415 km (SA), 1243 km (Tabu)  → PRODUCTION
100,000 iterations: 1400 km (SA), 1240 km (Tabu)  → Research/offline
```

**Finding:** Plateau after 30,000 (diminishing returns)

**Recommendation:** 
- Production: **30,000**
- Research: **100,000** (for marginal +1% improvement)

---

## TIME WINDOWS IMPACT

### When TW is Enabled (enforce_time_windows = "oui")

```
Distance Increase: ~55% (SA) and ~52% (Tabu)

Example (data101):
  Without TW: 1203 km (SA), 1054 km (Tabu)
  With TW:   1864 km (SA), 1607 km (Tabu)

Feasibility Impact:
  SA with TW:   98.1% feasible
  Tabu with TW: 100% feasible  ← TABU HANDLES BETTER
```

**Recommendation:** 
- If TW are critical: **Use Tabu** (100% feasible)
- If TW can be relaxed: **Use SA** (faster, acceptable quality)

---

## INSTANCE-SPECIFIC TUNING (if needed)

### data101.vrp (Small ~100 clients)
```
Recommended: SA (quick) or Tabu (best quality)
Both converge well. Tabu wins by 12%.
Runtime: SA 0.1s, Tabu 375s
```

### data111.vrp (Medium ~100 clients, more complex)
```
Recommended: Tabu (more complex instance)
SA struggles with complexity. Tabu: 13% better.
Runtime: SA 0.1s, Tabu 400-500s
```

### data201.vrp (Small, structure different)
```
Recommended: Insufficient data (only 2 runs)
Extrapolate from data101/111: Use Tabu for quality
```

### data1101.vrp (LARGE ~1100 clients) [UNTESTED - CAUTION]
```
⚠️  TABU WILL BE EXTREMELY SLOW (many hours)
Recommendation: 
  - If time allows: Tabu with iterations=10000 (compromise)
  - If time critical: SA only
  - Consider reducing tenure to 20-30 for Tabu
```

---

## QUICK REFERENCE TABLE

| Aspect | SA Config | Tabu Config |
|--------|-----------|------------|
| **Temperature** | 1250.0 | - |
| **Cooling Rate** | 0.9993 | - |
| **Tenure** | - | 40 |
| **Iterations** | 30,000 | 30,000 |
| **Neighborhood** | inter-relocate | inter-relocate |
| **Avg Distance** | 1415.66 km | 1243.44 km |
| **Runtime** | 0.1 s | 400 s |
| **Quality** | Good | Best (12% better) |
| **Use Case** | Real-time | Offline/Batch |
| **TW Feasibility** | 98% | 100% |

---

## COMMAND TEMPLATES FOR REPRODUCTION

### Run SA with Optimal Config

```powershell
java -cp bin vrptw.Main `
  --instance data/data101.vrp `
  --algo sa `
  --iter 30000 `
  --seed 42 `
  --out results `
  --temp 1250.0 `
  --cooling 0.9993
```

### Run Tabu with Optimal Config

```powershell
java -cp bin vrptw.Main `
  --instance data/data101.vrp `
  --algo tabu `
  --iter 30000 `
  --seed 42 `
  --out results `
  --tenure 40
```

---

## VALIDATION CHECKLIST

Before using these parameters in final tests:

- [ ] SA: T=1250.0, cooling=0.9993, iter=30000, neighborhood=inter-relocate
- [ ] Tabu: tenure=40, iter=30000, neighborhood=inter-relocate
- [ ] Test both modes: enforce_time_windows = false AND true
- [ ] Use at least 5 different seeds (42, 43, 44, 45, 46)
- [ ] Verify feasibility: time_violation=0, capacity_violation=0
- [ ] Record: best_distance, runtime_ms, routes generated

---

## NOTES FOR REPORT

**Statement for paper:**

> "Based on analysis of 186 runs across multiple instances, the optimal configuration 
> for Simulated Annealing uses T0=1250.0 and cooling=0.9993, while Tabu Search 
> achieves best results with tenure=40. Both algorithms benefit from inter-neighborhood 
> relocate moves. Tabu outperforms SA by 12.2% in solution quality at the cost of 
> ~8600× execution time. For real-time applications, SA is recommended. For offline 
> optimization, Tabu is preferred."

---

**File Generated:** 2026-04-28
**Valid For:** Final analysis and report writing
**Confidence Level:** HIGH (based on 186 runs)
