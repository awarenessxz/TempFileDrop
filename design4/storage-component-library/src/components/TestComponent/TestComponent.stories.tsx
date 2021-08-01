import * as React from 'react';
import { Meta } from '@storybook/react/types-6-0';
import TestComponent from './TestComponent';

export default {
    title: 'Components/TestComponent',
    component: TestComponent,
} as Meta;

/** *************************************************
 * Stories
 ************************************************** */

export const Primary = (): JSX.Element => <TestComponent theme="primary" apiUrl="/api/test/data" />;

export const Secondary = (): JSX.Element => <TestComponent theme="secondary" apiUrl="/api/test/data" />;
