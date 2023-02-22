const { ChatClient } = require('@azure/communication-chat');
const { AzureCommunicationTokenCredential } = require('@azure/communication-common');
const { CommunicationIdentityClient } = require('@azure/communication-identity');
const { MongoClient } = require("mongodb");

module.exports = async function (context, req) {

    let hermesId = context.req.body.hermesId;
    let threadId = context.req.body.threadId;
    let chatConnectionString = context.req.body.chatString;
    let dbConnectionString = context.req.body.connString;
    let userId = context.req.body.userId;
    let roomId = context.req.body.roomId;

    console.log(hermesId);
    console.log(userId);
    console.log(chatConnectionString);
    console.log(dbConnectionString);
    console.log(threadId);
    console.log(roomId);


    
    const client = new MongoClient(dbConnectionString);

    await client.connect();

    const db = await client.db("Rooms");
    
    let endpointUrl = 'https://hermeschat.communication.azure.com/';

    let tokenClient = new CommunicationIdentityClient(chatConnectionString);
    let hermesAccessToken = (await tokenClient.getToken({communicationUserId: hermesId}, ["chat"])).token;


    let credential = new AzureCommunicationTokenCredential(hermesAccessToken);
    let chatClient = new ChatClient(endpointUrl, credential);

    let chatThreadClient = chatClient.getChatThreadClient(threadId);
    
    let count = 0;
    for await (let partecipant of chatThreadClient.listParticipants()){
        console.log(partecipant);
        count++;
    }

    if(count <= 1){
        const result = await db.collection('Rooms').deleteOne({"_id": roomId})
        console.log(result);
    }

    
    context.res = {
        body: 'rimosso'
    };

}