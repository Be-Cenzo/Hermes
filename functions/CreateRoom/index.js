const { MongoClient } = require("mongodb");
const { v4: uuidv4 } = require('uuid');

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

    var longitude = parseFloat(context.req.body.longitude);
    var latitude = parseFloat(context.req.body.latitude);
    var nome = context.req.body.nome;
    var descrizione = context.req.body.descrizione;
    var threadId = context.req.body.threadId;
    var roomId = uuidv4().toString();

    const result = await db.collection('Rooms').insertOne(
        {_id: roomId, type:"Feature",geometry:{type:"Point",coordinates:[longitude, latitude]},properties:{Name:nome,Description: descrizione, threadId: threadId, roomId: roomId}}
        );
    console.log(result);

    context.res = {
        body: roomId
    };
}