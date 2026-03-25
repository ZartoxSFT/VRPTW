timestamp
Date et heure du lancement (format AAAA-MM-JJ HH:MM:SS).

instance
Nom du fichier d’instance VRPTW utilisé (ex: data101.vrp).

algorithm
Algorithme exécuté (sa ou tabu).

best_objective
Meilleure valeur de la fonction objectif trouvée pendant le run.
C’est la valeur optimisée (distance + pénalités selon ton évaluateur).

best_distance
Distance totale de la meilleure solution (sans les autres composantes de pénalité).

time_violation
Violation cumulée des fenêtres temporelles sur la meilleure solution.
0 = aucune violation de temps.

capacity_violation
Violation cumulée de capacité véhicule.
0 = toutes les tournées respectent la capacité.

routes
Nombre de tournées (véhicules utilisés) dans la meilleure solution.

runtime_ms
Temps total d’exécution de l’algorithme, en millisecondes.

solutions_evaluated
Nombre total de solutions évaluées par l’algorithme pendant ce run.

generated_relocate
Nombre de voisins générés avec le mouvement relocate.

generated_swap
Nombre de voisins générés avec le mouvement swap.

generated_noop
Nombre de voisins de type noop (aucun mouvement effectif).

penalty_weight
Poids de pénalité utilisé dans l’évaluateur pour contraindre les violations.

enforce_time_windows
Booléen indiquant si les fenêtres temporelles sont activées (true/false).

parameters
Résumé texte des paramètres spécifiques au run/algo (itérations, seed, température, cooling rate, tenure, etc.).