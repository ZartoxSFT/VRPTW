# To-Do tests uniquement (code déjà terminé)

Objectif: exécuter des tests propres, comparer SA vs Tabu, et produire une analyse claire pour le rapport.

## 1) Préparer le protocole de test

- [ ] Définir la liste d'instances à tester (petites, moyennes, grandes).
- [ ] Définir les seeds fixes pour la reproductibilité (ex: 10 seeds).
- [ ] Définir le budget de calcul identique pour SA et Tabu (itérations, timeout éventuel).
- [ ] Définir les paramètres testés pour SA (température initiale, cooling rate, voisinage).
- [ ] Définir les paramètres testés pour Tabu (neighborhood size, tabu tenure, voisinage).
- [ ] Définir deux modes d'évaluation séparés:
  - [ ] sans fenêtres de temps,
  - [ ] avec fenêtres de temps.

## 2) Vérifier le nombre minimal de véhicules (par instance)

- [ ] Exécuter l'estimation du minimum sans TW.
- [ ] Exécuter l'estimation du minimum avec TW.
- [ ] Créer un tableau de synthèse par instance:
  - [ ] borne inférieure capacité,
  - [ ] minimum faisable sans TW,
  - [ ] minimum faisable avec TW.
- [ ] Pour les campagnes finales, fixer maxVehicles au minimum estimé du mode testé.

## 3) Campagne de tests SA

- [ ] Lancer toutes les combinaisons de paramètres SA prévues.
- [ ] Faire plusieurs runs par combinaison (via seeds définies).
- [ ] Exporter pour chaque run:
  - [ ] distance finale,
  - [ ] faisabilité (time/capacity/vehicle violations),
  - [ ] nombre de véhicules final,
  - [ ] temps d'exécution,
  - [ ] nombre de solutions évaluées,
  - [ ] historique de convergence.

## 4) Campagne de tests Tabu

- [ ] Lancer toutes les combinaisons de paramètres Tabu prévues.
- [ ] Faire plusieurs runs par combinaison (mêmes seeds que SA si possible).
- [ ] Exporter les mêmes métriques que SA pour comparaison équitable.

## 5) Tests de sensibilité (important pour l'analyse)

- [ ] Sensibilité SA:
  - [ ] effet de la température initiale,
  - [ ] effet du cooling rate,
  - [ ] effet du type de voisinage.
- [ ] Sensibilité Tabu:
  - [ ] effet de neighborhood size,
  - [ ] effet de tabu tenure,
  - [ ] effet du type de voisinage.
- [ ] Comparer mixed vs voisinages simples (relocate/swap/2-opt).

## 6) Contrôles qualité des résultats

- [ ] Vérifier qu'aucun client n'est oublié.
- [ ] Vérifier qu'aucun client n'est dupliqué.
- [ ] Vérifier la cohérence dépôt départ/retour sur chaque route.
- [ ] Vérifier la cohérence faisabilité annoncée vs pénalités mesurées.
- [ ] Vérifier la stabilité des résultats (moyenne, écart-type sur seeds).

## 7) Tableaux et graphes pour le rapport

- [ ] Tableau 1: minimum véhicules par instance (sans TW / avec TW).
- [ ] Tableau 2: meilleure distance SA vs Tabu par instance.
- [ ] Tableau 3: moyenne et écart-type SA vs Tabu (qualité + temps).
- [ ] Tableau 4: influence des paramètres (sensibilité).
- [ ] Figure 1: courbes de convergence représentatives.
- [ ] Figure 2: visualisations de tournées finales (quelques cas clés).

## 8) Analyse à rédiger dans le rapport

- [ ] Expliquer le protocole de test (pour reproductibilité).
- [ ] Justifier les choix de paramètres retenus.
- [ ] Discuter qui gagne entre SA et Tabu selon:
  - [ ] qualité,
  - [ ] temps,
  - [ ] robustesse.
- [ ] Identifier les cas difficiles (TW serrées, instances grandes).
- [ ] Donner les limites de l'étude et pistes d'amélioration.

## 9) Vérification finale avant rendu

- [ ] Rejouer un sous-ensemble de tests pour vérifier la reproductibilité.
- [ ] Vérifier que tous les tableaux/chiffres du rapport viennent de logs archivés.
- [ ] Vérifier la cohérence des unités et légendes (distance, ms, itérations).
- [ ] Vérifier orthographe, clarté, et conclusion.
