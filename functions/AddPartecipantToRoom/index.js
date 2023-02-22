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
    
    let endpointUrl = 'https://hermeschat.communication.azure.com/';

    let tokenClient = new CommunicationIdentityClient(connectionString);
    let hermesAccessToken = (await tokenClient.getToken({communicationUserId: hermesId}, ["chat"])).token;

    let credential = new AzureCommunicationTokenCredential(hermesAccessToken);

    let chatClient = new ChatClient(endpointUrl, credential);
    let threads = chatClient.listChatThreads();

    let chatThreadClient = chatClient.getChatThreadClient(threadId);
    console.log(chatThreadClient);
    for await (let partecipant of chatThreadClient.listParticipants()){
        console.log(partecipant);
    }
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