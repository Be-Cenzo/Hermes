const os = require('os');
const sdk = require("microsoft-cognitiveservices-speech-sdk");
const { ProfanityOption } = require('microsoft-cognitiveservices-speech-sdk');

module.exports = function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');
    const text = req.query.text;
    const lang = req.query.lang;
    const voice = req.query.voice;
    if (!text) {
        context.res = {
            status: 400,
            body: "Please pass a text on the query string"
        };
    }
    else {
        const audioFile = os.tmpdir() + "\\temp" + Math.floor(Math.random() * 10000000); +".mp3";

        var speechkey = req.headers['x-speech-key'];
        var region = "westeurope";

        const speechConfig = sdk.SpeechConfig.fromSubscription(speechkey, region);
        const audioConfig = sdk.AudioConfig.fromAudioFileOutput(audioFile);

        console.log(voice);

        speechConfig.speechSynthesisVoiceName = voice;
        speechConfig.speechSynthesisOutputFormat =  sdk.OutputFormat.Mp3_64Kbps_16kHzMono;
        speechConfig.setProfanity(ProfanityOption.Raw);

        var synthesizer = new sdk.SpeechSynthesizer(speechConfig, audioConfig);

        synthesizer.speakTextAsync(text,
            function (result) {
        if (result.reason === sdk.ResultReason.SynthesizingAudioCompleted) {
            console.log("synthesis finished.");
            result.privAudioData = Buffer.from(result.privAudioData).toString('base64');
            var audio = {base64audio: result.privAudioData};
            context.res = {
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(audio)
            };
            console.log(result);
            context.done();
        } else {
            console.error("Speech synthesis canceled, " + result.errorDetails +
                "\nDid you set the speech resource key and region values?");
                context.res = {
                    headers: {
                        status: 400,
                        'Content-Type': 'text/html'
                    },
                    body: "Errore nella richiesta"
                };
                context.done()
        }
        synthesizer.close();
        synthesizer = null;
        },
            function (err) {
                console.trace("err - " + err);
                synthesizer.close();
                synthesizer = null;
                context.res = {
                    status: 400,
                    headers: {
                        'Content-Type': 'text/html'
                    },
                    body: "Errore nella richiesta"
                };
                context.done()
        });

        
        console.log("Now synthesizing to: " + audioFile);
        
    }
};