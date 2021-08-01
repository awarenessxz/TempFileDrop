/* eslint-disable */
module.exports = (componentName) => ({
    content: `import { Meta, Story, ArgsTable } from '@storybook/addon-docs/blocks';
import ${componentName} from './${componentName}';

<Meta title='Docs/${componentName}' component={${componentName}} />

# ${componentName}

`,
    extension: `.stories.mdx`,
});
