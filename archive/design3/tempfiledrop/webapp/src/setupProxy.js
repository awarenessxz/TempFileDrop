const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(createProxyMiddleware(["/api/**", "/swagger/**"], { target: "http://localhost:9090", changeOrigin: true, secure: false }));
    app.use(createProxyMiddleware("/auth/**", { target: "http://localhost:8080", changeOrigin: true, secure: false }));
};
