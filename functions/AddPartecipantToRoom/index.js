const { ChatClient } = require('@azure/communication-chat');
const { AzureCommunicationTokenCredential } = require('@azure/communication-common');
const { CommunicationIdentityClient } = require('@azure/communication-identity');

module.exports = async function (context, req) {

    let hermesId = req.body.hermesId;
    let threadId = req.body.threadId;
    const connectionString = req.body.connString;
    let userId = req.body.userId;
    let displayName = req.body.displayName;
    let token = req.body.token;


    console.log(hermesId);
    console.log(userId);
    console.log(connectionString);
    console.log(threadId);
        
    // Your unique Azure Communication service endpoint
    let endpointUrl = 'https://hermeschat.communication.azure.com/';
    // The user access token generated as part of the pre-requisites

    let tokenClient = new CommunicationIdentityClient(connectionString);
    //let hermesIDHolder = await tokenClient.createUser(hermesId);
    let hermesAccessToken = (await tokenClient.getToken({communicationUserId: hermesId}, ["chat"])).token;

    //console.log(hermesIDHolder.communicationUserId);

    let credential = new AzureCommunicationTokenCredential(hermesAccessToken);

    //console.log(credential);
    let chatClient = new ChatClient(endpointUrl, credential);
    let threads = chatClient.listChatThreads();
    /*console.log('Azure Communication Chat client created!');
    for await (let thread of threads) {
        console.log(thread);
     }*/

    console.log("Kitammuort");
    let chatThreadClient = chatClient.getChatThreadClient(threadId);
    console.log(chatThreadClient);
    const addParticipantsRequest =
    {
    participants: [
        {
            id: { communicationUserId: hermesId },
            displayName: "Hermes"
        },
        {
            id: { communicationUserId: userId },
            displayName: displayName
        }
    ]
    };

    let result = await chatThreadClient.addParticipants(addParticipantsRequest);
    console.log(result);
    
    context.res = {
        body: 'aggiunto'
    };

}