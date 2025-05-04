#!/bin/bash

# Nom du premier membre du shard 3 replica set
SHARD_HOST="shard3a:27017"
# Nom du Replica Set du Shard 3
SHARD_REPL_SET_NAME="shard3ReplSet"
# Membres du Replica Set (noms de service:port interne)
SHARD_MEMBER_0="shard3a:27017"
SHARD_MEMBER_1="shard3b:27017"
SHARD_MEMBER_2="shard3c:27017"

echo "**********************************************"
echo " Initializing Shard 3: ${SHARD_REPL_SET_NAME}"
echo " Waiting for ${SHARD_HOST} to be ready..."
echo "**********************************************"

# Boucle jusqu'à ce que mongosh puisse se connecter
until mongosh --host ${SHARD_HOST} --quiet --eval "print('Shard 3 member ${SHARD_HOST} connection attempt successful')" > /dev/null 2>&1
do
   echo "Shard 3 member ${SHARD_HOST} not ready yet, retrying in 2 seconds..."
   sleep 2
done

echo "**********************************************"
echo " ${SHARD_HOST} is ready."
echo " Initiating Shard 3 Replica Set (${SHARD_REPL_SET_NAME})..."
echo "**********************************************"

mongosh --host ${SHARD_HOST} <<EOF
rs.initiate(
  {
    _id: "${SHARD_REPL_SET_NAME}",
    members: [
      { _id: 0, host: "${SHARD_MEMBER_0}" },
      { _id: 1, host: "${SHARD_MEMBER_1}" },
      { _id: 2, host: "${SHARD_MEMBER_2}" }
    ]
  }
)
EOF

# Attente et vérification (optionnel)
echo "Waiting a few seconds for Shard 3 replica set to stabilize..."
sleep 5
echo "**********************************************"
echo " Shard 3 Replica Set initiation command sent."
echo " Checking Shard 3 status:"
echo "**********************************************"
mongosh --host ${SHARD_HOST} --quiet --eval "rs.status()"

echo "**********************************************"
echo " Shard 3 Setup Script Finished."
echo "**********************************************"