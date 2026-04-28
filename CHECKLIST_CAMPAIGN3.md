# VRPTW Campaign 3 - Checklist d'exécution

**Objectif:** Générer 360 runs robustes sur 3 instances avec fenêtres de temps activées.

**Durée estimée:** 1-3 heures (dépend de votre CPU et de la vitesse de Tabu)

**Date cible:** Avant le 4 mai 2026 ✅

---

## ✅ Pré-exécution (Avant de lancer)

- [ ] Vérifier que le répertoire `bin/` existe
  ```powershell
  Test-Path "bin"
  ```

- [ ] **Compiler le code Java** (si pas déjà compilé)
  ```powershell
  javac --release 21 -d bin src/vrptw/*.java
  ```

- [ ] Vérifier que les 3 instances existent
  ```powershell
  Test-Path "data/data101.vrp"
  Test-Path "data/data111.vrp"
  Test-Path "data/data1101.vrp"
  ```

- [ ] Créer les répertoires de résultats (s'ils n'existent pas)
  ```powershell
  mkdir -Force resultsSA
  mkdir -Force resultTABU
  ```

- [ ] Vérifier l'espace disque disponible (minimum 1 GB)
  ```powershell
  Get-PSDrive C | Select-Object @{Name="Free GB";Expression={[math]::Round($_.Free/1GB)}}
  ```

- [ ] Lire le README_CAMPAIGN3.md pour comprendre la stratégie
  - [ ] Configuration des paramètres
  - [ ] Dimension des balayages
  - [ ] Fichiers générés

---

## 🚀 Exécution (Lancer la campagne)

### Option A: Configuration complète (recommandée)

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run_campaign3.ps1
```

**Durée estimée:** 1-3 heures selon performance Tabu

- [ ] Script lancé
- [ ] Plan généré : `campaign3_plan_YYYYMMDD_HHMMSS.csv`
- [ ] Progression visible en direct

### Option B: Test rapide (validation protocole)

Si vous voulez tester d'abord:

```powershell
.\run_campaign3.ps1 -Iterations "10000"
```

Cela génère ~ 120 runs (3 instances × 10 seeds × 2 TW modes × 2 algos)

- [ ] Configuration testée

---

## ⏳ Pendant l'exécution

### Surveillance en direct

Ouvrir un **second terminal PowerShell** pour surveiller:

```powershell
# Voir la progression
Get-Content "campaign3_progress_*.csv" -Tail 5 -Wait

# Compter les runs réussis
(Import-Csv "campaign3_progress_*.csv" | Where-Object {$_.status -eq "ok"} | Measure-Object).Count

# Chercher les erreurs
Import-Csv "campaign3_progress_*.csv" | Where-Object {$_.status -eq "fail"}
```

### Points de contrôle

À mi-exécution (~180/360 runs):
- [ ] Au moins 90% de réussite
- [ ] Pas d'erreurs sistématiques récurrentes
- [ ] Temps d'exécution raisonnable par run

À fin d'exécution (~360/360 runs):
- [ ] 100% de réussite (ou >95%)
- [ ] Tous les fichiers `executions_log.csv` générés

---

## 📊 Post-exécution (Après la campagne)

### 1. Vérification des résultats

```powershell
# Compter les logs générés
(Get-ChildItem resultsSA/Exp*/executions_log.csv).Count  # devrait être ~60+
(Get-ChildItem resultTABU/Exp*/executions_log.csv).Count # devrait être ~60+

# Vérifier pas de fichiers vides
Get-ChildItem resultsSA/Exp*/executions_log.csv | Where-Object {$_.Length -lt 100}
Get-ChildItem resultTABU/Exp*/executions_log.csv | Where-Object {$_.Length -lt 100}
```

- [ ] Tous les runs ont généré logs
- [ ] Pas de fichiers vides

### 2. Analyse Python

```powershell
python analyze_campaign3.py
```

Génère les fichiers:
- [ ] `campaign3_consolidated_YYYYMMDD_HHMMSS.csv` - Tous les résultats
- [ ] `campaign3_summary_YYYYMMDD_HHMMSS.csv` - Statistiques moyennes
- [ ] `campaign3_feasibility_YYYYMMDD_HHMMSS.csv` - Taux faisabilité
- [ ] `campaign3_runtime_YYYYMMDD_HHMMSS.csv` - Analyse temps
- [ ] `campaign3_comparison_sa_tabu_YYYYMMDD_HHMMSS.csv` - SA vs Tabu

**⚠️ Installer pandas et numpy si besoin:**
```powershell
pip install pandas numpy
```

### 3. Inspection manuelle des résultats

Ouvrir Excel ou LibreOffice et charger:
- [ ] `campaign3_consolidated_*.csv` - **Vérifier les colonnes présentes**
- [ ] `campaign3_summary_*.csv` - **Vérifier l'ordre de grandeur des distances**
- [ ] `campaign3_comparison_sa_tabu_*.csv` - **Vérifier quel algo gagne**

**Questions à se poser:**

1. **Qualité :** 
   - Tabu est-il systématiquement meilleur que SA?
   - Ou le résultat dépend de l'instance?

2. **Impact TW :**
   - Les distances augmentent-elles en mode "oui"?
   - De combien en moyenne?

3. **Plateau :**
   - Y a-t-il une grosse amélioration entre 10k et 30k itérations?
   - Et entre 30k et 100k?

4. **Robustesse :**
   - L'écart-type (std) est-il faible ou énorme?
   - Y a-t-il des outliers problématiques?

---

## 📈 Préparation du rapport

Une fois l'analyse complète:

### Tableau 1 : Résultats par instance et mode TW

```
Instance | Mode TW | Algo | Dist. moy ± std | Temps (ms) | Taux faisable
---------|---------|------|-----------------|------------|---------------
data101  | non     | SA   | 1850 ± 45       | 150        | 100%
data101  | non     | Tabu | 1780 ± 50       | 450000     | 100%
data101  | oui     | SA   | 1900 ± 55       | 150        | 85%
...
```

- [ ] Tableau créé à partir des fichiers CSV

### Tableau 2 : Impact des itérations

```
Instance | Mode TW | Algo | 10k iter  | 30k iter  | 100k iter
---------|---------|------|-----------|-----------|----------
data101  | non     | SA   | 1920±60   | 1850±45   | 1805±30
...
```

- [ ] Tableau généré

### Figure 1 : Boîtes à moustaches

- [ ] Créer plots : distance (Y) vs algo (X) par (instance, mode TW)
  - Utiliser Excel, Python (matplotlib), ou R

### Figure 2 : Convergence par itérations

- [ ] Créer plots : distance (Y) vs itérations (X)
  - Deux courbes: SA bleu, Tabu rouge
  - Voir où se stabilisent les courbes

### Figure 3 : Impact TW

- [ ] Comparaison barres: sans TW (gris) vs avec TW (bleu)

---

## ✍️ Rédaction du rapport

Sections de résultats à remplir avec vos données:

### Section "Résultats expérimentaux"

> Nous avons exécuté une campagne de 360 runs sur 3 instances représentatives 
> (data101: petit, data111: moyen, data1101: large) avec 10 graines aléatoires 
> par configuration.
>
> **Impact des fenêtres de temps :**
> - Sans TW, la distance moyenne est [X]
> - Avec TW, elle augmente de [Y]%
> - Le nombre de véhicules augmente en moyenne de [Z]
>
> **Comparaison SA vs Tabu :**
> - Sur data101 sans TW: Tabu gagne de [A]% en qualité
> - Sur data111 sans TW: Tabu gagne de [B]%
> - Sur data1101 sans TW: [algo] gagne (résultat potentiellement différent!)
>
> **Plateau de convergence :**
> - Pour SA: amélioration majeure jusqu'à 30k itérations, plateau à 100k
> - Pour Tabu: amélioration continue jusqu'à 100k (ou plateau plus tard?)
>
> **Robustesse (stabilité) :**
> - SA: écart-type [écart1], coefficient variation [ratio1]%
> - Tabu: écart-type [écart2], coefficient variation [ratio2]%
> - [Algo] est plus stable/fiable.

- [ ] Section rédigée

### Section "Discussion"

> Pourquoi ces résultats?
> - SA explore davantage mais reste piégé localement
> - Tabu avec tenure=40 exploite mieux le voisinage inter-relocate
> - Les fenêtres de temps restreignent l'espace de recherche
> - Seul l'algo [X] maintient une bonne qualité en mode TW
>
> Quelle stratégie choisir en pratique?
> - Pour temps réel (< 1 sec): SA
> - Pour meilleure qualité (tolérance 10+ sec): Tabu
> - [Tradeoff personnel selon les constraints du sujet]

- [ ] Section rédigée

---

## 🎯 Critères de succès

✅ **La campagne est un succès si:**

1. ✅ Tous les 360 runs se terminent avec succès (ou > 95%)
2. ✅ Les logs contiennent données complètes et cohérentes
3. ✅ L'analyse Python génère au moins 3 fichiers de résumé
4. ✅ Les tableaux et figures sont générés et interprétables
5. ✅ Vous pouvez écrire une section "Résultats" de 1-2 pages solidement fondée

---

## 🚨 Dépannage

### Le script plante dès le démarrage

```
ERROR: Cannot find compiled classes in 'bin'.
```

**Solution:**
```powershell
javac --release 21 -d bin src/vrptw/*.java
```

### Le script s'exécute mais génère peu de fichiers

**Vérifier:**
- [ ] Que les répertoires `resultsSA/` et `resultTABU/` existent et sont accessibles en écriture
- [ ] Que le chemin du classpath est correct (`bin`)
- [ ] Les permissions de fichier (Windows)

### Tabu est TRÈS lent sur data1101

C'est normal : data1101 a 1100+ clients, Tabu explore énormément!

**Options:**
1. Lancer la campagne en arrière-plan sur plusieurs nuits
2. Réduire itérations pour Tabu sur data1101 (cf. contexte.md section 11)
3. Tester d'abord avec data101 + data111 seulement (Campagne 3-lite)

### L'analyse Python ne trouve pas les logs

```
ERROR: No execution logs found.
```

**Vérifier:**
```powershell
Get-ChildItem resultsSA/Exp*/executions_log.csv -ErrorAction SilentlyContinue
Get-ChildItem resultTABU/Exp*/executions_log.csv -ErrorAction SilentlyContinue
```

Si vide: les runs n'ont pas généré les fichiers. Vérifier Main.java génère bien `executions_log.csv`.

---

## 📅 Timeline recommandée

| Date | Tâche | Statut |
|------|-------|--------|
| 27 Avr | Préparation (compilation, vérif données) | ⏳ |
| 27-28 Avr | Lancer Campagne 3 (1-3h d'exécution) | ⏳ |
| 28 Avr | Analyse Python + tableaux | ⏳ |
| 28-29 Avr | Rédaction section résultats/discussion | ⏳ |
| 2-3 Mai | Relecture + ajustements finaux | ⏳ |
| 4 Mai 07h00 | **LIVRAISON FINALE** | 🎯 |

---

## Questions rapides ?

**Q: Faut-il vraiment 100 000 itérations?**
R: Oui, cela montre le plateau. Tabu peut être long, mais c'est le point!

**Q: Les fenêtres de temps vont vraiment tout casser?**
R: Oui! C'est l'intérêt: faire une vraie analyse comparée.

**Q: Puis-je paralléliser les runs?**
R: Le script actuel est séquentiel. Pour paralléliser, modifier `run_campaign3.ps1` ou lancer plusieurs instances du script.

**Q: Et si un algo timeout?**
R: Vérifier `campaign3_progress_*.csv`. Les runs abortés auront `status=fail`. C'est OK, l'analyse gardera les autres.

---

✅ **Vous êtes prêts!** Lancez `.\run_campaign3.ps1` et laissez tourner. 

Bon courage pour la campagne! 🚀
