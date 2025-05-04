# Gestion de Bibliothèque — MongoDB & RavenDB

## Présentation

Exemple d’application Java mettant en œuvre MongoDB pour la gestion d’une bibliothèque et RavenDB pour la comparaison des performances.

## Prérequis

* **JDK 16** (11 minimum)
* **Maven ≥  3.9**
* **Docker 20 +** ou un serveur **MongoDB ≥  6.0** local
* **RavenDB ≥  5.x** *(facultatif, uniquement pour la partie comparaison)*

## Démarrage rapide avec Docker

```bash
# MongoDB 6.x community
docker run --name mongo-community -p 27017:27017 -d mongodb/mongodb-community-server:latest

# RavenDB 5.x
docker run -d --name ravendb -p 8080:8080 -p 38888:38888 -v ravendb-data:/opt/RavenDB/Server/RavenData ravendb/ravendb:latest
```

* MongoDB : `mongosh mongodb://localhost:27017`
* RavenDB : [http://localhost:8080](http://localhost:8080) (admin/admin par défaut)

## Organisation du dépôt

### 1. Vidéo de démonstration (\~10 min)

* Dossier : `video/`

### 2. Rapport PDF (chapitres 1 à 5)

* Dossier : `rapport/`

### 3. Objets générés

* Dossier : `generated-data/`
* Utilisé par le générateur de données  ; son contenu peut être régénéré.

* Génération: `src/main/java/com/library/common/util/ModelDataGenerator.java`

### 4. Scripts de chargement

* **MongoDB** : `src/main/java/com/library/mongodb/dataloader`
* **RavenDB** : `src/main/java/com/library/ravendb/dataloader`
* Exécutez les classes `main` correspondantes

### 5. Sources Java (chapitres 6, 7 et 8)

#### MongoDB

* Chapitre 6 : `src/main/java/com/library/mongodb/dataloader`
* Chapitre 7 : `src/main/java/com/library/mongodb/crud`
* Chapitre 8 : `src/main/java/com/library/mongodb/manager`

#### RavenDB

* Chapitre 6 : `src/main/java/com/library/ravendb/dataloader`
* Chapitre 7 : `src/main/java/com/library/ravendb/crud`
* Chapitre 8 : `src/main/java/com/library/ravendb/manager`

### 6. Comparaison MongoDB / moteur NoSQL choisi

* Dossier : `comparaison/`

### 7. Cluster MongoDB pas à pas

* Dossier : `mongo-cluster/`
* Scripts, rapport et étapes de construction du cluster.

## Nettoyage

```bash
docker stop mongo-community ravendb
docker rm mongo-community ravendb
docker volume rm ravendb-data
```