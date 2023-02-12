const axios = require('axios').default;

module.exports = function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');

    let location = "westeurope";
    let endpoint = "https://" + location + ".tts.speech.microsoft.com/";
    let key = req.headers['x-speech-key'];



    axios({
        baseURL: endpoint,
        url: 'cognitiveservices/voices/list',
        method: 'get',
        headers: {
            'Ocp-Apim-Subscription-Key': key,
            'Content-type': 'application/json',
        },
        responseType: 'json'
    }).then((response) => {
        let lastLocal = "";
        let voicesList = [];
        for(let i in response.data){
            let x = response.data[i];
            if(x.Locale !== lastLocal){
                lastLocal = x.Locale;
                let singleVoice = {
                    code: x.Locale,
                    label: x.LocaleName,
                    voice: {
                        voiceLabel: x.DisplayName,
                        voiceCode: x.ShortName,
                        voiceGender: x.Gender
                    }
                }
                voicesList.push(singleVoice);
            }
        }
        context.res = {
            'Content-type': 'application/json',
            body: voicesList
        };
        context.done();
    })
    .catch((err) => {
        console.log(err);
        context.res = {
            status: 400,
            body: err.response.data.error.message
        };
        context.done();
    })
}