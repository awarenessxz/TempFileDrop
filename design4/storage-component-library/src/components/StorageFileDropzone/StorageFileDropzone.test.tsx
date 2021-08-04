import * as React from 'react';
import { act, cleanup, fireEvent, render, screen, waitFor, RenderResult } from '@testing-library/react';
import { StorageFileDropzoneProps } from './StorageFileDropzone.types';
import { setupServer } from "msw/node";
import { rest } from "msw";

// Import Component
import StorageFileDropzone from './StorageFileDropzone';

/*********************************************************************
 * Configuration / Helper Functions
 ********************************************************************/

// set up mock server for rest api end point
const server = setupServer(
    rest.post('/api/storagesvc/anonymous/upload', (req, res, ctx) => {
        return res(ctx.json({
            message: "Upload Success",
            storageIdList: ["example-storage-id"],
            storagePathList: ["example/storage/path.json"],
        }));
    }),
    rest.post('/api/storagesvc/upload', (req, res, ctx) => {
        return res(ctx.json({
            message: "Upload Success",
            storageIdList: ["example-storage-id"],
            storagePathList: ["example/storage/path.json"],
        }));
    }),
);
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// generate ui
type PartialFileDropzoneProps = Partial<StorageFileDropzoneProps>;
const generateUI = ({ ...props }: PartialFileDropzoneProps = {}): React.ReactElement => {
    const defaultProps: StorageFileDropzoneProps = {
        uploadMetadata: {
            bucket: "test",
            storagePath: "/test",
            eventRoutingKey: "test",
            eventData: JSON.stringify({ test: "test" })
        },
        maxSizeInBytes: 10000
    };
    const merged = { ...defaultProps, ...props };
    return <StorageFileDropzone {...merged} />;
};

// function to render Component before each test
const renderComponent = (ui: React.ReactElement): RenderResult => {
    return render(ui);
};

const flushPromises = async (rerender: (ui: React.ReactElement) => void, ui: React.ReactElement) => {
    await act(() => waitFor(() => rerender(ui)));
};

const dispatchEvt = (node: any, type: string, data: any) => {
    const event = new Event(type, { bubbles: true });
    Object.assign(event, data);
    fireEvent(node, event);
};

const createMockData = (files: File[]) => {
    return {
        dataTransfer: {
            files,
            items: files.map(file => ({
                kind: 'file',
                type: file.type,
                getAsFile: () => file
            })),
            types: ['Files']
        }
    };
};

const createFile = (name: string, size: number, type: string) => {
    const file = new File([], name, { type })
    Object.defineProperty(file, 'size', {
        get() {
            return size;
        }
    });
    return file;
};

/*********************************************************************
 * Test Cases
 ********************************************************************/

// 1. Testing if component renders properly
describe('Testing if component renders properly', () => {
    // Snapshot Testing
    it('Snapshot Testing', () => {
        const ui = generateUI();
        const { asFragment } = renderComponent(ui);
        expect(asFragment()).toMatchSnapshot();
    });

    // Dropzone render properly
    it("Dropzone render properly", () => {
        const ui = generateUI();
        const { container } = renderComponent(ui);
        const dropzone = container.querySelector('#dropzone');
        expect(dropzone).toBeInTheDocument();
    });

    // Config render properly
    it("Config render properly", () => {
        const ui = generateUI({ showConfigs: true });
        renderComponent(ui);
        expect(screen.queryByText("Upload Settings")).toBeInTheDocument();
        expect(screen.queryByText("Expiry Period")).toBeInTheDocument();
        expect(screen.queryByText("Allow Anonymous Download?")).toBeInTheDocument();
        expect(screen.queryByPlaceholderText("Maximum number of downloads (Default = 1)")).toBeInTheDocument();
    });
});

// 2. Testing Upload Functionality
describe("Testing Upload Functionality", () => {
    let file: File[];
    let files: File[];
    let largeFile: File[];

    beforeEach(() => {
        file = [createFile('file1.pdf', 1111, 'application/pdf')];
        files = [createFile('file1.pdf', 1234, 'application/pdf'), createFile('dogs.gif', 2345, 'image/jpeg')];
        largeFile = [createFile('large.pdf', 20000, 'application/pdf')];
    });

    afterEach(cleanup);

    describe("Drop File", () => {
        it("Drop Single File Successfully", async () => {
            const data = createMockData(file);
            const ui = generateUI();
            const { container, rerender } = renderComponent(ui);
            const dropzone = container.querySelector('#dropzone');
            dispatchEvt(dropzone, "drop", data);
            await flushPromises(rerender, ui);
            expect(screen.getByText("1 files selected.")).toBeInTheDocument();
        });

        it("Drop Multiple Files Successfully", async () => {
            const data = createMockData(files);
            const ui = generateUI();
            const { container, rerender } = renderComponent(ui);
            const dropzone = container.querySelector('#dropzone');
            dispatchEvt(dropzone, "drop", data);
            await flushPromises(rerender, ui);
            expect(screen.getByText("2 files selected.")).toBeInTheDocument();
        });

        it("Table Shows Drop Files", async () => {
            const data = createMockData(files);
            const ui = generateUI({ showDrops: true });
            const { container, rerender } = renderComponent(ui);
            const dropzone = container.querySelector('#dropzone');
            dispatchEvt(dropzone, "drop", data);
            await flushPromises(rerender, ui);
            expect(screen.getByTestId("file1.pdf-0")).toBeInTheDocument();
            expect(screen.getByTestId("dogs.gif-1")).toBeInTheDocument();
        });

        it("File Too Large", async () => {
            const data = createMockData(largeFile);
            const ui = generateUI();
            const { container, rerender } = renderComponent(ui);
            const dropzone = container.querySelector('#dropzone');
            dispatchEvt(dropzone, "drop", data);
            await flushPromises(rerender, ui);
            expect(screen.getByText("Upload is too large. Max upload size is 9.77 KB.")).toBeInTheDocument();
        });
    });

    describe("Testing Upload Response",  () => {
        describe("Testing Normal Upload", () => {
            it("Upload without metadata should have error", async () => {
                // set up component
                const data = createMockData(files);
                const ui = generateUI({ uploadMetadata: undefined });
                const { container, rerender } = renderComponent(ui);
                // drop files
                const dropzone = container.querySelector('#dropzone');
                dispatchEvt(dropzone, "drop", data);
                await flushPromises(rerender, ui);
                // upload
                const uploadBtn = screen.getByRole('button', { name: 'Upload' });
                fireEvent.click(uploadBtn);
                // assert
                const errorMsg = await screen.findByText('FileDropzone is missing metadata! Please provide metadata parameters...');
                expect(errorMsg).toBeInTheDocument();
            });

            it("Upload with metadata success", async () => {
                // set up component
                const data = createMockData(files);
                const ui = generateUI();
                const { container, rerender } = renderComponent(ui);
                // drop files
                const dropzone = container.querySelector('#dropzone');
                dispatchEvt(dropzone, "drop", data);
                await flushPromises(rerender, ui);
                // upload
                const uploadBtn = screen.getByRole('button', { name: 'Upload' });
                fireEvent.click(uploadBtn);
                // assert
                const successMsg = await screen.findByText('Upload Success');
                expect(successMsg).toBeInTheDocument();
            });
        });
    });
});
