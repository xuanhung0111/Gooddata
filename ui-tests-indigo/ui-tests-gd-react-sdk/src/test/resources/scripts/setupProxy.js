const proxy = require('http-proxy-middleware');

module.exports = function (app) {
     app.use(proxy("/gdc", {
         "changeOrigin": true,
         "cookieDomainRewrite": "localhost",
         "secure": false,
         "target": "http://replaceWithTestingHost",
         "headers": {
             "host": "replaceWithTestingHost",
             "origin": null
         }
     }));
     app.use(proxy("/*.html", {
         "changeOrigin": true,
         "secure": false,
         "target": "http://replaceWithTestingHost"
     }));
     app.use(proxy("/packages/*.{js,css}", {
         "changeOrigin": true,
         "secure": false,
         "target": "http://replaceWithTestingHost"
     }));
 };
