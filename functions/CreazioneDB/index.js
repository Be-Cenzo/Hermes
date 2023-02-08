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

    const result = await db.collection('Rooms').insertOne(
    {type:"Feature",geometry:{type:"Point",coordinates:[40.774424, 14.789417]},properties:{Name:"Room di Prova",Description: "descrizione"}}
    );
    console.log(result);

    const cursor = collection.find();
    await cursor.forEach(console.log);

    context.res = {
        body: "ok"
    };
}