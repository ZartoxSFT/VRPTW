# Campaign 3 : Analyse Complète avec Fenêtres de Temps

## Vue d'ensemble

La **Campagne 3** est conçue pour générer des données suffisantes et fiables pour une **discussion approfondie** sur les résultats de SA et Tabu. Elle corrige toutes les lacunes identifiées des Campagnes 1 et 2.

### Améliorations par rapport aux campagnes précédentes

| Critère | Campagne 1 | Campagne 2 | Campagne 3 |
|---------|-----------|-----------|-----------|
| **Seeds** | 1 par algo | 1 par algo | **10** (robustesse stats) |
| **Instances** | 1 (data101) | 1 (data101) | **3** (data101, data111, data1101) |
| **Modes TW** | Non seulement | Non seulement | **Avec + Sans** |
| **Itérations variables** | 30k fixe | 30k fixe | **10k, 30k, 100k** |
| **Objectif** | Baseline | Tuning | **Analyse robuste + plateau** |
| **Nombre de runs** | ~5 | 1800 | ~360 |

---

## Configuration de Campagne 3

### Paramètres fixes (basés sur l'analyse des campagnes précédentes)

```
Simulated Annealing:
  - Température initiale (T0): 1250.0
  - Cooling rate: 0.9993
  - Voisinage: inter - relocate

Tabu Search:
  - Tabu tenure: 40
  - Voisinage: inter - relocate
  
Penalty weight: 1000.0
```

### Dimension de balayage

**Instances (3):**
- `data101.vrp` - Small/référence (~100-150 clients)
- `data111.vrp` - Medium (~100 clients, probablement plus complexe)
- `data1101.vrp` - Large/difficile (~1100+ clients)

**Seeds (10):**
- 41, 42, 43, 44, 45, 101, 102, 103, 104, 105
- Permet calcul de moyenne, médiane, écart-type fiables

**Modes TW (2):**
- `non` - Sans fenêtres de temps
- `oui` - Avec fenêtres de temps

**Itérations (3):**
- 10 000 - Budget petit/rapide
- 30 000 - Budget standard (base des campagnes 1-2)
- 100 000 - Budget large (pour voir plateau de convergence)

**Algorithmes (2):**
- SA (Simulated Annealing)
- Tabu (Tabu Search)

### Calcul du nombre de runs

```
3 instances × 10 seeds × 2 modes TW × 2 algos × 3 itérations
= 3 × 10 × 2 × 2 × 3
= 360 runs

Temps estimé:
- SA: ~150 ms par run → 360 × 150ms ÷ 1000 ≈ 54 secondes pour tous les SA
- Tabu: ~400-500s par run (beaucoup plus coûteux) → énorme
```

**⚠️ Tabu sera très long pour les grandes instances (data1101).** Possible que vous voudriez réduire les itérations pour Tabu sur data1101.

---

## Mode d'exécution

### Compilation (si pas déjà compilé)

```powershell
javac --release 21 -d bin src/vrptw/*.java
```

### Lancer la Campagne 3

#### Option 1: Configuration par défaut (itérations = 10k, 30k, 100k)

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run_campaign3.ps1
```

#### Option 2: Itérations réduites (test rapide, ~30 minutes)

```powershell
.\run_campaign3.ps1 -Iterations "10000,30000"
```

#### Option 3: Seul SA (si Tabu est trop lent)

Vous pouvez modifier le script manuellement pour tester uniquement SA d'abord.

#### Option 4: Avec skip de compilation

```powershell
.\run_campaign3.ps1 -SkipCompile
```

---

## Fichiers générés

### Plan et progression

**`campaign3_plan_YYYYMMDD_HHMMSS.csv`**

Contient la liste complète des 360 runs à exécuter avec tous les paramètres.

Colonnes:
- `instance` - Fichier VRP
- `seed` - Graine aléatoire
- `enforce_time_windows` - "oui" ou "non"
- `algo` - "sa" ou "tabu"
- `iterations` - Budget itératif
- `neighborhood_family` - "inter"
- `inter_type` - "relocate"
- `sa_initial_temp` - 1250.0
- `sa_cooling_rate` - 0.9993
- `tabu_tenure` - 40
- `penalty_weight` - 1000.0

**`campaign3_progress_YYYYMMDD_HHMMSS.csv`**

Mis à jour en direct pendant l'exécution. Permet de suivre l'avancement.

Colonnes:
- `timestamp` - Heure d'exécution
- `status` - "ok" ou "fail"
- `index` - Numéro de run
- `total` - 360
- `instance`, `seed`, `algo`, `iterations`, `enforce_time_windows`
- `message` - Erreur si fail

### Résultats détaillés

Les runs génèrent automatiquement des fichiers dans :
- `resultsSA/Exp{i}/executions_log.csv`
- `resultTABU/Exp{i}/executions_log.csv`

Chaque `executions_log.csv` contient:
- `best_objective` - Valeur finale
- `best_distance` - Distance minimale trouvée
- `time_violation`, `capacity_violation` - Faisabilité
- `runtime_ms` - Temps d'exécution
- `solutions_evaluated` - Nombre de solutions générées
- `routes`, `generated_relocate`, `generated_swap`, `generated_2opt` - Détails

---

## Analyse après exécution

### 1️⃣ Consolidation des données

Une fois Campagne 3 terminée, consolider tous les logs:

```python
# Pseudocode Python
import pandas as pd
import glob

sa_logs = glob.glob("resultsSA/Exp*/executions_log.csv")
tabu_logs = glob.glob("resultTABU/Exp*/executions_log.csv")

sa_df = pd.concat([pd.read_csv(f) for f in sa_logs])
tabu_df = pd.concat([pd.read_csv(f) for f in tabu_logs])

# Ajouter colonne algo
sa_df['algorithm'] = 'SA'
tabu_df['algorithm'] = 'TABU'

# Consolidation
all_results = pd.concat([sa_df, tabu_df])
all_results.to_csv('campaign3_consolidated_results.csv')
```

### 2️⃣ Analyses clés à faire

**Par configuration (instance + mode TW + algo + itérations):**

```
Calcul de:
- Moyenne de best_distance
- Écart-type
- Min, Max, Médiane
- Taux faisabilité (count où violations=0 / total)
- Runtime moyen
- Stddev du runtime
```

**Questions à répondre:**

1. **Qualité de SA vs Tabu :**
   - Sur chaque instance, lequel donne les meilleures solutions?
   - La conclusion change-t-elle en mode TW?

2. **Impact de TW :**
   - Le nombre de véhicules augmente-t-il?
   - L'écart entre SA et Tabu se réduit-il?

3. **Plateau de convergence :**
   - À partir de combien d'itérations n'améliore-t-on plus?
   - SA plateau-t-il plus tôt que Tabu?

4. **Robustesse (10 seeds):**
   - Quel algo a la meilleure stabilité (plus faible stddev)?
   - Existe-t-il des outliers?

5. **Complexité instance :**
   - data101 vs data111 vs data1101: tendances?
   - Un algo scalabilité mieux que l'autre?

### 3️⃣ Tableaux pour le rapport

**Tableau A – Résultats moyens par instance et mode TW**

| Instance | Mode TW | Algo | Iter=10k | Iter=30k | Iter=100k |
|---|---|---|---:|---:|---:|
| data101 | non | SA | 1850±45 | 1820±30 | 1805±20 |
| data101 | non | Tabu | 1780±50 | 1750±25 | 1740±15 |
| ... | ... | ... | ... | ... | ... |

**Tableau B – Taux de faisabilité**

| Instance | Mode TW | Algo | Iter=10k | Iter=30k | Iter=100k |
|---|---|---|---:|---:|---:|
| data101 | oui | SA | 70% | 85% | 95% |
| ... | ... | ... | ... | ... | ... |

**Tableau C – Impact de TW (différence distance)**

| Instance | Algo | Δ distance (TW-noTW) | Δ vehicles |
|---|---|---:|---:|
| data101 | SA | +12% | +1 |
| data111 | SA | +8% | +0 |
| ... | ... | ... | ... |

### 4️⃣ Graphiques

**Figure 1 : Boîtes à moustaches (distance par config)**

```
Pour chaque (instance, mode TW, algo):
  Box plot de 10 valeurs (une par seed)
  Montrer médiane, Q1/Q3, min/max
```

**Figure 2 : Ligne d'itérations vs qualité**

```
Pour chaque (instance, mode TW):
  Deux courbes: SA (bleu) et Tabu (rouge)
  X-axis: iterations (10k, 30k, 100k)
  Y-axis: average best_distance
  Visible: où est le plateau?
```

**Figure 3 : Impact TW**

```
Bar plot comparatif:
- Gauche: sans TW (gris)
- Droite: avec TW (bleu)
- Pour chaque instance et algo
```

---

## Points stratégiques pour la discussion du rapport

Une fois les 360 runs terminés et analysés, vous pouvez écrire :

✅ **"Nous avons testé sur 3 instances de complexités différentes (100, 100, 1100 clients)..."**

✅ **"Avec 10 exécutions par configuration, les intervalles de confiance montrent..."**

✅ **"L'ajout de fenêtres de temps augmente la distance de [X]% en moyenne..."**

✅ **"Au-delà de 30 000 itérations, le gain pour SA est [Y]%, pour Tabu [Z]%..."**

✅ **"L'algorithme X est **[Z]% plus robuste** (écart-type [A] vs [B]) sur l'ensemble des instances."**

✅ **"En mode sans TW, [algo] gagne de [K]%, mais en mode avec TW, l'écart diminue à [K']%..."**

---

## Pièges à éviter

❌ **Trop attendre d'une seule itération** (10k ou 30k): comparer les trois!

❌ **Ignorer les fenêtres de temps** (elles changent TOUT le contexte du problème)

❌ **Comparer SA et Tabu sans budget calcul équivalent** (Tabu est très coûteux!)

❌ **Croire qu'une seule seed suffit** (variabilité importante en métaheuristiques)

❌ **Conclure sur une seule instance** (les résultats peuvent ne pas généraliser)

---

## Commandes utiles après exécution

### Vérifier que tous les runs ont réussi

```powershell
$progress = Import-Csv "campaign3_progress_*.csv"
$progress | Where-Object {$_.status -eq "fail"} | Measure-Object
```

### Compter les runs par instance

```powershell
$plan = Import-Csv "campaign3_plan_*.csv"
$plan | Group-Object instance | Select-Object Name, Count
```

### Vérifier la structure des résultats

```powershell
Get-ChildItem resultsSA/Exp*/executions_log.csv | Measure-Object
Get-ChildItem resultTABU/Exp*/executions_log.csv | Measure-Object
```

---

## Résumé des gains de Campagne 3

| Aspect | Gain |
|--------|------|
| **Robustesse stats** | 10 seeds → intervalles de confiance fiables |
| **Généralisation** | 3 instances → pas seulement sur data101 |
| **Contexte réaliste** | Modes TW testés → impact étudié |
| **Analyse plateau** | 3 budgets itératifs → tradeoff visible |
| **Justification rapport** | Données massives → rapport "excellent" |

---

Prêt à lancer ! 🚀
