/* eslint-disable */
const fs = require('fs');
const templates = require('./new-component-template');

// 1. get component name
const componentName = process.argv[2];
if (!componentName) {
    console.error('\x1b[31m%s\x1b[0m', `Please supply a valid component name!`);
    process.exit(1);
}

// 2. verify component name starts with uppercase with no spacing
if (componentName[0] !== componentName[0].toUpperCase()) {
    console.error('\x1b[31m%s\x1b[0m', `Invalid component name! Please ensure first letter is uppercase.`);
    process.exit(1);
}
if (componentName.indexOf(' ') >= 0) {
    console.error('\x1b[31m%s\x1b[0m', `Invalid component name! Please ensure there are no white spaces in component name.`);
    process.exit(1);
}

// 3. create component
console.info('\x1b[32m%s\x1b[0m', 'Creating component template with name', componentName, '...');
const componentDirectory = `./src/components/${componentName}`;

if (fs.existsSync(componentDirectory)) {
    console.error('\x1b[31m%s\x1b[0m', `Component ${componentName} already exists!`);
    process.exit(1);
}

fs.mkdirSync(componentDirectory);

const generatedTemplates = templates.map(template => template(componentName));
generatedTemplates.forEach(template => {
    fs.writeFileSync(
        `${componentDirectory}/${componentName}${template.extension}`,
        template.content
    );
});

// 4. append component to index.ts
const indexFilePath = './src/index.ts';
fs.appendFileSync(indexFilePath, `export { default as ${componentName} } from './components/${componentName}/${componentName}';`);

// 5. print success
console.info('\x1b[32m%s\x1b[0m', 'Successfully created component under: ', componentDirectory);
