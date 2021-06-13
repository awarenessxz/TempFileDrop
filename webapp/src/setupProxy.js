const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    app.use(createProxyMiddleware("/swagger/storagesvc/**", { target: "http://localhost:8001", changeOrigin: true, secure: false, pathRewrite: { "^/swagger/storagesvc": "" }}));
    app.use(createProxyMiddleware("/api/storagesvc/**", { target: "http://localhost:8001", changeOrigin: true, secure: false }));
    app.use(createProxyMiddleware("/api/**", { target: "http://localhost:7001", changeOrigin: true, secure: false }));
    app.use(createProxyMiddleware("/auth/**", { target: "http://localhost:8080", changeOrigin: true, secure: false }));
    app.use(createProxyMiddleware("/ws/**", { target: "ws://localhost:7001", ws: true, pathRewrite: { "^/ws": "" } }));
};