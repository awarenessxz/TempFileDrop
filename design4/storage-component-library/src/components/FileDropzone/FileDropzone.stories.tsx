import * as React from 'react';
import { Meta, Story } from '@storybook/react/types-6-0';
import FileDropzone from './FileDropzone';
import { FileDropzoneProps } from './FileDropzone.types';

export default {
    title: 'Components/FileDropzone',
    component: FileDropzone,
    excludeStories: /.*Data$/,
} as Meta;

// create a template
const Template: Story<FileDropzoneProps> = (args) => {
    return <FileDropzone {...args} />;
};

/** *************************************************
 * Data
 ************************************************** */

const sampleArgs: FileDropzoneProps = {
    uploadUrl: "/api/file-upload",
    showDrops: false,
    maxSizeInBytes: 5368706371 // 5GB
};

/** *************************************************
 * Stories
 ************************************************** */

// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
export const Basic: Story = Template.bind({});
Basic.args = { ...sampleArgs };

// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
export const ShowDrops: Story = Template.bind({});
ShowDrops.args = { ...sampleArgs, showDrops: true };
