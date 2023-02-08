const { MongoClient } = require("mongodb");




module.exports = async function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');

    let connectionString = "";

    if(context.req.query.str !== undefined)
      connectionString = context.req.query.str;
    else if(context.req.body.connString !== undefined)
      connectionString = context.req.body.connString;
    else{
        context.res = {
          status: 401
        };
        context.done();
    }
    
    const client = new MongoClient(connectionString);

    await client.connect();
    

    const db = await client.db("Rooms");

    const collection = await db.collection("Rooms");

    const rooms = {
        "type": "FeatureCollection",
        "features": []
        };

    const cursor = collection.find();
    await cursor.forEach((document) => {
        rooms.features.push(document);
    });

    console.log(rooms);

    context.res = {
        headers: {
            'Content-Type': 'application/json'
        },
        body: rooms
    };
    
    context.done();
}