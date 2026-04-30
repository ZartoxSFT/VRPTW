# Rapport VRPTW - Guide d'Utilisation

## 📄 Fichier Principal

**`RAPPORT_VRPTW_FINAL.tex`** - Rapport LaTeX complet (~32 pages)

## 📋 Contenu du Rapport

### Structure

1. **Résumé Exécutif** (page 2)
   - Synthèse des données expérimentales
   - Conclusions principales
   - Recommandations

2. **Chapitre 1: Introduction et Contexte** (p. 3-5)
   - Définition formelle VRPTW
   - Complexité et relevance pratique
   - Justification choix métaheuristiques

3. **Chapitre 2: Méthodologie Expérimentale** (p. 6-15)
   - Architecture Java et structure code
   - Protocole 3 phases (sweeps → validation → campagne finale)
   - Configuration instances et paramètres

4. **Chapitre 3: Résultats Globaux** (p. 16-22)
   - Synthèse statistique complète (SA vs TABU)
   - Résultats par instance (data101, data111, data201)
   - **DÉCOUVERTE CLÉ**: Fenêtres temps réduisent variance TABU de 89.9%!

5. **Chapitre 4: Analyse Comparative SA vs TABU** (p. 23-29)
   - Fondamentaux théoriques + pseudocode
   - Comparaison empirique (qualité, robustesse, trade-offs)
   - Analyse voisinages (Relocate, Exchange, 2-opt)

6. **Chapitre 5: Impact Fenêtres de Temps** (p. 30-34)
   - Impact sur espace solution (+50-70% distance)
   - Variance avant/après TW (résultat clé: TABU se stabilise)
   - Faisabilité et nombre de véhicules

7. **Chapitre 6: Justification Paramètres** (p. 35-40)
   - **Température SA (1250K)** : Pourquoi? Accepte ~67% solutions dégradées
   - **Cooling rate (0.9993)** : Équilibre exploration/refroidissement
   - **Tenure TABU (40)** : Évite cycles, permet diversification
   - **30,000 itérations** : Point d'équilibre qualité/temps

8. **Chapitre 7: Discussion et Interprétations** (p. 41-48)
   - 4 découvertes principales expliquées mécaniquement
   - Performance par cas d'usage
   - Limitations et améliorations possibles
   - Algorithme hybride proposé

9. **Chapitre 8: Conclusion** (p. 49-50)
   - Synthèse générale
   - Apports scientifiques
   - Recommandations déploiement
   - Perspectives futures

## 📊 Données Clés du Rapport

### Comparaison Globale (135 runs consolidés)

| Critère | SA | TABU | Avantage |
|---------|----|----|----------|
| Distance moyenne | 1272.08 km | 1197.37 km | TABU +5.9% |
| Runtime | 85.69 ms | 1,398,556 ms | SA 16,317× plus rapide |
| Meilleure solution | 938.94 km | 873.55 km | TABU +6.9% |
| Faisabilité | 11.1% | 12.7% | TABU légèrement meilleur |
| Variance (avec TW) | ±235 km | ±29 km | **TABU 8× plus stable!** |

### Découvertes Principales

1. **TABU +5.9% meilleur en qualité** → Énumère 7,883 voisins/itération vs 1 pour SA
2. **SA 16,317× plus rapide** → Complexité O(n) vs O(n⁴)
3. **Fenêtres temps +50-70% distance** → Contraintes réduisent drastiquement espace solution
4. **Fenêtres temps stabilisent TABU** → Variance réduite de 89.9% (287→29 km)!

## 🔧 Comment Générer le PDF

### Option 1: Avec LaTeX (Recommandé)

```bash
# Installation (première fois)
# Windows: Installer MikTeX depuis https://miktex.org/download
# Linux: sudo apt-get install texlive-full
# Mac: brew install mactex

# Générer PDF
pdflatex RAPPORT_VRPTW_FINAL.tex
# Ou
xelatex RAPPORT_VRPTW_FINAL.tex
```

### Option 2: Avec VS Code + Extension LaTeX

1. Installer extension "LaTeX Workshop" (James Yu)
2. Ouvrir RAPPORT_VRPTW_FINAL.tex
3. Clic "Build LaTeX project" ou Ctrl+Alt+B

### Option 3: Overleaf (En ligne, aucune installation)

1. Aller sur https://www.overleaf.com
2. "New Project" → "Upload Project"
3. Upload RAPPORT_VRPTW_FINAL.tex
4. Compiler directement dans l'interface

## 📐 Structure Contenu par Section

### Pour Ingénieurs/Praticiens
**Lire** : Chapitre 7 (Discussion) + Chapitre 8 (Conclusion)
- Trade-offs qualité/temps
- Recommandations déploiement
- Paramétrisation finale

### Pour Chercheurs
**Lire** : Chapitre 2-4 + Chapitre 6
- Méthodologie rigoureuse
- Résultats statistiques complets
- Justification scientifique paramètres

### Pour Étudiants
**Lire** : Chapitre 1 (Introduction) + Chapitre 4-5
- Explication fondamentaux SA/TABU
- Impact paramètres et contraintes
- Analyse comparative

## 🎯 Points Clés à Retenir

### Quand Utiliser SA?
✓ Besoin temps réel (< 1 sec)
✓ Scalabilité requise (> 200 clients)
✓ Parallélisation possible (multirun)
✓ Qualité acceptable (5-10% gap optimal)

### Quand Utiliser TABU?
✓ Temps budget ample (minutes-heures)
✓ Qualité quasi-optimale requise
✓ Faisabilité critique (TW+capacité)
✓ Robustesse/déterminisme important
✓ Instances modérées (50-150 clients)

## 📚 Références dans le Rapport

Le rapport cite et s'appuie sur:
- Données empiriques : 186 runs consolidés
- Littérature métaheuristiques classique
- Best practices paramétrisation (Gendreau et al., Michel et al.)
- Analyses statistiques rigoureuses

## ⚠️ Notes Importantes

1. **Fenêtres de temps** = contraintes TRÈS importantes pour VRPTW réaliste
2. **30,000 itérations** = bon compromis pour nos instances (100 clients)
3. **Paramètres peuvent varier** pour autres instances (ajuster tenure/temp selon problème size)
4. **Taux faisabilité bas (~12%)** = limitation reconnue, suggestions améliorations incluses

## 🚀 Utilisations Recommandées du Rapport

### Pour Présentation Orale
- Résumé Exécutif + Chapitre 7 (10-15 min)
- Graphiques convergence + trade-off qualité/temps
- Recommandations déploiement

### Pour Publication Académique
- Chapitre 2-4-6 : Méthodologie + résultats
- Tableaux statistiques + justifications paramétriques
- Discussion limitations et perspectives

### Pour Documentation Technique
- Chapitre 6 : Paramétrisation détaillée
- Pseudocode complet SA + TABU
- Recommandations finales (Chapitre 8)

---

**Dernière mise à jour** : 29 avril 2026  
**Basé sur** : 186 runs expérimentaux (360 initialement lancés)  
**Pages** : 50 pages complètes avec analyses détaillées
