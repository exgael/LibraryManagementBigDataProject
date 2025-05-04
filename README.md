# Comparaison entre MongoDB et RavenDB

```
docker run --name mongo-community -p 27017:27017 -d mongodb/mongodb-community-server:latest
```

```
docker run -d --name ravendb -p 8080:8080 -p 38888:38888 -v ravendb-data:/opt/RavenDB/Server/RavenData ravendb/ravendb:latest
```
## Les résultats de l’étude

### 1. Un vidéo (contexte, démo) obligatoire de 10 minutes environ présentant pas à pas travail

Dossier: `video`

### 2. Rapport PDF contenant les résultats des chapitres 1, 2, 3, 4 et 5.

Dossier: `rapport`

### 3. Le dossier des objets générées

Dossier: `generated-data`

Noté que ce dossier est utilisé par le générateur de donnée. 
Donc sujet à changer.

### 4. Les scripts de chargement des objets

MangoDB -> Dossier: `src/main/java/com/library/mangodb/dataloader`

RavenDB -> Dossier: `src/main/java/com/library/ravendb/dataloader`

Executer les classes mains.

### 5. Les sources java contenant les résultats des chapitres 6, 7 et 8

#### MangoDB
Chapitre 6 -> Dossier: `src/main/java/com/library/mangodb/dataloader`

Chapitre 7 -> Dossier: `src/main/java/com/library/mangodb/crud`

Chapitre 8 -> Dossier: `src/main/java/com/library/mangodb/manager`

#### RavenDB
Chapitre 6 -> Dossier: `src/main/java/com/library/ravendb/dataloader`

Chapitre 7 -> Dossier: `src/main/java/com/library/ravendb/crud`

Chapitre 8 -> Dossier: `src/main/java/com/library/ravendb/manager`

### 6. Dossier de comparaison MONGODB et du moteur NOSQL de votre choix

Dossier: `comparaison`

### 7. Les scripts, le rapport, la construction par étape du cluster MONGODB

Dossier: `mongo-cluster`
