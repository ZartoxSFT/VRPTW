# Checklist rapport — VRPTW (Recuit simulé & Recherche Tabou)

## 1) Objectif du rapport
Comparer deux métaheuristiques pour le VRPTW :
- Recuit simulé (SA)
- Recherche Tabou (TS)

Objectifs de comparaison :
- Qualité de solution (distance)
- Faisabilité (contraintes capacité/fenêtres temporelles)
- Temps de calcul
- Stabilité entre plusieurs exécutions (seeds)

---

## 2) Travail expérimental à réaliser
- Exécuter SA et TS sur toutes les instances : `data101.vrp` à `data202.vrp`.
- Faire plusieurs runs par instance (recommandé : 5 seeds).
- Exporter pour chaque run :
  - distance finale,
  - violations (`timeV`, `capV`),
  - nombre de tournées,
  - temps d’exécution,
  - image des tournées (`*_routes.png`),
  - courbe d’historique (`*_history.png`) + CSV (`*_history.csv`).

---

## 3) Paramètres recommandés (point de départ)
Utiliser un budget équitable entre algorithmes :
- `iter = 30000`
- `penaltyWeight = 1000`

### Recuit simulé (SA)
- `T0 = 2500`
- `coolingRate = 0.9995`
- `iter = 30000`
- `seed = 42`

### Recherche Tabou (TS)
- `neighborhoodSize = 40`
- `tabuTenure = 25`
- `iter = 30000`
- `seed = 49`

### Seeds recommandées (stabilité)
- `42, 43, 44, 45, 46`

---

## 4) Calibration (tuning) à inclure dans le rapport
Faire un mini-tuning sur 2 instances représentatives (une plus contrainte, une plus souple), puis figer les paramètres globaux.

### Grille SA
- `T0 ∈ {1000, 2500, 5000}`
- `coolingRate ∈ {0.999, 0.9995, 0.9997}`

### Grille TS
- `tabuTenure ∈ {10, 25, 40}`
- `neighborhoodSize ∈ {20, 40, 80}`

Critère de choix : meilleur compromis qualité/temps en moyenne.

---

## 5) Justification du jeu de données (texte à reprendre)
- Les instances sont homogènes en format VRPTW, ce qui permet une comparaison cohérente.
- Le lot contient plusieurs profils de difficulté (fenêtres temporelles et capacité différentes selon les instances).
- Utiliser tout le lot limite le biais lié à une instance unique.
- La taille (100 clients) est suffisante pour évaluer des métaheuristiques en conditions réalistes.

---

## 6) Indicateurs à présenter (tableaux)
Pour chaque instance et chaque algorithme, rapporter :
- Distance totale (`distance`)
- Violation temporelle (`timeV`)
- Violation de capacité (`capV`)
- Nombre de tournées (`routes`)
- Temps d’exécution (`runtimeMs`)

Sur les 5 seeds, ajouter :
- moyenne,
- écart-type,
- meilleur,
- pire.

---

## 7) Plan d’analyse recommandé
- Comparer SA vs TS sur la qualité finale des solutions.
- Vérifier la faisabilité (idéalement `timeV = 0` et `capV = 0`).
- Comparer vitesse de convergence via `*_history.png`.
- Analyser la sensibilité aux paramètres (résultats du tuning).
- Commenter les tournées via `*_routes.png` (cohérence géographique, croisements, retours dépôt).

---

## 8) Trame de sections pour le rapport
1. Introduction (VRPTW + objectif)
2. Modélisation (variables, contraintes, objectif)
3. Méthodes (SA, TS, voisinages, pénalisation)
4. Protocole expérimental (instances, seeds, paramètres, machine)
5. Résultats (tableaux + figures)
6. Discussion (forces/faiblesses, sensibilité)
7. Conclusion et perspectives

---

## 9) Vérifications finales avant rendu
- Même budget d’itérations pour SA et TS.
- Même jeu d’instances pour les deux méthodes.
- Plusieurs seeds exécutées.
- Résultats agrégés (moyenne/écart-type).
- Figures lisibles et référencées dans le texte.
- Interprétation des résultats (pas seulement des chiffres).
