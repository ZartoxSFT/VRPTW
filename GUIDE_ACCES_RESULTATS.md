# 📁 GUIDE D'ACCÈS AUX RÉSULTATS - Campagne 3

**Généré:** 28 avril 2026  
**État:** Analyse complète (186 runs, 320 enregistrements)

---

## 🎯 PAR OÙ COMMENCER?

### Si vous avez 5 minutes:
1. Ouvrir **RESUME_EXECUTIF_FR.md** ← Lisez ça en premier!
2. Voir les chiffres clés en bas du fichier

### Si vous avez 20 minutes:
1. **RESUME_EXECUTIF_FR.md** (lisez sections 1-3)
2. Ouvrir **campaign3_report_summary_*.csv** dans Excel
3. Consulter **OPTIMAL_PARAMETERS.md** (tableau de synthèse)

### Si vous avez 1 heure (préparation rapport):
1. **RESUME_EXECUTIF_FR.md** (complet)
2. **ANALYSIS_REPORT_CAMPAIGN3.md** (détails complets)
3. Ouvrir **campaign3_consolidated_*.csv** pour valider
4. Créer quelques graphiques (Excel/LibreOffice)

---

## 📄 FICHIERS GÉNÉRÉS - DESCRIPTION

### 🔴 FICHIERS À LIRE EN PRIORITÉ

#### 1. **RESUME_EXECUTIF_FR.md** ⭐⭐⭐
   - **Quoi:** Résumé français pour rapport
   - **Qui:** Vous lisez ça MAINTENANT
   - **Contenu:** Sections prêtes à copier-coller dans rapport
   - **Durée lecture:** 15 min
   - **Action:** Copier sections pertinentes → rapport Word

#### 2. **ANALYSIS_REPORT_CAMPAIGN3.md** ⭐⭐⭐
   - **Quoi:** Rapport détaillé complet
   - **Qui:** Référence de fond
   - **Contenu:** 12 sections (paramètres, voisinage, comparaison, TW, instances)
   - **Durée lecture:** 30 min
   - **Action:** Approfondir comprendre les findings

#### 3. **OPTIMAL_PARAMETERS.md** ⭐⭐⭐
   - **Quoi:** Configuration exacte des paramètres
   - **Qui:** Validation & reproductibilité
   - **Contenu:** Quick reference table, commandes templates
   - **Durée lecture:** 10 min
   - **Action:** Valider que paramètres utilisés sont corrects

---

### 🟠 FICHIERS DE DONNÉES POUR EXCEL

#### 4. **campaign3_consolidated_YYYYMMDD_HHMMSS.csv**
   - **Quoi:** Tous les 320 runs bruts
   - **Lignes:** 320 (165 SA + 155 Tabu)
   - **Colonnes:** instance, algorithm, best_distance, runtime_ms, parameters, etc.
   - **Action:** 
     - Ouvrir en Excel
     - Créer tableau pivot
     - Faire graphiques (boxplot, courbes)

#### 5. **campaign3_report_summary_YYYYMMDD_HHMMSS.csv** ⭐
   - **Quoi:** Tableau synthétisé prêt pour rapport
   - **Lignes:** ~12 (résumé par instance/mode/algo)
   - **Colonnes:** Instance, Mode, Algo, Best, Avg±std, Faisable%, Time
   - **Action:** Copier directement dans Word/Calc → Tableau rapport

#### 6. **campaign3_summary_YYYYMMDD_HHMMSS.csv**
   - **Quoi:** Statistiques agrégées
   - **Action:** Référence pour vérifier moyennes/écart-types

#### 7. **campaign3_feasibility_YYYYMMDD_HHMMSS.csv**
   - **Quoi:** Taux faisabilité par configuration
   - **Action:** Valider que 98-100% sont faisables

#### 8. **campaign3_runtime_YYYYMMDD_HHMMSS.csv**
   - **Quoi:** Temps d'exécution en secondes
   - **Action:** Vérifier ratio temps SA vs Tabu

#### 9. **campaign3_comparison_sa_tabu_YYYYMMDD_HHMMSS.csv**
   - **Quoi:** Comparaison directe SA vs Tabu
   - **Colonnes:** SA distance, Tabu distance, Différence, Ratio
   - **Action:** Voir directement quel algo gagne

---

### 🟢 FICHIERS DE SCRIPT (Si vous voulez réanalyser)

#### 10. **advanced_analysis_campaign3.py**
   - **Quoi:** Script Python complet d'analyse
   - **Action:** Lancer `python advanced_analysis_campaign3.py` si besoin
   - **Output:** Régénère tous les fichiers CSV

#### 11. **analyze_campaign3.py**
   - **Quoi:** Analyse Python basique
   - **Action:** Pour consolidation simple

---

## 🗂️ ORGANISATION RECOMMANDÉE

Créer un dossier `RESULTATS_FINAUX/` avec:

```
RESULTATS_FINAUX/
├── 📘 Pour Rapport
│   ├── RESUME_EXECUTIF_FR.md ← COMMENCER PAR ICI
│   ├── ANALYSIS_REPORT_CAMPAIGN3.md
│   ├── OPTIMAL_PARAMETERS.md
│   └── campaign3_report_summary_*.csv ← Tableau Excel
│
├── 📊 Données Brutes
│   ├── campaign3_consolidated_*.csv
│   ├── campaign3_summary_*.csv
│   ├── campaign3_feasibility_*.csv
│   ├── campaign3_runtime_*.csv
│   └── campaign3_comparison_sa_tabu_*.csv
│
└── 🐍 Scripts
    ├── advanced_analysis_campaign3.py
    └── analyze_campaign3.py
```

---

## 📋 UTILISATION PAR CONTEXTE

### Pour Rédiger Section "Résultats Expérimentaux"

1. Ouvrir **RESUME_EXECUTIF_FR.md** → Section 2
2. Copier le tableau principal
3. Ajouter le texte des sections 3-4 (Comparaison + TW)
4. Citer les chiffres clés

### Pour Rédiger Section "Analyse Paramètres"

1. Consulter **OPTIMAL_PARAMETERS.md** → sections "Parameter Sensitivity"
2. Copier tableau de synthèse
3. Ajouter contexte "Pourquoi ces paramètres?"

### Pour Créer Graphiques

1. Ouvrir **campaign3_consolidated_*.csv** en Excel
2. Faire tab pivot: grouper par (instance, algorithm, enforce_time_windows)
3. Créer boxplot distance (Y) vs algo (X)
4. Créer ligne graph: itérations (X) vs distance moyenne (Y)

### Pour Discuter en Soutenance

1. Préparer slides de **RESUME_EXECUTIF_FR.md**
2. Avoir **OPTIMAL_PARAMETERS.md** sous la main pour questions paramètres
3. Préparer réponses aux 5 "Points à défendre" (fin du résumé)

---

## ✅ CHECKLIST UTILISATION

- [ ] Lire **RESUME_EXECUTIF_FR.md** (15 min)
- [ ] Ouvrir **campaign3_report_summary_*.csv** en Excel
- [ ] Comparer chiffres avec RESUME_EXECUTIF_FR
- [ ] Copier tableau prêt → rapport Word
- [ ] Lire **ANALYSIS_REPORT_CAMPAIGN3.md** pour profondeur
- [ ] Consulter **OPTIMAL_PARAMETERS.md** si questions paramètres
- [ ] Créer 2-3 graphiques Excel (boxplot, courbes)
- [ ] Préparer slides powerpoint avec findings
- [ ] Valider que données consolidées concordent (320 runs)

---

## 🚨 SI QUELQUE CHOSE NE CONCORDE PAS

**Symptôme:** Les chiffres du RESUME ne correspondent pas à CONSOLIDATED

**Solution:**
1. Ouvrir **campaign3_consolidated_*.csv**
2. Vérifier nombre de lignes (doit être 320)
3. Filtrer par algo: count(SA) + count(Tabu)
4. Vérifier dates (tous générés le même jour?)

**Symptôme:** Fichiers CSV ne s'ouvrent pas bien en Excel

**Solution:**
1. Ouvrir dans Notepad d'abord (vérifier UTF-8)
2. Utiliser LibreOffice Calc (meilleur avec encodage)
3. Importer comme délimiteur "," (virgule)

---

## 📞 QUESTIONS RAPIDES

**Q: Par où je commence vraiment?**
R: 1) RESUME_EXECUTIF_FR.md 2) campaign3_report_summary_*.csv 3) Créer graphiques

**Q: Où les paramètres optimaux?**
R: OPTIMAL_PARAMETERS.md tableau "Quick Reference Table"

**Q: Comment citer les données?**
R: Utiliser chiffres de RESUME_EXECUTIF_FR section "Données clés à citer"

**Q: Comment je valide mes findings?**
R: Ouvrir campaign3_consolidated_*.csv → faire "Data → Pivot Table"

**Q: Je dois refaire l'analyse?**
R: `python advanced_analysis_campaign3.py` (regénère tous les fichiers)

---

## 🎯 RÉSUMÉ: CE QUI EST PRÊT

✅ Données consolidées (320 enregistrements)
✅ Paramètres optimaux identifiés
✅ Voisinage optimal validé (inter-relocate)
✅ Impact fenêtres de temps quantifié
✅ Comparaison SA vs Tabu complète
✅ Tableaux Excel prêts à copier
✅ Texte pour rapport disponible
✅ Graphiques suggérés
✅ Réponses aux questions clés

**IL NE MANQUE QUE LA RÉDACTION FINALE DANS LE RAPPORT!**

---

**Généré:** 28 avril 2026  
**Prochaine étape:** Copier findings → rapport final  
**Date limite:** 4 mai 2026 07h00

Vous êtes dans les délais! 🚀
