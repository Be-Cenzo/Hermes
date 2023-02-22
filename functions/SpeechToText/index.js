const fs = require("fs");
var qs = require('querystring');
var sdk = require("microsoft-cognitiveservices-speech-sdk");
const os = require('os');

module.exports = function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');
    let b = qs.parse(req.body);
    let form = JSON.parse(JSON.stringify(b));
    const body =  form.file;
    const srcLang = form.srcLang;
    const dstLang = form.dstLang;
    if (!body) {
        context.res = {
            status: 400,
            body: "Please pass an audio in the body"
        };
    }
    else {

        var speechkey = req.headers['x-speech-key'];
        var region = "westeurope";

        let speechConfig = sdk.SpeechConfig.fromSubscription(speechkey, region);
        speechConfig.setProfanity(sdk.ProfanityOption.Raw);
        speechConfig.speechRecognitionLanguage = srcLang;

        let file = Buffer.from(body, 'base64');
        
        const tempFile = os.tmpdir() + "\\temp" + Math.floor(Math.random() * 10000000); +".wav"; 
        console.log(tempFile);
        
        fs.writeFileSync(tempFile, file);

        let stat = fs.statSync(tempFile);
        console.log(stat.size);
        let audioConfig = sdk.AudioConfig.fromWavFileInput(fs.readFileSync(tempFile));

        let speechRecognizer = new sdk.SpeechRecognizer(speechConfig, audioConfig);

        console.log("Esecuzione");

        speechRecognizer.recognizeOnceAsync(result => {
            console.log(result);
            switch (result.reason) {
                case sdk.ResultReason.RecognizedSpeech:
                    console.log(`RECOGNIZED: Text=${result.text}`);
                    context.res = {
                        body: result.text
                    };
                    context.done();
                    break;
                case sdk.ResultReason.NoMatch:
                    console.log("NOMATCH: Speech could not be recognized.");
                    context.res = {
                        body: "Riconoscimento fallito!"
                    };
                    context.done();
                    break;
                case sdk.ResultReason.Canceled:
                    const cancellation = sdk.CancellationDetails.fromResult(result);
                    console.log(`CANCELED: Reason=${cancellation.reason}`);
    
                    if (cancellation.reason == sdk.CancellationReason.Error) {
                        console.log(`CANCELED: ErrorCode=${cancellation.ErrorCode}`);
                        console.log(`CANCELED: ErrorDetails=${cancellation.errorDetails}`);
                        console.log("CANCELED: Did you set the speech resource key and region values?");
                    }
                    context.res = {
                        status: 400,
                        body: "Errore nella richiesta."
                    };
                    context.done();
                    break;
                default:
                    console.log("non ho idea di cosa stia succedendo");
                    context.res = {
                        status: 400,
                        body: "Errore nella richiesta."
                    };
                    break;
            }
            speechRecognizer.close();
            fs.unlinkSync(tempFile);
        },
        error => {
            console.log("Errore:");
            console.log(error);
            context.res = {
                status: 400,
                body: "Errore nella richiesta."
            };
            fs.unlinkSync(tempFile);
        });
        
    }
}