/* eslint-disable */
module.exports = (componentName) => ({
    content: `import * as React from 'react';
import { Meta, Story } from '@storybook/react/types-6-0';
import ${componentName} from './${componentName}';
import { ${componentName}Props } from './${componentName}.types';

export default {
    title: 'Components/${componentName}',
    component: ${componentName},
    excludeStories: /.*Data$/,
} as Meta;

// create a template
const Template: Story<${componentName}Props> = (args) => {
    return <${componentName} {...args} />;
};

/** *************************************************
 * Data
 ************************************************** */

const sampleArgs: ${componentName}Props = {
    title: 'New Component Created --> ${componentName}'
};

/** *************************************************
 * Stories
 ************************************************** */

// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
export const Basic: Story = Template.bind({});
Basic.args = { ...sampleArgs };

`,
    extension: `.stories.tsx`,
});
