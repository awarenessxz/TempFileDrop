import { combineReducers } from 'redux';
import webSocketReducer from "./websocket/websocket-reducer";

export const rootReducer = combineReducers({
    websocket: webSocketReducer
});

export type RootState = ReturnType<typeof rootReducer>;
