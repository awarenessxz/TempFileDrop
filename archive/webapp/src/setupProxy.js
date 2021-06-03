const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(createProxyMiddleware("/api/**", { target: "http://localhost:7001", changeOrigin: true, secure: false }));
    app.use(createProxyMiddleware("/storagesvc/api-docs", { target: "http://localhost:8001", changeOrigin: true, secure: false, pathRewrite: { "^/storagesvc": "" }, }));
};