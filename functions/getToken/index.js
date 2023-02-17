const { CommunicationIdentityClient } = require('@azure/communication-identity');
const acsEndpoint = "https://hermeschat.communication.azure.com/";

module.exports = async function (context, req) {
    const connectionString = req.body.connString;
    let userId = req.body.userId;
    let tokenClient = new CommunicationIdentityClient(connectionString);
    let userIDHolder;

    let userToken;

    if(userId === undefined || userId === null){
        userIDHolder = await tokenClient.createUser();
        userId = userIDHolder.communicationUserId
        
        userToken = await (await tokenClient.getToken(userIDHolder, ["chat"])).token;
    }
    else
        userToken = await (await tokenClient.getToken({communicationUserId: userId}, ["chat"])).token;


    context.res = {
        body: {
            acsEndpoint,
            userId,
            userToken
        }
    };
}