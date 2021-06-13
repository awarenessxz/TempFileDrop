import { createStore, compose, applyMiddleware, Store } from 'redux';
import reduxImmutableStateInvariant from 'redux-immutable-state-invariant';
import { composeWithDevTools } from 'redux-devtools-extension';
import thunkMiddleware from 'redux-thunk';
import { RootState, rootReducer } from './root-reducer';
import { RootActions } from './root-actions.types';

const configureProdStore = (initialState?: RootState): Store<RootState, RootActions> => {
    const middlewares = [thunkMiddleware];
    return createStore(rootReducer, initialState, compose(applyMiddleware(...middlewares)));
};

const configureDevStore = (initialState?: RootState): Store<RootState, RootActions> => {
    const middlewares = [reduxImmutableStateInvariant(), thunkMiddleware];
    const composeEnhancers = composeWithDevTools({}); // typescript shortcut for window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose
    const store = createStore(rootReducer, initialState, composeEnhancers(applyMiddleware(...middlewares)));

    if (module.hot) {
        module.hot.accept('./root-reducer', () => {
            store.replaceReducer(rootReducer);
        });
    }

    return store;
};

const configureStore = process.env.NODE_ENV === 'production' ? configureProdStore : configureDevStore;
export default configureStore;
