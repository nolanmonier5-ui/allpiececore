# ServerForge (Paper 1.21.4)

Un seul plugin avec trois systemes, **100% editables en jeu** :

1. **Shop dynamique** — prix qui montent a l'achat, baissent a la vente.
2. **Fabrication d'armes/items** — recettes creees en placant les items.
3. **Coffres a butin** — outil pour configurer des coffres a loot aleatoire.

L'economie passe par **Vault** (par reflection, pas de dependance Maven) : Vault +
un plugin d'economie doivent etre installes sur le serveur pour les achats/ventes.

## Compilation
Java 21 + Maven : `mvn clean package` -> `target/ServerForge-1.0.0.jar` dans `plugins/`.

## Commandes
| Commande | Effet | Permission |
|----------|-------|------------|
| `/shop` | Ouvre la boutique | tous |
| `/shopadmin` | Editeur du shop | `serverforge.admin` |
| `/shopadmin reload` | Recharge config + donnees | `serverforge.admin` |
| `/craft` | Ouvre la fabrication | tous |
| `/craftadmin` | Editeur des recettes | `serverforge.admin` |
| `/lootchest` | Recoit l'outil coffres a butin | `serverforge.admin` |

## 1) Shop dynamique
- Le prix d'un item monte de `buy-step %` a chaque unite **achetee**, baisse de
  `sell-step %` a chaque unite **vendue** (reglables par item dans l'editeur).
- **Vente = 50% du prix d'achat courant** (`sell-ratio` dans `config.yml`).
- Le multiplicateur est borne par `min-multiplier` / `max-multiplier`.
- **Tous les prix reviennent au prix initial toutes les `reset-minutes`** (defaut 30,
  `0` = jamais). Bouton "reinitialiser le prix" aussi dispo par item.
- Categories pre-remplies : **Minerais**, **Maison & Deco**, **Nourriture**.
- `/shopadmin` : creer des categories, ajouter des items (depuis l'inventaire),
  regler prix de base, %, achat/vente on-off, emplacement.
- Cote joueur : clic gauche = acheter, clic droit = vendre, Shift = x64.

## 2) Fabrication
- `/craftadmin` -> categories d'armes -> creer une recette.
- Dans l'editeur de recette : **place les ingredients** dans la grille (10-16) et
  **l'item a fabriquer** dans la case de sortie (24), directement depuis ton
  inventaire (CMD / nom / lore conserves). La quantite d'un ingredient = la pile.
- Regle le **temps de craft** (secondes) et le nom.
- Les items de test deposes te sont rendus a la fermeture (rien n'est perdu).
- Cote joueur : `/craft` -> categorie -> clic gauche sur une recette pour fabriquer
  (les ingredients sont consommes ; apres le delai, l'item est remis).

## 3) Coffres a butin
- `/lootchest` te donne l'outil.
- **Clic droit** sur un coffre : ouvre la config (items + % de drop, delai de reset).
  Le coffre se re-remplit automatiquement quand le delai est ecoule, a l'ouverture.
- **Clic gauche** sur un autre coffre : copie la config du dernier coffre configure.
- Delai de reset par coffre (ex: `24h`, `30m`, `0` = jamais). "Remplir maintenant"
  pour forcer. "Supprimer" pour le redevenir coffre normal.

## Fichiers generes
```
plugins/ServerForge/
├── config.yml     # titres, % par defaut, bornes, sell-ratio, reset-minutes
├── shop.yml       # categories + items du shop (prix, multiplicateurs)
├── craft.yml      # categories + recettes
└── chests.yml     # coffres a butin (par position)
```
