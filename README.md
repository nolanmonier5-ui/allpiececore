# ServerForge (Paper 1.21.4)

Un seul plugin avec trois systemes, **100% editables en jeu** :

1. **Shop dynamique** — prix qui montent a l'achat, baissent a la vente.
2. **Fabrication d'armes/items** — recettes creees en placant les items.
3. **Coffres a butin** — outil pour configurer des coffres a loot aleatoire.

L'economie passe par **Vault** (par reflection, pas de dependance Maven) : Vault +
un plugin d'economie doivent etre installes sur le serveur pour les achats/ventes.

## Compilation
Java 21 + Maven : `mvn clean package` -> `target/ServerForge-1.0.0.jar` dans `plugins/`.

## Noms de menus (icone vs titre)

Pour le shop ET la fabrication, chaque categorie a **deux noms distincts** :
- **Nom (icone)** : le texte affiche sur l'icone dans le menu principal.
- **Titre du menu** : le titre affiche EN HAUT du menu de la categorie quand on
  l'ouvre. Independant, et il **supporte les caracteres custom** (police de GUI a
  texture personnalisee). Editable en jeu (editeur de categorie) ou en config
  (`menu-title`). Vide = reprend le nom de l'icone.

Les **titres des menus principaux** (shop, fabrication) et le **titre du menu
coffre** sont aussi editables : en jeu (boutons admin) et en config
(`shop.main-title`, `craft.main-title`, `chest.gui-title`).

## Icones du menu principal du shop

Dans `/shopadmin` -> **Icones du menu principal**. Tu peux creer **autant d'icones
que tu veux**, chacune ouvrant une categorie, avec son **nom affiche propre**, son
item + CustomModelData (choisi dans l'inventaire) et son slot. **Plusieurs icones
peuvent ouvrir la meme categorie** (ex: 4 icones "Minerais" a des emplacements
differents). Une categorie sans icone custom recoit une icone auto pour rester
visible. Stocke dans `shop.yml` (section `entries`).

## Fleche retour

La fleche "retour" des menus de categorie (shop ET craft) est **entierement
configurable** : item, CustomModelData, nom et slot. Editable en jeu (bouton
"Fleche retour" dans `/shopadmin` et `/craftadmin`) ou en config (`back-button`
dans `config.yml`). Choisir l'item se fait en cliquant dans ton inventaire.

## Recharger le plugin

`/sfreload` (alias `/serverforge`, `/forgereload`) recharge config + shop + craft +
coffres. `/shopadmin reload` et `/craftadmin reload` fonctionnent toujours aussi.

## Commandes
| Commande | Effet | Permission |
|----------|-------|------------|
| `/shop` | Ouvre la boutique | tous |
| `/shopadmin` | Editeur du shop | `serverforge.admin` |
| `/shopadmin reload` | Recharge config + donnees | `serverforge.admin` |
| `/craft` | Ouvre la fabrication | tous |
| `/craftadmin` | Editeur des recettes | `serverforge.admin` |
| `/lootchest` | Recoit l'outil coffres a butin | `serverforge.admin` |
| `/sfreload` | Recharge tout le plugin | `serverforge.admin` |

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
- **Clic gauche** sur un coffre configure : **copie** sa config (loot + chances + delai)
  dans le presse-papier. **Shift + clic gauche** sur un autre coffre : **colle** cette
  config dessus.
- **Items uniques** : dans la config d'un coffre, clic droit sur un item pour le rendre
  "unique". Une fois loote par un joueur, il est retire de **tous** les coffres a butin
  (ne reapparaitra plus nulle part). Note: detecte sur les coffres simples (pas double).
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
