const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();

// Proxy /api vers le backend
app.use('/api', (req, res, next) => {
    console.log('Proxy request:', req.method, req.url);
    next();
}, createProxyMiddleware({ target: 'http://localhost:8080/api', changeOrigin: true }));

// Servir les fichiers statiques Angular
app.use(express.static(path.join(__dirname, 'dist/front/browser')));

// Fallback Angular pour toutes les routes
app.use((req, res) => {
    res.sendFile(path.join(__dirname, 'dist/front/browser/index.html'));
});

const PORT = 8081;
app.listen(PORT, () => {
    console.log(`Front prod avec proxy sur http://localhost:${PORT}`);
});
