/* eslint-disable */
module.exports = (componentName) => ({
    content: `export interface ${componentName}Props {
    title: string;
}

`,
    extension: `.types.ts`,
});
