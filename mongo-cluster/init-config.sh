#!/bin/bash

# Nom du premier membre du config server replica set
CONFIG_SVR_PRIMARY_HOST="configsvr1:27017"
# Nom du Replica Set des config servers
CONFIG_REPL_SET_NAME="configReplSet"
# Membres du Replica Set (noms de service:port interne)
CONFIG_MEMBER_0="configsvr1:27017"
CONFIG_MEMBER_1="configsvr2:27017"
CONFIG_MEMBER_2="configsvr3:27017"

echo "**********************************************"
echo " Waiting for ${CONFIG_SVR_PRIMARY_HOST} to be ready..."
echo "**********************************************"

# Boucle jusqu'à ce que mongosh puisse se connecter et exécuter une commande simple
# L'option --quiet supprime les messages de bienvenue etc, mais mongosh
# renverra un code d'erreur != 0 s'il ne peut pas se connecter.
    until mongosh --host ${CONFIG_SVR_PRIMARY_HOST} --quiet --eval "print('Connection attempt successful')" > /dev/null 2>&1
do
    echo "Config server ${CONFIG_SVR_PRIMARY_HOST} not ready yet, retrying in 2 seconds..."
sleep 2
done

echo "**********************************************"
echo " ${CONFIG_SVR_PRIMARY_HOST} is ready."
echo " Initiating Config Server Replica Set (${CONFIG_REPL_SET_NAME})..."
echo "**********************************************"

# Utilisation d'un "here document" (<<EOF) pour passer la commande d'initialisation
mongosh --host ${CONFIG_SVR_PRIMARY_HOST} <<EOF
rs.initiate(
    {
        _id: "${CONFIG_REPL_SET_NAME}",
        configsvr: true,
        members: [
            { _id: 0, host: "${CONFIG_MEMBER_0}" },
            { _id: 1, host: "${CONFIG_MEMBER_1}" },
            { _id: 2, host: "${CONFIG_MEMBER_2}" }
        ]
    }
)
EOF

# Vérification simple (optionnelle mais utile)
# Attend quelques secondes pour que l'initiation prenne effet et qu'un primaire soit élu
echo "Waiting a few seconds for replica set to stabilize..."
sleep 5

echo "**********************************************"
echo " Config Server Replica Set initiation command sent."
echo " Checking status (may take a moment for primary election):"
echo "**********************************************"
mongosh --host ${CONFIG_SVR_PRIMARY_HOST} --quiet --eval "rs.status()"

echo "**********************************************"
echo " Config Server Setup Script Finished."
echo "**********************************************"