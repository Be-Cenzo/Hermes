const { MongoClient } = require("mongodb");

module.exports = function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');

    let km = parseInt(req.query.km);
    let long = parseFloat(req.query.long);
    let lat = parseFloat(req.query.lat);

    km = km*1000;
    console.log(km);

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

    client.connect()
    .then(() => {

      const collection = client.db("Rooms").collection("Rooms");

      const rooms = {
          "type": "FeatureCollection",
          "features": []
          };
  
        
      collection.createIndex({geometry: "2dsphere"})
      .then(() =>  {
        const cursor = collection.find({ "geometry": { $near: {$geometry: { type: "Point", coordinates: [long, lat]}, $maxDistance: km} } } );
        cursor.forEach((document) => {
            rooms.features.push(document);
            console.log(document);
        })
        .then(() => {
          context.res = {
            headers: {
                'Content-Type': 'application/json'
            },
            body: rooms
        };
        
        context.done();
        })
        .catch(() => {
          context.res = {
            status: 400
          };
          context.done();
        });
    
        console.log(rooms);
      }).catch(() => {
        context.res = {
          status: 400
        };
        context.done();
      });

    })
    .catch(() => {
      context.res = {
        status: 400
      };
      context.done();
    });
   
}