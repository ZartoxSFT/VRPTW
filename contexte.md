# Contexte de travail – Projet VRPTW

## 1) Rappel du cadre du projet

- Projet en groupe de 2 étudiants maximum.
- Date limite: lundi 4 mai à 07h00.
- Pénalité: 5 points en moins par jour de retard.
- Objectif global: résoudre le VRPTW avec 2 métaheuristiques et comparer leurs performances.

## 2) Résumé du problème VRPTW

- Chaque tournée commence et se termine au dépôt (client 0).
- Chaque client est visité une seule fois par un seul véhicule.
- Tous les véhicules ont la même capacité C.
- La demande client ne doit pas dépasser la capacité disponible dans la tournée.
- Les fenêtres de temps peuvent être ignorées (mode sans TW) ou imposées (mode avec TW).
- Le nombre de véhicules n’est pas limité dans l’énoncé, mais le projet demande d’estimer le minimum nécessaire.
- Objectif principal: minimiser la distance totale parcourue.

## 3) Exigences à couvrir dans le rapport

1. Modélisation du problème et structure logicielle.
2. Détermination du nombre minimal de véhicules, d’abord sans TW, puis avec TW.
3. Générateur aléatoire de solutions initiales.
4. Implémentation de 2 métaheuristiques, protocole de test clair, analyse des résultats.
5. Comparaison des algorithmes en temps, qualité, nombre de solutions générées, impact voisinages et paramètres.
6. Bonus optionnel: étude de limite de résolution via programmation linéaire.

## 4) Ce que je dois recevoir après tes campagnes de tests

Pour que je rédige une analyse solide et discute les résultats, j’ai besoin des données suivantes:

- Les fichiers executions_log.csv de chaque campagne.
- Les historiques de convergence (fichiers *_history.csv) pour des runs représentatifs.
- Le mapping clair des paramètres utilisés par campagne:
  - instance
  - algo
  - seed
  - itérations
  - mode TW (on/off)
  - poids de pénalité
  - voisinage
  - paramètres SA (température initiale, cooling rate)
  - paramètres Tabu (tenure)
- Le tableau du minimum de véhicules estimé:
  - borne capacité
  - minimum sans TW
  - minimum avec TW
- Idéalement 2 à 4 captures ou exports de tournées finales par cas intéressant.

## 5) Variables déjà prévues pour la campagne en cours

Balayage demandé:

- SA, température initiale: 500, 750, 1000, 1250, 1500
- Tabu, taille de liste tabou (tenure): 10, 20, 30, 40, 50

Recommandation minimum pour fiabilité statistique:

- Même protocole pour SA et Tabu (itérations, seed set, instances, mode TW).
- Au moins 10 seeds par configuration.
- Deux campagnes séparées:
  - Campagne A: sans fenêtres de temps
  - Campagne B: avec fenêtres de temps

## 6) Dictionnaire des colonnes de log à exploiter

Colonnes clés attendues dans executions_log.csv:

- timestamp
- instance
- algorithm
- best_objective
- best_distance
- time_violation
- capacity_violation
- routes
- runtime_ms
- solutions_evaluated
- generated_relocate
- generated_swap
- generated_noop
- penalty_weight
- enforce_time_windows
- parameters

Remarque:

- Une solution est considérée faisable si time_violation = 0 et capacity_violation = 0, avec contrainte véhicules respectée.

## 7) Tableaux à remplir pour le rapport

### Tableau A – Minimum de véhicules

| Instance | Borne capacité | Min véhicules sans TW | Min véhicules avec TW | Commentaire |
|---|---:|---:|---:|---|
| data101 |  |  |  |  |
| data102 |  |  |  |  |

### Tableau B – Résultats bruts par configuration

| Instance | Mode TW | Algo | Paramètre clé | Seed | Distance | Objectif | Faisable (0/1) | Routes | Temps (ms) | Sol. évaluées |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| data101 | off | sa | T0=500 | 42 |  |  |  |  |  |  |

### Tableau C – Agrégation par configuration

| Instance | Mode TW | Algo | Paramètre clé | Dist. moyenne | Dist. écart-type | Meilleure dist. | Taux faisable | Temps moyen (ms) | Sol. évaluées moyennes |
|---|---|---|---|---:|---:|---:|---:|---:|---:|
| data101 | off | sa | T0=500 |  |  |  |  |  |  |

### Tableau D – Comparaison SA vs Tabu (paramètres retenus)

| Instance | Mode TW | SA (best) | Tabu (best) | Gagnant qualité | SA temps moyen | Tabu temps moyen | Gagnant temps | Commentaire |
|---|---|---:|---:|---|---:|---:|---|---|
| data101 | off |  |  |  |  |  |  |  |

## 8) Graphiques à produire

- Courbes de convergence (best objective vs itération) pour des runs représentatifs.
- Boxplots ou barres d’erreur des distances finales par configuration.
- Barres comparatives du temps moyen d’exécution SA vs Tabu.
- Optionnel: distribution des violations (en mode avec TW) avant convergence finale.

## 9) Plan d’analyse statistique recommandé

Par configuration (instance, mode TW, algo, paramètre):

- moyenne, médiane, écart-type de best_distance
- minimum et maximum de best_distance
- taux de faisabilité
- moyenne de runtime_ms
- moyenne de solutions_evaluated

Comparaisons SA vs Tabu:

- comparaison sur mêmes seeds quand possible
- discussion en 3 axes:
  - qualité des solutions
  - coût de calcul
  - robustesse (variance, stabilité)

## 10) Guide de discussion des résultats (texte du rapport)

Questions à traiter explicitement:

1. Quel algo donne les meilleures distances selon les instances?
2. Cet avantage reste-t-il vrai en mode avec TW?
3. Quel algo est le plus stable (faible variance)?
4. Quel paramètre SA semble optimal selon la taille/complexité des instances?
5. Quel tenure Tabu donne le meilleur compromis qualité/temps?
6. Quel voisinage contribue le plus (au vu des compteurs generated_*)?
7. Observe-t-on des cas où plus de calcul ne donne pas mieux (plateau)?

## 11) Règles pour choisir les nouveaux paramètres après la 1re campagne

### SA

- Si convergence trop lente: augmenter T0 ou ralentir légèrement le refroidissement.
- Si exploration trop aléatoire et faible qualité finale: baisser T0 ou accélérer le refroidissement.
- Si bloqué tôt: comparer `relocate` et `exchange` (famille inter), puis vérifier le niveau intra avec `2opt`.

### Tabu

- Si cycles fréquents / stagnation rapide: augmenter tenure.
- Si exploration trop contrainte et lente: diminuer tenure.
- Si amélioration faible: comparer `relocate` et `exchange`, puis tester `2opt` en famille intra.

## 12) Plan concret de la suite (quand tu m’enverras les données)

Quand tu auras fini les runs, envoie:

1. Les chemins des dossiers de résultats utilisés.
2. Les CSV de logs consolidés (ou tous les executions_log.csv).
3. Les réglages exacts de campagne (itérations, seeds, TW on/off, pénalité).
4. Les 3 à 5 cas que tu trouves les plus intéressants.

Ensuite je ferai:

1. Nettoyage et structuration des données.
2. Analyse comparative complète SA vs Tabu.
3. Discussion argumentée des résultats.
4. Proposition d’un second plan de tests avec paramètres affinés.
5. Texte quasi final prêt à intégrer dans le rapport.

## 13) Checklist finale avant rendu

- Le protocole est reproductible.
- Les tableaux correspondent exactement aux données de logs.
- Les comparaisons SA vs Tabu sont faites à budget calcul comparable.
- Le minimum de véhicules est justifié sans TW puis avec TW.
- Les limites de l’étude sont expliquées.
- Les pistes d’amélioration sont proposées.

## 14) Campagne 1 (prête à lancer)

Objectif de la campagne 1:

- Obtenir une première base de comparaison SA vs Tabu.
- Mesurer l'impact du choix de famille de voisinage (inter ou intra) sans mélange dans un même run.

Règle importante du programme actuel:

- Un run choisit une seule famille de voisinage: `inter` ou `intra`.
- Si `inter`: seul le type inter est actif (`relocate` ou `exchange`).
- Si `intra`: seul le type intra est actif (`2opt`).

Commandes de lancement recommandées:

1. Campagne inter (premier passage):

```powershell
.\run_sweeps.ps1 -NeighborhoodFamily inter -InterNeighborhoodType relocate
```

2. Campagne intra (second passage):

```powershell
.\run_sweeps.ps1 -NeighborhoodFamily intra -IntraNeighborhoodType 2opt
```

Précondition:

```powershell
javac -d bin src/vrptw/*.java
```

## 15) Informations à consigner absolument dans les résultats

Pour chaque run, conserver explicitement:

- `neighborhoodFamily` (`inter` ou `intra`)
- `interNeighborhoodType` (si famille inter)
- `intraNeighborhoodType` (si famille intra)
- `algorithm` (`sa` ou `tabu`)
- paramètre balayé (`initialTemp` ou `tabuTenure`)
- `seed`, `iterations`, `enforce_time_windows`, `penalty_weight`

Pourquoi:

- Sans ces champs, impossible de justifier l'effet des structures de voisinage dans la discussion finale.

## 16) Lecture des résultats pour décider la suite

Après la campagne 1, appliquer cette grille:

1. Qualité: comparer meilleure distance et moyenne des distances.
2. Robustesse: comparer l'écart-type et le taux de faisabilité.
3. Coût: comparer runtime moyen et solutions évaluées.

Décision pour campagne 2:

- Garder la famille qui offre le meilleur compromis qualité/temps/stabilité.
- Affiner ensuite les paramètres de cette famille (ex: `relocate` vs `exchange` côté inter, `2opt` vs `mixed` côté intra).

## 17) Remarque d'interprétation

- Aucun mode `mixed` n'est utilisé dans la campagne d'analyse.
- Chaque run correspond à un choix explicite et traçable de voisinage.

## 18) Campagne 2 automatisée

Un script dédié est disponible:

- `run_campaign2.ps1`

Il produit automatiquement:

- un plan d'exécution `campaign2_plan_*.csv`
- un journal de progression `campaign2_progress_*.csv`

Modes de campagne:

- `comparison`: compare les structures de voisinage (inter relocate/exchange vs intra 2opt) avec paramètres de base.
- `tuning`: affine SA (température/refroidissement) et Tabu (tenure) autour des meilleurs réglages observés.
- `full`: enchaîne `comparison` + `tuning`.

Échelles:

- `quick`: 3 instances, 5 seeds.
- `full`: 6 instances, 10 seeds.

Commandes recommandées:

1. Démarrage rapide (validation protocole):

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run_campaign2.ps1 -Campaign comparison -Scale quick
```

2. Campagne complète comparaison + tuning:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run_campaign2.ps1 -Campaign full -Scale full
```

3. Tuning uniquement (si comparaison déjà faite):

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run_campaign2.ps1 -Campaign tuning -Scale full
```

Précondition compilation:

```powershell
javac --release 21 -d bin src/vrptw/*.java
```

## 19) Analyse des résultats disponibles à ce jour

Résumé global des logs présents dans `resultsSA` et `resultTABU`:

- Total analysé: 129 runs
- SA: 69 runs
- Tabu: 60 runs
- Répartition temporelle:
  - 2026-04-22: 23 runs
  - 2026-04-23: 106 runs

Faisabilité:

- Total faisables: 10/129 = 7.75%
- SA faisables: 5/69 = 7.25%
- Tabu faisables: 5/60 = 8.33%
- Tous les runs faisables observés sont en mode fenêtres de temps activées

Qualité moyenne:

- SA:
  - meilleure distance: 938.9378
  - distance moyenne: 1259.9936
  - écart-type: 303.2493
  - temps moyen: 85.391 ms
  - temps médian: 86 ms
- Tabu:
  - meilleure distance: 873.5485
  - distance moyenne: 1185.9301
  - écart-type: 332.9274
  - temps moyen: 1,444,540.750 ms
  - temps médian: 262,245 ms

Lecture immédiate:

- Tabu produit les meilleures distances mais reste très coûteux en temps.
- SA est très rapide, mais moins performant en distance.
- Le gain de distance de Tabu est réel, mais le coût temps est énorme.

Meilleurs réglages observés:

- SA: `initialTemp=1250.0`, `coolingRate=0.9993`, voisinage inter `relocate`
- Tabu: `tabuTenure=40`, voisinage inter `relocate`
- Tabu avec meilleure moyenne de distance: `tabuTenure=70`

Meilleure solution observée:

- Algo: Tabu
- Instance: `data101.vrp`
- distance: 873.5485
- runtime: 264,444 ms
- paramètre clé: `tabuTenure=40`

État de la campagne 2:

- Plan total généré: 1800 runs
- Progression observée: 119/1800
- Statut: campagne partielle, non terminée

## 20) Conclusion provisoire et suite recommandée

Ce que l'on peut déjà écrire dans le rapport:

- SA est la référence temps.
- Tabu est la référence qualité.
- Le voisinage inter `relocate` est le meilleur point de départ actuel.
- Le meilleur compromis SA observé est autour de `T0=1250` et `coolingRate=0.9993`.
- Le meilleur compromis Tabu observé est autour de `tenure=40`.

Ce qui manque encore pour un rapport final robuste:

- plusieurs instances supplémentaires (pas seulement `data101.vrp`)
- davantage de runs par configuration pour comparer la stabilité
- une vraie comparaison finale sur deux modes:
  - sans fenêtres de temps
  - avec fenêtres de temps

Recommandation pratique:

- On ne continue pas la campagne 2 complète telle quelle.
- On fait plutôt une campagne ciblée et plus courte sur 3 instances représentatives.
- Objectif de la suite: consolider les paramètres retenus et produire un rapport propre sans gonfler inutilement le nombre de runs.

Plan conseillé pour la suite:

1. Garder SA avec `T0=1250` et `coolingRate=0.9993`.
2. Garder Tabu avec `tenure=40`.
3. Tester sur 3 instances: une petite, une moyenne, une plus difficile.
4. Garder 5 seeds.
5. Comparer `TW off` et `TW on`.
6. Si on veut un deuxième voisinage à comparer, ajouter `exchange` côté inter, mais seulement sur un sous-ensemble plus réduit.

## 21) Campagne 3 ciblée (60 runs)

Objectif:

- Produire un jeu de résultats propre, équilibré et directement exploitable pour le rapport final.

Design retenu:

- 3 instances: `data101`, `data111`, `data201`
- 5 seeds: 41, 42, 43, 44, 45
- 2 modes TW: `non` et `oui`
- 2 algorithmes: SA et Tabu

Total:

- 3 x 5 x 2 x 2 = 60 runs

Paramètres figés:

- voisinage: famille `inter`, type `relocate`
- SA: `initialTemp=1250.0`, `coolingRate=0.9993`
- Tabu: `tabuTenure=40`

Script dédié:

- `run_campaign3_targeted.ps1`

Sorties générées:

- `campaign3_plan_*.csv`
- `campaign3_progress_*.csv`

Commandes de lancement:

```powershell
Set-Location "C:\Users\darkf\Desktop\Travail\VRPTW"
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
javac --release 21 -d bin src/vrptw/*.java
.\run_campaign3_targeted.ps1
```

Quand les 60 runs sont terminés:

- lancer l'analyse finale SA vs Tabu sur ce lot de 60 runs
- utiliser ce lot comme base principale du rapport
- garder les anciennes campagnes comme résultats exploratoires / pré-tests

## 22) Analyse 3e campagne disponible (lot diagnostic actuel)

Etat des fichiers d'analyse consolidés:

- `analysis_overall.csv`
- `analysis_by_instance_sa.csv`
- `analysis_by_instance_tabu.csv`
- `analysis_summary.json`

Périmètre réellement agrégé dans ces exports:

- SA: 5 runs
- Tabu: 5 runs
- Instance couverte: `data101.vrp`
- Faisabilité: 100% pour SA et 100% pour Tabu (tous les runs faisables)

Résultats globaux extraits:

- SA
  - meilleure distance: 1984.7421
  - distance moyenne: 2018.6619
  - distance médiane: 2012.2229
  - écart-type: 28.4356
  - runtime moyen: 122.6 ms
  - solutions évaluées (moyenne): 30001
- Tabu
  - meilleure distance: 1796.8156
  - distance moyenne: 1805.0394
  - distance médiane: 1806.1589
  - écart-type: 7.1799
  - runtime moyen: 417519 ms
  - solutions évaluées (moyenne): 352849380.6

Lecture comparative immédiate (3e analyse):

- Qualité: Tabu domine SA sur la meilleure distance et la moyenne.
- Stabilité: Tabu a une variance plus faible sur ce lot (écart-type plus petit).
- Coût calcul: SA est très largement plus rapide.
- Conclusion opérationnelle: compromis classique confirmé, Tabu pour la qualité, SA pour le temps.

Interprétation pour le rapport:

- Ces résultats sont exploitables comme bloc "diagnostic contrôlé" et illustrent très bien le trade-off qualité/temps.
- Comme l'agrégation courante est centrée sur `data101.vrp`, il faut conserver dans le rapport la distinction:
  - résultats exploratoires multi-instances (campagnes précédentes),
  - résultats diagnostic focalisés (ce lot de 10 runs),
  - puis résultats finaux de la campagne ciblée 60 runs quand consolidés.

Action recommandée juste avant rédaction finale:

1. Garder cette analyse 10 runs dans la section "validation rapide du protocole".
2. Lancer ou consolider la campagne ciblée 60 runs pour la section "résultats principaux".
3. Conclure avec la comparaison SA vs Tabu sur les deux axes: distance et temps.
