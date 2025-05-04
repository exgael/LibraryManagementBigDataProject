#!/bin/bash

# Adresse du routeur mongos (telle qu'exposée sur l'hôte)
MONGOS_HOST="localhost:27023"

# Connection strings pour chaque shard replica set
# Format: "replicaSetName/member1:port,member2:port,member3:port"
# Utilise les noms de service internes et le port interne 27017
SHARD1_CONN_STRING="shard1ReplSet/shard1a:27017,shard1b:27017,shard1c:27017"
SHARD2_CONN_STRING="shard2ReplSet/shard2a:27017,shard2b:27017,shard2c:27017"
SHARD3_CONN_STRING="shard3ReplSet/shard3a:27017,shard3b:27017,shard3c:27017"

echo "**********************************************"
echo " Adding Shards to the Cluster via Mongos"
echo " IMPORTANT: Ensure Replica Sets are initiated and have primaries."
echo " Waiting 15 seconds for replica sets to stabilize..."
echo "**********************************************"
sleep 15

echo "**********************************************"
echo " Waiting for Mongos (${MONGOS_HOST}) to be ready..."
echo "**********************************************"

# Boucle jusqu'à ce que mongosh puisse se connecter à mongos
until mongosh --host ${MONGOS_HOST} --quiet --eval "print('Mongos connection attempt successful')" > /dev/null 2>&1
do
   echo "Mongos at ${MONGOS_HOST} not ready yet, retrying in 2 seconds..."
   sleep 2
done

echo "**********************************************"
echo " Mongos (${MONGOS_HOST}) is ready."
echo " Adding Shards..."
echo "**********************************************"

mongosh --host ${MONGOS_HOST} <<EOF
print("Adding Shard 1: ${SHARD1_CONN_STRING}");
sh.addShard("${SHARD1_CONN_STRING}");

print("Adding Shard 2: ${SHARD2_CONN_STRING}");
sh.addShard("${SHARD2_CONN_STRING}");

print("Adding Shard 3: ${SHARD3_CONN_STRING}");
sh.addShard("${SHARD3_CONN_STRING}");

print("Waiting briefly for shard addition process...");
sleep(3000); // Attendre 3 secondes

print("Cluster status after adding shards:");
sh.status();
EOF

echo "**********************************************"
echo " Shard Addition Script Finished."
echo " Check the output of sh.status() above."
echo "**********************************************"