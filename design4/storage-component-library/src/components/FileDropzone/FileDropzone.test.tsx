import * as React from 'react';
import { act, cleanup, fireEvent, render, screen, waitFor, RenderResult } from '@testing-library/react';
import { FileDropzoneProps } from './FileDropzone.types';
import { setupServer } from "msw/node";
import { rest } from "msw";

// Import Component
import FileDropzone from './FileDropzone';

/*********************************************************************
 * Configuration / Helper Functions
 ********************************************************************/

// set up mock server for rest api end point
const server = setupServer(
    rest.post('/mock/file-upload', (req, res, ctx) => {
        return res(ctx.text("Success"));
    }),
    rest.post('/mock/file-upload/error', (req, res, ctx) => {
        return res.networkError("Network Error");
    }),
);
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// generate ui
type PartialFileDropzoneProps = Partial<FileDropzoneProps>;
const generateUI = ({ ...props }: PartialFileDropzoneProps = {}): React.ReactElement => {
    const defaultProps: FileDropzoneProps = {
        uploadUrl: "/mock/file-upload",
        maxSizeInBytes: 10000
    };
    const merged = { ...defaultProps, ...props };
    return <FileDropzone {...merged} />;
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
        it("Upload Success", async () => {
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
            const successMsg = await screen.findByText('Files have been uploaded!');
            expect(successMsg).toBeInTheDocument();
        });

        it("Upload Failure", async () => {
            // set up component
            const data = createMockData(files);
            const ui = generateUI({ uploadUrl: '/mock/file-upload/error' });
            const { container, rerender } = renderComponent(ui);
            // drop files
            const dropzone = container.querySelector('#dropzone');
            dispatchEvt(dropzone, "drop", data);
            await flushPromises(rerender, ui);
            // upload
            const uploadBtn = screen.getByRole('button', { name: 'Upload' });
            fireEvent.click(uploadBtn);
            // assert
            const errorMsg = await screen.findByText('Upload Failed!');
            expect(errorMsg).toBeInTheDocument();
        });
    });
});
