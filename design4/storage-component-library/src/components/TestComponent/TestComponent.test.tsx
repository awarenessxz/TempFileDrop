import * as React from 'react';
import 'whatwg-fetch';
import { rest } from 'msw';
import { setupServer } from 'msw/node';
import { render, RenderResult, screen } from '@testing-library/react';

// Import Component
import TestComponent from './TestComponent';
import { TestComponentProps } from './TestComponent.types';

/*********************************************************************
 * Configuration / Helper Functions
 ********************************************************************/

// set up mock server for rest api end point
const server = setupServer(
    rest.get('/mock/test/data', (req, res, ctx) => {
        return res(ctx.text('Hello, Mock Data!'));
    }),
);
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// function to render Component before each test
type PartialTestComponentProps = Partial<TestComponentProps>;
const renderComponent = ({ ...props }: PartialTestComponentProps = {}): RenderResult => {
    const defaultProps: TestComponentProps = {
        theme: 'primary',
        apiUrl: '/mock/test/data'
    };
    const merged = { ...defaultProps, ...props };
    return render(<TestComponent {...merged} />);
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

    // Testing Dom Elements: check if renders with correct value (Note: the usage of data-testid in component)
    describe('Testing if component renders with correct state/value', () => {
        it('should have primary className with default props', () => {
            const { getByTestId } = renderComponent();
            const testComponent = getByTestId('test-component');
            expect(testComponent).not.toHaveClass('testComponentSecondary');
        });

        it('should have secondary className with theme set as secondary', () => {
            const { getByTestId } = renderComponent({ theme: 'secondary' });
            const testComponent = getByTestId('test-component');
            expect(testComponent).toHaveClass('testComponentSecondary');
        });
    });
});

// 2. Testing Event Listener
describe('Testing OnClick Events', () => {
    it('Testing API Button', async () => {
        renderComponent();
        screen.getByTestId('test-button').click()
        const message = await screen.findByText('Hello, Mock Data!');
        expect(message).toBeInTheDocument();
    });
});
