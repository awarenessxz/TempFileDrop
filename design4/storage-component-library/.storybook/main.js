module.exports = {
    stories: ['../src/**/*.stories.mdx', '../src/**/*.stories.@(ts|tsx)'],
    addons: [
        {
            name: '@storybook/preset-scss',
            options: {
                cssLoaderOptions: {
                    modules: true,
                },
            },
        },
        '@storybook/addon-postcss',
        '@storybook/addon-links',
        '@storybook/addon-essentials',
    ],
    typescript: {
        check: false,
        checkOptions: {},
        reactDocgen: 'react-docgen-typescript',
        reactDocgenTypescriptOptions: {
            shouldExtractLiteralValuesFromEnum: true,
            propFilter: (prop) => (prop.parent ? !/node_modules/.test(prop.parent.fileName) : true),
        },
    },
};
