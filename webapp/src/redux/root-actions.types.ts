import { Action } from 'redux';
import { ThunkAction } from 'redux-thunk';
import { RootState } from './root-reducer';
import { WebSocketActions } from "./websocket/websocket-actions.types";

export type RootActions = WebSocketActions;
export type RootThunkResult<R> = ThunkAction<R, RootState, undefined, Action>;
