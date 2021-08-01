import * as React from 'react';
import { Meta, Story } from '@storybook/react/types-6-0';
import StorageFileDropzone from './StorageFileDropzone';
import { StorageFileDropzoneProps } from './StorageFileDropzone.types';

export default {
    title: 'Components/StorageFileDropzone',
    component: StorageFileDropzone,
    excludeStories: /.*Data$/,
} as Meta;

// create a template
const Template: Story<StorageFileDropzoneProps> = (args) => {
    return <StorageFileDropzone {...args} />;
};

/***************************************************
 * Data
 ************************************************** */

const sampleArgs: StorageFileDropzoneProps = {
    showDrops: false,
    showConfigs: false,
    maxSizeInBytes: 5368706371, // 5GB
    isAnonymousUpload: false,
    uploadMetadata: {
        bucket: "example",
        storagePath: "/example",
        eventRoutingKey: "example",
        eventData: JSON.stringify({ example: "example" })
    }
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

// eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
export const ShowConfigs: Story = Template.bind({});
ShowConfigs.args = { ...sampleArgs, showConfigs: true };
