const path = require('path');

module.exports = {
    module: {
        rules: [
            {
                test: /\.(ts|js)$/,
                include: [path.join(__dirname, 'src')],
                exclude: [
                    /node_modules/,
                    /\.spec\.(ts|js)$/,
                    /\.jest\.spec\.(ts|js)$/,
                    /\.cy\.(ts|js)$/,
                    path.join(__dirname, 'cypress'),
                ],
                enforce: 'post',
                use: {
                    loader: '@jsdevtools/coverage-istanbul-loader',
                    options: {
                        esModules: true,
                    },
                },
            },
        ],
    },
};
