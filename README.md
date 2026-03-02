# VRPTW

Implémentation Java de 2 métaheuristiques pour VRPTW :
- Recuit simulé (`sa`)
- Recherche tabou (`tabu`)

Le projet exporte :
- l'historique d'amélioration (`.csv` + `.png`)
- une image des tournées finales (`.png`)

## Structure

- `data/` : instances `.vrp`
- `src/vrptw/` : code Java
- `results/` : sorties générées

## Compilation

```powershell
javac -d out src/vrptw/*.java
```

## Exécution

```powershell
java -cp out vrptw.Main --instance data/data101.vrp --algo both --iter 30000 --seed 42 --out results
```

Paramètres principaux :
- `--instance` : chemin de l'instance `.vrp`
- `--algo` : `sa`, `tabu` ou `both`
- `--iter` : nombre d'itérations
- `--seed` : graine aléatoire
- `--out` : dossier de sortie

## Fichiers de sortie

Pour chaque algo, les fichiers suivants sont générés :
- `*_history.csv`
- `*_history.png`
- `*_routes.png`
