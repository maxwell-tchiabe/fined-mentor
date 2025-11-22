Fined Mentor — Auth Postman 

Overview
- This folder contains a Postman collection (`Auth.postman_collection.json`) with example requests for the backend auth API implemented in `AuthController`.

Endpoints included
- `POST /api/auth/register` — register a new user
- `POST /api/auth/login` — authenticate and receive JWT
- `POST /api/auth/activate` — activate account using OTP token
- `POST /api/auth/resend-activation?email=...` — resend activation OTP

How to use
1. Start the backend locally (default: `http://localhost:8080`).
2. Import `Auth.postman_collection.json` in Postman (File → Import).
3. Set an environment variable `baseUrl` to your backend URL (default in collection: `http://localhost:8080`).
4. Use the `Register` request to create a new user. Example body is pre-filled for `testuser`.
5. If your app sends real emails, get the activation token from the email and set the environment variable `activationToken`, then run `Activate (use token)`.
   - If email sending isn't configured in your local setup, look for the OTP/activation token in backend logs or in your database table for activation tokens.
6. Alternatively, use `Resend Activation` to trigger sending a new OTP to the user email.
7. After activation, run `Login`. The collection has a test script that will save the JWT token to the environment variable `jwtToken` (if returned).
8. You can then use `{{jwtToken}}` in Authorization header for other API requests (the collection does not include downstream protected endpoints).

Quick curl examples
- Register
```
curl -X POST "http://localhost:8080/api/auth/register" -H "Content-Type: application/json" -d \
  "{\"username\":\"testuser\",\"email\":\"testuser@example.com\",\"password\":\"password123\",\"firstName\":\"Test\",\"lastName\":\"User\"}"
```

- Login (returns a JSON with `data` containing the JWT token)
```
curl -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d \
  "{\"usernameOrEmail\":\"testuser\",\"password\":\"password123\"}"
```

- Activate (replace `<TOKEN>` with real OTP)
```
curl -X POST "http://localhost:8080/api/auth/activate" -H "Content-Type: application/json" -d \
  "{\"token\":\"<TOKEN>\"}"
```

- Resend activation
```
curl -X POST "\"http://localhost:8080/api/auth/resend-activation?email=testuser@example.com\""
```

Notes / Troubleshooting
- If `register` returns 200 but you never receive an email, check application properties for email settings. For local testing, check application logs for printed activation tokens or the database table that stores activation tokens.
- The login response wraps JWT inside the `ApiResponse` object under the `data` field. Example response shape:
```
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "<JWT_TOKEN>",
    "type": "Bearer",
    "id": "<userId>",
    "username": "testuser",
    "email": "testuser@example.com",
    "roles": ["ROLE_USER"]
  }
}
```