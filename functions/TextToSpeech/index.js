module.exports = function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');
    const text = req.query.text;
    if (!text) {
        context.res = {
            status: 400,
            body: "Please pass a text on the query string"
        };
    }
    else {
        var sdk = require("microsoft-cognitiveservices-speech-sdk");


        var audioFile = "audio.mp3";

        var speechkey = req.headers['x-speech-key'];
        var region = "westeurope";
        // This example requires environment variables named "SPEECH_KEY" and "SPEECH_REGION"
        const speechConfig = sdk.SpeechConfig.fromSubscription(speechkey, region);
        const audioConfig = sdk.AudioConfig.fromAudioFileOutput(audioFile);

        // The language of the voice that speaks.
        speechConfig.speechSynthesisVoiceName = "it-IT-LisandroNeural"; 
        speechConfig.speechSynthesisOutputFormat =  sdk.OutputFormat.Mp3_64Kbps_16kHzMono;

        // Create the speech synthesizer.
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
                        'Content-Type': 'text/html'
                    },
                    body: "Errore nella richiesta"
                };
          }
          synthesizer.close();
          synthesizer = null;
        },
            function (err) {
          console.trace("err - " + err);
          synthesizer.close();
          synthesizer = null;
        })
        console.log("Now synthesizing to: " + audioFile);

        //console.log(synthetized);
        
    }
};