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
- Si bloqué tôt: tester voisinage mixed.

### Tabu

- Si cycles fréquents / stagnation rapide: augmenter tenure.
- Si exploration trop contrainte et lente: diminuer tenure.
- Si amélioration faible: tester voisinage mixed ou plus diversifié.

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
