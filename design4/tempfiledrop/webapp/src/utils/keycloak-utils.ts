import { KeycloakRoles, KeycloakTokenParsed } from "keycloak-js";
import Data from "../config/app.json";

/* ***************************************************************************************
 * Type Definition
 *************************************************************************************** */

// additional types which are not available in the original type definition provided
export interface CustomKeycloakTokenParsed extends KeycloakTokenParsed {
    preferred_username: string;
    name: string;
    roles: string[];
}

export interface UserToken {
    username: string;
    name: string;
    roles: string[];
    isAdmin: boolean;
    token: string;
}

/* ***************************************************************************************
 * Functions
 *************************************************************************************** */

export const extractUserToken = (token: KeycloakTokenParsed | undefined): UserToken | null => {
    const ntoken = token as CustomKeycloakTokenParsed;
    if (ntoken) {
        return {
            username: ntoken.preferred_username,
            name: ntoken.name,
            roles: ntoken.roles,
            isAdmin: ntoken.roles?.includes("admin"),
            token: ""
        };
    }
    return null;
};

export const hasTempFileDropRoles = (keycloakRoles: KeycloakRoles | undefined): boolean => {
    if (keycloakRoles) {
        return Data.keycloak.realmRoles.some(role => keycloakRoles.roles.includes(role));
    }
    return false;
};
