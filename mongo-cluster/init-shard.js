// Init shard1
rs.initiate({
    _id: "shard1ReplSet",
    members: [
        { _id: 0, host: "shard1:27020" }
    ]
})

// Attends un peu que les replica sets soient bien prêts
sleep(5000)

// Init shard2
rs.initiate({
    _id: "shard2ReplSet",
    members: [
        { _id: 0, host: "shard2:27021" }
    ]
})

// Attends un peu
sleep(5000)

// Init shard3
rs.initiate({
    _id: "shard3ReplSet",
    members: [
        { _id: 0, host: "shard3:27022" }
    ]
})

// Attends un peu
sleep(5000)

// Ajout des shards au mongos
sh.addShard("shard1ReplSet/shard1:27020")
sh.addShard("shard2ReplSet/shard2:27021")
sh.addShard("shard3ReplSet/shard3:27022")
