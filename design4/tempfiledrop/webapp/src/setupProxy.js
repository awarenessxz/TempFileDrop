const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(createProxyMiddleware(["/api/**", "/swagger/**", "/auth/**", "/logout/**", "/oauth2/authorization/**"],
        { target: "http://localhost:9090", changeOrigin: true, secure: false }));
};
