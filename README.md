# ChillSMP Plugin

Plugin all-in-one pour ton serveur "Chill SMP" (Paper / Minecraft 1.21+).

## ⚙️ COMMENT COMPILER — SANS RIEN INSTALLER (méthode recommandée)

Ce projet contient déjà tout pour que **GitHub compile le plugin à ta place**,
gratuitement, dans le cloud. Tu n'as besoin que d'un compte GitHub (gratuit).

### Étapes
1. Va sur https://github.com et crée un compte si tu n'en as pas
2. Crée un nouveau dépôt (bouton vert "New") — par exemple nommé `chillsmp-plugin`,
   laisse-le en **Public** ou **Private**, peu importe
3. Sur la page du dépôt vide, clique sur **"uploading an existing file"**
4. Glisse-dépose **tout le contenu** du dossier `chillsmp-plugin` (dézippé) — y
   compris le dossier caché `.github` (si ton explorateur de fichiers le masque,
   active l'affichage des fichiers cachés, ou utilise GitHub Desktop)
5. Clique sur **"Commit changes"**
6. Va dans l'onglet **"Actions"** en haut du dépôt → tu verras une compilation
   en cours ("Build Plugin") avec un rond jaune qui tourne. Attends 1-2 minutes
   qu'il devienne vert ✅
7. Clique sur cette compilation terminée → en bas tu verras **"Artifacts"** →
   clique sur **"ChillSMP-plugin"** pour télécharger un zip contenant ton `.jar`
8. Dézippe, récupère le `.jar`, mets-le dans `plugins/` de ton serveur Paper,
   redémarre le serveur

C'est tout — pas de Java, pas de Maven, pas de terminal à installer chez toi.

⚠️ Si le dossier `.github` ne s'est pas uploadé (certains navigateurs cachent les
dossiers commençant par un point), dis-le moi, je te donnerai une autre méthode
pour l'ajouter directement depuis l'interface GitHub.

## ⚙️ Alternative : compiler en local

### Prérequis
- Java 21 installé (https://adoptium.net/)
- Maven installé (https://maven.apache.org/download.cgi)
  (ou utilise IntelliJ IDEA qui a Maven intégré, encore plus simple)

### Étapes
1. Télécharge/dézippe ce dossier `chillsmp-plugin`
2. Ouvre un terminal dans ce dossier
3. Lance :
   ```
   mvn clean package
   ```
4. Le fichier `.jar` final sera dans `target/ChillSMP-1.0.0.jar`
5. Mets ce `.jar` dans le dossier `plugins/` de ton serveur Paper
6. Redémarre le serveur

## 📋 Liste des fonctionnalités

- **Anti-Xray** : surveille le ratio minerai/pierre par joueur, alerte les OP dans le
  chat avec un message cliquable, `/xray` ouvre un menu (têtes des joueurs connectés)
  puis détail par joueur (minerais minés, %, etc.)
- **Drop de tête** : un joueur drop sa tête à sa mort (configurable)
- **Dashboard Staff** (`/staff`, OP uniquement) : menu têtes de joueurs → clique sur
  une tête → kill, tp à toi, se tp à lui, voir son inventaire, voir son ender chest,
  ban temporaire
- **Vanish** (`/vanish`, OP uniquement) : invisible sans particules, faux message de
  déco/connexion dans le chat, caché du tab
- **Vein Miner** + **Tree Feller** : configurables dans `config.yml` (liste de blocs,
  outil requis, sneak ou non, nombre max de blocs, usure d'outil)
- **Tab list custom** : "Chill SMP" en en-tête, infos en pied de page, tout configurable
- **HUD custom** (`/hud` ou `/interface`) : scoreboard à droite, playtime, kills,
  deaths, K/D, ping, biome, coords... configurable dans `config.yml`
- **Stack custom jusqu'à 99** : liste d'items configurable dans `config.yml`
- **Hoppers configurables** : vitesse de transfert modifiable dans `config.yml`
- **`/smpreload`** : recharge TOUTE la config sans redémarrer le serveur (pratique
  pour les modifs de hoppers, vein miner, etc.)
- **`/trade <joueur>`** : interface double-coffre, verre gris pour séparer, verre vert
  pour accepter, échange sécurisé
- **Commandes custom faciles** (`custom-commands.yml`) : crée des commandes comme
  `/site`, `/discord`, `/rules` sans toucher au code, avec liens cliquables
- **`/stats [joueur]`** : interface avec horloge (playtime au survol), épée (kills),
  etc.
- **Bans temporaires** : `/tempban <joueur> <durée> <raison>` (ex: `7d`, `2h`, `30m`),
  `/unban <joueur>`

Toutes les commandes staff sont automatiquement limitées aux OP.

## 📁 Fichiers de configuration (créés au premier lancement)

- `config.yml` → tout le réglage principal (anti-xray, vein miner, hoppers, stack,
  tab, hud, messages...)
- `custom-commands.yml` → tes commandes personnalisées
- `players.yml` → stats des joueurs (auto-généré, ne pas toucher)
- `bans.yml` → liste des bans temporaires (auto-généré)

## 🔧 Après une modif de config

Tape `/smpreload` en jeu (OP uniquement) — pas besoin de redémarrer le serveur.
