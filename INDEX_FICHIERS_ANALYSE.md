# 📑 INDEX - CAMPAGNE 3 : TOUS LES FICHIERS D'ANALYSE

**Généré:** 28 avril 2026  
**Total Runs:** 186 / Enregistrements: 320

---

## 🎯 COMMENCEZ PAR CELUI-CI

### 👉 **RESULTATS_CLÉS.txt** ⭐⭐⭐
- **Format:** Texte court et concis
- **Durée:** 5 minutes de lecture
- **Contenu:** Les 3 points clés pour rapport + tableau + réponses questions
- **Action:** Lire d'abord, puis consulter autres fichiers si besoin

---

## 📘 FICHIERS DE RAPPORT (PRÊT À COPIER)

### 1. **RESUME_EXECUTIF_FR.md** ⭐⭐⭐
   - **Quoi:** Résumé complet en français pour rapport académique
   - **Sections:** 7 (Intro, Tableaux, Comparaison, TW, Voisinage, Params, Recommandations)
   - **Durée:** 20 min lecture
   - **Action:** Copier-coller sections dans Word
   - **Bon pour:** Rédaction sections "Résultats" et "Analyse"

### 2. **ANALYSIS_REPORT_CAMPAIGN3.md** ⭐⭐
   - **Quoi:** Rapport détaillé complet (12 sections)
   - **Sections:** Paramètres, voisinage, instances, TW impact, données, stats
   - **Durée:** 30 min lecture
   - **Action:** Référence de profondeur pour questions
   - **Bon pour:** Comprendre tous les détails

### 3. **OPTIMAL_PARAMETERS.md** ⭐⭐
   - **Quoi:** Configuration exacte des paramètres
   - **Sections:** Quick reference, sensitivity analysis, templates
   - **Durée:** 10 min lecture
   - **Action:** Vérifier paramètres utilisés
   - **Bon pour:** Reproductibilité, soutenance

### 4. **GUIDE_ACCES_RESULTATS.md** ⭐
   - **Quoi:** Guide navigation dans tous les fichiers
   - **Sections:** Par contexte (5min, 20min, 1h), par usage
   - **Durée:** 5 min lecture
   - **Action:** Savoir quel fichier utiliser pour quelle tâche
   - **Bon pour:** S'orienter

---

## 📊 FICHIERS DE DONNÉES (EXCEL)

### **campaign3_report_summary_*.csv** ⭐⭐⭐
   - **Quoi:** Tableau synthétisé prêt pour rapport
   - **Format:** CSV (ouvrir en Excel)
   - **Lignes:** ~12 (résumé par instance/algo)
   - **Colonnes:** Instance | Mode | Algo | Best | Avg±std | Faisable | Temps
   - **Action:** Copier-coller directement dans Word → Tableau 1
   - **Important:** C'est le tableau principal!

### **campaign3_consolidated_*.csv**
   - **Quoi:** Tous les 320 runs bruts
   - **Format:** CSV (ouvrir en Excel)
   - **Lignes:** 320
   - **Colonnes:** Tous les métriques (distance, runtime, parameters, etc.)
   - **Action:** Créer pivot tables, graphiques
   - **Bon pour:** Créer boxplot, courbes convergence

### **campaign3_comparison_sa_tabu_*.csv**
   - **Quoi:** Comparaison directe SA vs Tabu
   - **Colonnes:** SA distance | Tabu distance | Différence | Ratio
   - **Action:** Voir rapidement quel algo gagne
   - **Bon pour:** Valider avantage Tabu

### **campaign3_feasibility_*.csv**
   - **Quoi:** Taux faisabilité par configuration
   - **Action:** Vérifier 98-100% faisables

### **campaign3_runtime_*.csv**
   - **Quoi:** Temps d'exécution en secondes
   - **Action:** Vérifier ratio temps

### **campaign3_summary_*.csv**
   - **Quoi:** Statistiques agrégées (moyennes, std, min, max)
   - **Action:** Référence pour vérifier chiffres

---

## 🐍 FICHIERS DE SCRIPT (Si besoin réanalyse)

### **advanced_analysis_campaign3.py**
   - **Quoi:** Analyse Python complète (neighborhoods, paramètres, instances, TW)
   - **Action:** `python advanced_analysis_campaign3.py` pour régénérer tout
   - **Output:** Tous les fichiers CSV

### **analyze_campaign3.py**
   - **Quoi:** Analyse Python basique
   - **Action:** `python analyze_campaign3.py` pour consolidation simple

---

## 📋 WORKFLOW RECOMMANDÉ

### Étape 1: Comprendre les résultats (10 min)
1. Lire **RESULTATS_CLÉS.txt**
2. Consulter tableau dans ce fichier

### Étape 2: Rédiger section "Résultats" (30 min)
1. Ouvrir **RESUME_EXECUTIF_FR.md**
2. Copier sections 2-5 → rapport Word
3. Adapter texte à votre style

### Étape 3: Créer tableau pour rapport (10 min)
1. Ouvrir **campaign3_report_summary_*.csv** en Excel
2. Copier-coller dans Word (ou Overleaf)
3. Formatter tableau

### Étape 4: Créer graphiques (30 min)
1. Ouvrir **campaign3_consolidated_*.csv** en Excel
2. Faire pivot table (Instance × Algorithm)
3. Créer boxplot distance (Y) vs algo (X)
4. Créer courbe iterations (X) vs distance (Y)

### Étape 5: Préparer soutenance (30 min)
1. Slides 1-2 : Résumé design expérimental (186 runs, 320 enregistrements)
2. Slides 3-4 : Tableau + graphiques
3. Slides 5-6 : Interprétation (Tabu gagne, TW critique, voisinage optimal)
4. Slides 7 : Recommandations (SA vs Tabu selon contexte)

---

## 🎯 FICHIERS PAR OBJECTIF

### Objectif: "Je dois écrire la section Résultats"
→ Utiliser: **RESUME_EXECUTIF_FR.md** (sections 2-6)

### Objectif: "Je dois justifier mes paramètres"
→ Utiliser: **OPTIMAL_PARAMETERS.md** (sensitivity analysis)

### Objectif: "Je dois créer un graphique"
→ Utiliser: **campaign3_consolidated_*.csv** (ouvrir en Excel, pivot)

### Objectif: "Je dois répondre à une question de soutenance"
→ Utiliser: **RESULTATS_CLÉS.txt** (section Réponses aux questions)

### Objectif: "Je dois valider un chiffre"
→ Utiliser: **campaign3_report_summary_*.csv** (vérifier rapidement)

### Objectif: "Je ne sais pas par où commencer"
→ Lire: **GUIDE_ACCES_RESULTATS.md** (guide contexte)

---

## 📱 VERSION COURTE DES CHIFFRES CLÉS

À citer dans rapport:

```
TABU surpasse SA:    +12.2% qualité (1243 vs 1415 km)
Voisinage optimal:   inter-relocate
Impact fenêtres TW:  +55% distance
Faisabilité avec TW: SA 98%, Tabu 100%
Temps:               SA 0.1s, Tabu 400s (8600× plus lent)
Robustesse:          Tabu plus stable (std inférieur)
```

---

## ✅ CHECKLIST FINALE

Avant de rendre votre rapport:

- [ ] Lire RESULTATS_CLÉS.txt (comprendre les 3 points clés)
- [ ] Ouvrir campaign3_report_summary_*.csv (vérifier tableau)
- [ ] Copier sections de RESUME_EXECUTIF_FR.md dans rapport
- [ ] Créer 1-2 graphiques (boxplot, courbes)
- [ ] Vérifier que tous les chiffres concordent
- [ ] Préparer slides pour soutenance
- [ ] Relire la section Recommandations (RESUME_EXECUTIF_FR.md)

---

## 📞 FAQ RAPIDE

**Q: Quel fichier pour commencer?**
→ RESULTATS_CLÉS.txt (5 min)

**Q: Quels chiffres citer dans rapport?**
→ RESULTATS_CLÉS.txt (section "Données clés à citer")

**Q: Où le tableau prêt pour rapport?**
→ campaign3_report_summary_*.csv (copier-coller direct)

**Q: Comment je justifie le voisinage inter-relocate?**
→ ANALYSIS_REPORT_CAMPAIGN3.md (section 2)

**Q: Comment je valide mes paramètres?**
→ OPTIMAL_PARAMETERS.md (tableau synthèse)

---

## 🚀 STATUT: PRÊT POUR RAPPORT

✅ Données consolidées (320 enregistrements)
✅ Paramètres optimaux trouvés
✅ Voisinage optimal validé
✅ Impact TW quantifié
✅ Recommandations claires
✅ Texte prêt à copier
✅ Tableaux Excel générés
✅ Graphiques sugérés

**IL NE MANQUE QUE VOTRE RÉDACTION DANS WORD/OVERLEAF!**

---

## 📅 TIMELINE FINALE

- **28 avril (aujourd'hui):** Vous avez les résultats
- **28-29 avril:** Rédaction section Résultats (2-3 pages)
- **29-2 mai:** Création graphiques, slides
- **2-3 mai:** Relecture, corrections
- **4 mai 07h00:** **LIVRAISON FINALE** 🎯

**Vous êtes dans les délais!**

---

**Navigation facile:**
1. Commencez par RESULTATS_CLÉS.txt
2. Puis RESUME_EXECUTIF_FR.md
3. Consultez autres fichiers au besoin

Bonne rédaction! 📝
