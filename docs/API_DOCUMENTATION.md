# FoodBridge API Notes

Base URL: `http://localhost:8080/api`

Authentication: login returns a JWT. Send it on protected requests as:

```http
Authorization: Bearer <token>
```

## Authentication

### POST `/auth/register`

```json
{
  "name": "Example Donor",
  "email": "donor@example.com",
  "password": "change-me-123",
  "phone": "9000000000",
  "role": "DONOR",
  "organizationName": "Example Kitchen",
  "address": "Sector 1",
  "city": "Noida",
  "state": "Uttar Pradesh",
  "pincode": "201301"
}
```

Valid public roles: `DONOR`, `NGO`, `VOLUNTEER`. Public registration cannot create an admin.

### POST `/auth/login`

```json
{"email":"donor@example.com","password":"change-me-123"}
```

## Donations

- `GET /donations` — available listings (authenticated users)
- `GET /donations/{id}` — donation details
- `GET /donations/mine` — donor's listings
- `POST /donations` — donor creates listing
- `PUT /donations/{id}` — donor edits own available listing
- `DELETE /donations/{id}` — donor cancels own available listing

Example create body:

```json
{
  "foodName": "Vegetable pulao",
  "description": "Freshly prepared and packed",
  "category": "Prepared meals",
  "quantity": 20,
  "unit": "packets",
  "foodType": "VEGETARIAN",
  "preparationTime": "2026-11-18T09:00:00",
  "useByTime": "2026-11-18T20:00:00",
  "pickupDeadline": "2026-11-18T16:00:00",
  "storageConditions": "Keep covered",
  "allergenInformation": "Contains wheat",
  "packagingInformation": "Sealed food-grade containers",
  "imageUrl": null,
  "address": "Sector 62",
  "city": "Noida",
  "state": "Uttar Pradesh",
  "pincode": "201309",
  "latitude": null,
  "longitude": null,
  "safetyConfirmed": true
}
```

## Requests

- `POST /requests` — verified NGO submits a request (`donationId`, `requestedQuantity`, `message`)
- `GET /requests/mine` — NGO's own requests, or donor's incoming requests
- `PUT /requests/{id}/accept` — donor accepts an incoming pending request
- `PUT /requests/{id}/reject` — donor rejects an incoming pending request
- `PUT /requests/{id}/cancel` — NGO cancels its own pending request

## Deliveries

- `GET /deliveries/available` — volunteer sees unassigned accepted requests
- `GET /deliveries/mine` — current user's related deliveries
- `PUT /deliveries/{id}/claim` — volunteer claims task
- `PUT /deliveries/{id}/pickup` — assigned volunteer confirms pickup
- `PUT /deliveries/{id}/complete` — assigned volunteer completes with body `{ "verificationCode": "123456" }`

## Notifications

- `GET /notifications`
- `PUT /notifications/{id}/read`
- `PUT /notifications/read-all`

## Admin (ADMIN role only)

- `GET /admin/overview`
- `GET /admin/users`
- `GET /admin/ngos/pending`
- `PUT /admin/ngos/{id}/approve`
- `PUT /admin/ngos/{id}/reject`
- `PUT /admin/users/{id}/status?active=true|false`
- `GET /admin/donations`

## Image upload

`POST /uploads/image` with multipart form field `file`, JPG/PNG/WebP, max 5 MB. Returns `{ "url": "/uploads/<generated-name>" }`.
