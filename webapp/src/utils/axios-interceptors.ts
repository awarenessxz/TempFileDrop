import axios, {AxiosRequestConfig, AxiosResponse} from "axios";

const requestHandler = (request: AxiosRequestConfig): AxiosRequestConfig | Promise<AxiosRequestConfig> => {
    if (window.accessToken) {
        const token = window.accessToken ? window.accessToken : "dummy_token";
        request.headers['Authorization'] = 'Bearer ' + token;
    }
    return request;
};

const responseHandler = (response: AxiosResponse): AxiosResponse | Promise<AxiosResponse> => {
    return response;
};

const errorHandler = (error: any) => {
    console.log("Axios Fetch Error : ", error);
    return Promise.reject(error);
};

axios.interceptors.request.use(requestHandler, errorHandler);
axios.interceptors.response.use(responseHandler, errorHandler);