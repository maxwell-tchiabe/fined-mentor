export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    usernameOrEmail: string;
    password: string;
}

export interface ActivateRequest {
    token: string;
}

export interface JwtResponse {
    token: string;
    type: string;
    id: string;
    username: string;
    email: string;
    roles: string[];
}
