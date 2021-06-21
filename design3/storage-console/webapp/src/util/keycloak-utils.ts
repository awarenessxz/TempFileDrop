import { KeycloakTokenParsed } from "keycloak-js";

/* ***************************************************************************************
 * Type Definition
 *************************************************************************************** */

interface StorageClientAttributes {
    buckets: string[];
    routingkeys: string[];
}

// additional types which are not available in the original type definition provided
export interface CustomKeycloakTokenParsed extends KeycloakTokenParsed {
    preferred_username: string;
    storage_client_attr: StorageClientAttributes;
    name: string;
    roles: string[];
}

export interface UserToken {
    username: string;
    name: string;
    roles: string[];
    buckets: string[];
    routingKeys: string[];
    isAdmin: boolean;
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
            buckets: ntoken.storage_client_attr.buckets,
            routingKeys: ntoken.storage_client_attr.routingkeys
        };
    }
    return null;
};
