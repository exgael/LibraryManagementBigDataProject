# Gestion de Bibliothèque — MongoDB & RavenDB

## Présentation

Exemple d’application Java mettant en œuvre MongoDB pour la gestion d’une bibliothèque et RavenDB pour la comparaison des performances.

Tous les fichiers peuvent être retrouvé ici:
[LibraryManagementBigDataProject](https://github.com/exgael/LibraryManagementBigDataProject.git)

## Prérequis

* **JDK 23.0.2 (⚠️ langage level 16 minimum)**
* **Maven ≥  3.9**
* **Docker 20 +** ou un serveur **MongoDB ≥  6.0** local
* **RavenDB ≥  5.x** *(facultatif, uniquement pour la partie comparaison)*

Le projet à été testé avec:
```
openjdk version "23.0.2" 2025-01-21
OpenJDK Runtime Environment Homebrew (build 23.0.2)
OpenJDK 64-Bit Server VM Homebrew (build 23.0.2, mixed mode, sharing)
```

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

* Dossier : [video/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/video)

### 2. Rapport PDF (chapitres 1 à 5)

* Dossier : [rapport/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/rapport)

### 3. Objets générés

* Dossier : [generated-data/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/generated-data)
* Utilisé par le générateur de données  ; son contenu peut être régénéré.

* Génération: [src/main/java/com/library/common/util/ModelDataGenerator.java](https://github.com/exgael/LibraryManagementBigDataProject/blob/main/src/main/java/com/library/common/util/ModelDataGenerator.java)

### 4. Scripts de chargement

* **MongoDB** : [src/main/java/com/library/mongodb/dataloader/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/mangodb/dataloader)
* **RavenDB** : [src/main/java/com/library/ravendb/dataloader/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/ravendb/dataloader)
* Exécutez les classes `main` correspondantes

### 5. Sources Java (chapitres 6, 7 et 8)

#### MongoDB

* Chapitre 6 : [src/main/java/com/library/mongodb/dataloader/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/mangodb/dataloader)
* Chapitre 7 : [src/main/java/com/library/mongodb/crud/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/mangodb/crud)
* Chapitre 8 : [src/main/java/com/library/mongodb/manager/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/mangodb/manager)

#### RavenDB

* Chapitre 6 : [src/main/java/com/library/ravendb/dataloader/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/ravendb/dataloader)
* Chapitre 7 : [src/main/java/com/library/ravendb/crud/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/ravendb/crud)
* Chapitre 8 : [src/main/java/com/library/ravendb/manager/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/src/main/java/com/library/mangodb/manager)

### 6. Comparaison MongoDB / moteur NoSQL choisi

* Dossier : [comparaison/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/comparaison)

### 7. Cluster MongoDB pas à pas

* Dossier : [mongo-cluster/](https://github.com/exgael/LibraryManagementBigDataProject/tree/main/mongo-cluster)
* Scripts, rapport et étapes de construction du cluster.

## Nettoyage

```bash
docker stop mongo-community ravendb
docker rm mongo-community ravendb
docker volume rm ravendb-data
```