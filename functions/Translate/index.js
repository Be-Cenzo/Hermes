const axios = require('axios').default;
const { v4: uuidv4 } = require('uuid');

module.exports = function (context, req) {
    context.log('JavaScript HTTP trigger function processed a request.');
    let key = req.headers['x-translate-key'];
    let endpoint = "https://api.cognitive.microsofttranslator.com";
    let text = req.query.text;
    let from = req.query.from;
    let to = req.query.to;

    let location = "westeurope";

    axios({
        baseURL: endpoint,
        url: '/translate',
        method: 'post',
        headers: {
            'Ocp-Apim-Subscription-Key': key,
            'Ocp-Apim-Subscription-Region': location,
            'Content-type': 'application/json',
            'X-ClientTraceId': uuidv4().toString()
        },
        params: {
            'api-version': '3.0',
            'from': from,
            'to': to
        },
        data: [{
            'text': text
        }],
        responseType: 'json'
    }).then(function(response){
        console.log(JSON.stringify(response.data, null, 4));
        context.res = {
            body: response.data[0].translations[0].text
        };
        context.done()
    })
    .catch((err) => {
        console.log(err.response.data);
        context.res = {
            status: 400,
            body: err.response.data.error.message
        };
        context.done()
    })

    
}