const { createProxyMiddleware } = require('http-proxy-middleware');

/**
 *  /oauth2/authorization/storage-gateway --> Spring Security’s redirection to the Identity Provider first gets redirected
 *  here to make call the authorize endpoint for this particular client
 *
 *  /login/oauth2/code/storage-gateway --> that is where the Identity Provider sends back the code during the OAuth 2.0
 *  authorization_code dance
 *
 */

module.exports = function(app) {
    app.use(createProxyMiddleware(["/api/**", "/swagger/**", "/auth/**", "/logout/**", "/oauth2/authorization/**", "/login/oauth2/code/**"],
        { target: "http://localhost:9090", changeOrigin: true, secure: false }));
};
