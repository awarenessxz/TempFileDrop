export interface UserTokenResponse {
    name: string;
    username: string;
    roles: string[];
    token: string;
    buckets: string[];
    routingKeys: string[];
}

export interface UserToken extends UserTokenResponse{
    isAdmin: boolean;
}