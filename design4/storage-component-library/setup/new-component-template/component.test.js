/* eslint-disable */
module.exports = (componentName) => ({
    content: `import * as React from 'react';
import { render, RenderResult } from '@testing-library/react';

// Import Component
import ${componentName} from './${componentName}';
import { ${componentName}Props } from './${componentName}.types';

/*********************************************************************
 * Configuration / Helper Functions
 ********************************************************************/

// function to render Component before each test
type Partial${componentName}Props = Partial<${componentName}Props>;
const renderComponent = ({ ...props }: Partial${componentName}Props = {}): RenderResult => {
    const defaultProps: ${componentName}Props = {
        title: '${componentName}',
    };
    const merged = { ...defaultProps, ...props };
    return render(<${componentName} {...merged} />);
};

/*********************************************************************
 * Test Cases
 ********************************************************************/

// 1. Testing if component renders properly
describe('Testing if component renders properly', () => {
    // Snapshot Testing
    it('Snapshot Testing', () => {
        const { asFragment } = renderComponent();
        expect(asFragment()).toMatchSnapshot();
    });
});

`,
    extension: `.test.tsx`,
});
