const fs = require("fs");
var qs = require('querystring');
var sdk = require("microsoft-cognitiveservices-speech-sdk");
const os = require('os');

class BufferAudioStream extends sdk.PullAudioInputStreamCallback {
    constructor(buffer) {
      super();
      this.buffer = buffer;
      this.index = 0;
    }
  
    close() {
        this.buffer = undefined;
        this.index = undefined;
    }
  
    read() {
      if (this.index >= this.buffer.length) {
        return null;
      }
  
      const chunkSize = Math.min(1024, this.buffer.length - this.index);
      const chunk = this.buffer.slice(this.index, this.index + chunkSize);
      this.index += chunkSize;
  
      return chunk;
    }
  }

module.exports = async function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');
    //console.log(context);
    //console.log(req);
    let b = qs.parse(req.body);
    //console.log(b);
    let temp = JSON.parse(JSON.stringify(b));
    //console.log("\n\n\n\n");
    //console.log(temp);
    const body =  temp.file;//temp.file;
    //console.log(body);
    if (!body) {
        context.res = {
            status: 400,
            body: "Please pass an audio in the body"
        };
    }
    else {

        var speechkey = req.headers['x-speech-key'];
        //var speechkey = "c8d93f92692c45c1a9e918bb04448686";//req.headers['x-speech-key'];
        var region = "westeurope";

        let speechConfig = sdk.SpeechConfig.fromSubscription(speechkey, region);
        speechConfig.speechRecognitionLanguage = "it-IT";

        //fs.writeFileSync('myFile.wav', body);

        /*let audioStream = new AudioStream({
            buffer: body
        });*/

        //console.log(req.files);


        let file = Buffer.from(body, 'base64'); //Buffer.from(body, 'base64');//body;// Buffer.from(body);//  //
        
        const tempFile = os.tmpdir() + "\\temp" + Math.floor(Math.random() * 10000000); +".wav"; //os.homedir()

        //const tempFile = "D:\\home\\temp.wav"

        
        console.log(tempFile);
        
        fs.writeFileSync(tempFile, file);

        let stat = fs.statSync(tempFile);
        console.log(stat.size);

        //let audioInputStream = new sdk.AudioInputStream();

        /*let audioInputStream = sdk.AudioInputStream.createPushStream({
            format: sdk.AudioStreamFormat.getWaveFormatPCM(16000, 16, 1),
          });

        audioInputStream.write(file.buffer);*/

        //let audioConfig = sdk.AudioConfig.fromStreamInput(new BufferAudioStream(file.buffer));
        //let audioConfig = sdk.AudioConfig.fromWavFileInput(fs.readFileSync(tempFile));

        let pushStream = sdk.AudioInputStream.createPushStream();
        pushStream.write(file.buffer);

        fs.createReadStream(tempFile).on('data', function(arrayBuffer) {
            pushStream.write(arrayBuffer.slice());
        }).on('end', function() {
            pushStream.close();
        });

        let audioConfig = sdk.AudioConfig.fromStreamInput(pushStream);

        let speechRecognizer = new sdk.SpeechRecognizer(speechConfig, audioConfig);

        let buf = fs.readFileSync(tempFile);
        //buf = Buffer.from(body, 'base64').toString('utf-8');
        let i = 0;
        for(i = 0; i<50; i++){
            console.log(buf[i]);
            if(i === 43)
                console.log("finito l'header");
        }
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