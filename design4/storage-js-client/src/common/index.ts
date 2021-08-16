export interface CommonMap {
    [key: string]: string | boolean | number | string[];
}

export type RequestParams = CommonMap;

export interface FileMap {
    [key: string]: File;
}

export interface StringArrayMap {
    [key: string]: string[];
}

export interface StringMap {
    [key: string]: string;
}