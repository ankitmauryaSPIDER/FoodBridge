# FoodBridge Architecture

## Runtime architecture

```text
Browser
  |
  | React + Vite (localhost:5173)
  | Axios requests with Bearer JWT
  v
Spring Boot REST API (localhost:8080)
  |-- Authentication / JWT filter / role authorization
  |-- Controllers (HTTP layer)
  |-- Services (business rules and workflow)
  |-- Spring Data JPA repositories
  v
MySQL (Docker volume)

Uploaded food images -> backend upload directory -> persistent Docker volume
```

## Main domain relationships

- One user with role DONOR can create many donations.
- One donation can receive requests from multiple NGOs, but the donor accepts at most one request while the donation is available.
- One accepted request creates one delivery record.
- A volunteer can claim an unassigned delivery.
- A delivery progresses through READY_FOR_PICKUP -> PICKED_UP -> COMPLETED.
- One user can receive many notifications.

## Donation lifecycle

`AVAILABLE -> RESERVED -> PICKED_UP -> DELIVERED`

Alternative terminal states: `CANCELLED`, `EXPIRED`.

## Request lifecycle

`PENDING -> ACCEPTED | REJECTED | CANCELLED`

On acceptance, the donation becomes RESERVED, remaining pending requests for that donation are rejected and a delivery record is created.

## Delivery lifecycle

`READY_FOR_PICKUP -> PICKED_UP -> COMPLETED`

The assigned volunteer must enter the generated six-digit handoff code to complete a delivery.

## Authorization model

- DONOR: create and manage own available donations; decide incoming requests for own donations.
- NGO: view available donations; verified NGOs can submit requests; can cancel own pending requests.
- VOLUNTEER: view unassigned delivery jobs, claim one and update only assigned jobs.
- ADMIN: review pending NGOs, manage user active status and inspect platform records.
