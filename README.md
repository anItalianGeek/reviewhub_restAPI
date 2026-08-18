# ReviewHub API

Spring Boot backend for a school tutoring-desk system. Teachers open *sportelli* —
tutoring sessions on a given subject, in a given room, across one or more dated time
slots. Students browse what's available and book individual slots. Admins manage
everyone and everything.

Built during my final year of high school, and deployed for real: it ran on a Raspberry
Pi inside the school network, on its own DNS name, behind Apache with TLS, as a systemd
service that had to survive a reboot without me.

**Frontend**: [anItalianGeek/reviewhub](https://github.com/anItalianGeek/reviewhub)

---

## Stack

- **Spring Boot 3.3** · Java 17 · Gradle
- **Spring Security** with JWT authentication (`jjwt` 0.12.3)
- **Spring Data JPA** on MariaDB
- Embedded Tomcat

---

## Domain model

```
Person ──< Sportello >── Room
   │           │
   │           └──── Subject
   │           │
   │           └──< Slot
   │                 │
   └────< Booking >──┘
```

- **Person** — a student, a teacher, or an admin. Roles: `STUDENT`, `TEACHER`, `ADMIN`.
- **Sportello** — a tutoring session, tied to a room, a subject and a responsible teacher.
- **Slot** — a single dated time window within a sportello, with limited capacity.
- **Booking** — one student against one slot.
- **Room** and **Subject** — reference data, admin-managed.

---

## Authentication

Credentials go to `POST /users/login`, which returns a signed token and the caller's
role. Every protected endpoint expects `Authorization: Bearer {token}`.

Tokens are **HMAC-SHA signed and expire after 20 minutes**. Rather than issuing
long-lived tokens, `PUT /users/refresh` exchanges a still-valid token for a fresh one, so
a leaked token has a short useful life. Only login, email availability and the health
endpoints are public.

## Authorization

Roles and ownership do different jobs here:

- **Role decides what you can create.** `TEACHER` or `ADMIN` can open a sportello; `ADMIN` alone can manage users, rooms and subjects.
- **Ownership decides what you can change.** Editing or deleting a sportello requires being the teacher responsible for *that* sportello. Holding the `TEACHER` role isn't enough: a teacher can't touch another teacher's sessions.
- **Admins bypass ownership**, by design.

Being a teacher is an organizational fact and a permission to create, not blanket
authority over every session in the system.

## Booking rules

Booking isn't a plain insert. `POST /sportello/subscribe/{id}` rejects the request when
the slot is full, or when it overlaps something the student is already booked into, so
capacity and schedule conflicts are enforced server-side rather than trusted from the
client.

---

## API

Base path: `/api/v1`

| Group | Endpoints |
|---|---|
| **Users** | login · refresh · logout · create · modify · remove · list · detail · email availability |
| **Sportelli** | list all · available · subscribed · by teacher · detail · create · modify · remove · subscribe · unsubscribe |
| **Rooms** | list · create · update · delete |
| **Subjects** | list · create · update · delete |

List endpoints are paginated with `offset` and `limit`.

---

## Running locally

```bash
./gradlew bootRun
```

Requires a reachable MariaDB instance; credentials go in `application.properties`.
The app listens on `http://localhost:8888`.
