# CAMPAIGN 3 ANALYSIS REPORT - Final Results (WITH TIME WINDOWS ONLY)

**Date:** April 28, 2026  
**Total Runs (WITH TW):** 106 runs (out of 186 total)  
**Instances:** data101, data111, data201  
**Focus:** Time-windowed vehicle routing (realistic constraints)

---

## Executive Summary

Campagne 3 a généré **106 runs avec fenêtres temporelles** sur 3 instances VRPTW réalistes. Les résultats montrent que :

- **TABU surpasse SA** en qualité moyenne
- **data111 est plus facile** que data101  
- **Convergence:** SA s'améliore avec + d'itérations (100k meilleur); TABU converge tôt (10k acceptable)
- **Optimum trouvé:** 1136.01 km (SA, data111, 14 routes)

---

## 1. RÉSULTATS GLOBAUX (AVEC FENÊTRES DE TEMPS)

### Statistiques Globales


**SA**
- Meilleure distance: 1136.01 km
- Distance moyenne: 1864.87 km (±384.71)
- Pire distance: 2529.81 km
- Nombre de runs: 53

**TABU**
- Meilleure distance: 1176.15 km
- Distance moyenne: 1607.01 km (±273.27)
- Pire distance: 1827.52 km
- Nombre de runs: 53

---

## 2. PERFORMANCE PAR INSTANCE (AVEC TW)


### data101.vrp

**SA**
- Meilleure distance: 1820.51 km
- Nombre de routes: 23
- Distance moyenne: 2073.39 km (±235.01)
- Seed: 66571993098
- Runtime (best): 227 ms

**TABU**
- Meilleure distance: 1741.09 km
- Nombre de routes: 21
- Distance moyenne: 1791.72 km (±28.71)
- Seed: 15032385634
- Runtime (best): 128619 ms


### data111.vrp

**SA**
- Meilleure distance: 1136.01 km
- Nombre de routes: 14
- Distance moyenne: 1420.29 km (±242.31)
- Seed: 66571993101
- Runtime (best): 199 ms

**TABU**
- Meilleure distance: 1176.15 km
- Nombre de routes: 15
- Distance moyenne: 1209.85 km (±31.92)
- Seed: 15032385570
- Runtime (best): 105469 ms


### data201.vrp

**SA**
- Meilleure distance: 1471.77 km
- Nombre de routes: 11
- Distance moyenne: 1471.77 km (±nan)
- Seed: 66571993098
- Runtime (best): 97 ms

**TABU**
- Meilleure distance: 1311.90 km
- Nombre de routes: 14
- Distance moyenne: 1311.90 km (±nan)
- Seed: 15032385570
- Runtime (best): 566007 ms


---

## 3. MEILLEURE SOLUTION GLOBALE (AVEC TW)

### SA

```
Instance:         data111.vrp
Distance:         1136.01 km
Routes:           14
Runtime:          199 ms
Seed:             66571993101
```

### TABU

```
Instance:         data111.vrp
Distance:         1176.15 km
Routes:           15
Runtime:          105469 ms
Seed:             15032385570
```


---

## 4. CONVERGENCE AVEC ITÉRATIONS

| Itérations | SA Moyenne | SA Min | TABU Moyenne | TABU Min |
|-----------|-----------|--------|-------------|----------|
| 10,000 | 2204.26 | 1661.25 | 1597.07 | 1176.15 |
| 30,000 | 1782.24 | 1275.81 | 1619.98 | 1176.15 |
| 100,000 | 1652.20 | 1136.01 | 1597.07 | 1176.15 |

✓ **SA bénéficie d'itérations longues** (convergence progressive)
✓ **TABU converge vite** (10k-30k suffisent)


---

## 5. RECOMMANDATIONS

### Pour Amélioration
1. **Augmenter pénalité véhicule:** `penalty_weight = 100000` (vs 1000) pour minimiser agressivement
2. **Forcer véhicules:** `MaxVehicles = 19` pour reproduire solution prof
3. **Recherche bi-étape:** (1) min véhicules, (2) min distance à véhicules fixés

### Paramètres Confirmés
- **SA:** 100k itérations, T=1250, cooling=0.9993, inter_relocate
- **TABU:** 30k itérations, tenure=40, inter_relocate

---

## DONNÉES EXTRAITES

✓ 106 runs WITH time windows  
✓ 3 instances (data101, data111, data201)  
✓ 2 algorithmes (SA, TABU)  
✓ Convergence analysis (10k, 30k, 100k itérations)
