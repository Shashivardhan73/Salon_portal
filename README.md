# Bloom Salon Portal

A sample salon portal built with Spring Boot, Thymeleaf, Spring Security, and an H2 database.

## Run locally

Install Java 17 or newer and Maven, then run this from the project folder:

```text
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Owner | `owner` | `owner123` |
| Client | `alex@example.com` | `client123` |

The owner can view all client profiles and feedback, create individual client accounts, and see upcoming appointments grouped by date. Clients can register from the login page using their name, email, phone number, and password. Their email is used as the login username, and the password is stored as a secure hash. Clients can view their own profile and feedback history, submit a rating and comment, and book salon appointments by service, date, and time.

The H2 database is stored in `data/salon-db.mv.db` and keeps records between restarts. Demo credentials are for local evaluation only; replace them and use a production database and deployment configuration before making the portal public.
