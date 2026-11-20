# Alumni LinkedIn Profile Searcher

Spring Boot 3 / Java 17+ / PostgreSQL REST API that searches LinkedIn profiles of alumni
(by university, designation and optional pass-out year) and stores the results in a database.

## Tech stack
Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, PostgreSQL, JUnit 5, Mockito.

## Design
Controller -> Service -> `ProfileScraper` (interface) + Repository

- `AlumniController`: REST endpoints and request validation.
- `AlumniService`: calls the scraper, saves only new profiles (de-duplicated by LinkedIn profile URL).
- `ProfileScraper`: strategy interface. Implementations:
    - `PhantomBusterScraper`: PhantomBuster v2 client (profile `default`).
    - `MockProfileScraper`: returns sample alumni (profile `mock`, active by default).
- `GlobalExceptionHandler`: consistent error responses.

## Setup
1. Install JDK 17+ and Maven (or use IntelliJ's bundled Maven).
2. Install PostgreSQL and create a database named `alumni`
   (default user `postgres`, password `postgres`; override with `DB_URL`, `DB_USER`, `DB_PASSWORD`).
3. Run `mvn spring-boot:run` or run `AlumniApplication` from the IDE. The table is created automatically.
4. Run the tests with `mvn test` (they use an in-memory H2 database and mocks, so no setup is needed).

## API

### POST /api/alumni/search
Request:
```json
{ "university": "IIT Bombay", "designation": "Software Engineer", "passoutYear": 2020 }
```
`passoutYear` is optional. Response (200):
```json
{
  "status": "success",
  "data": [
    { "name": "John Doe", "currentRole": "Software Engineer", "university": "IIT Bombay",
      "location": "New York, NY", "linkedinHeadline": "Software Engineer at XYZ Corp", "passoutYear": 2020 }
  ]
}
```

### GET /api/alumni/all
Returns all saved profiles in the same format.

### Errors
`{"status": "error", "message": "..."}` with status 400 (validation or malformed JSON),
502 (scraper failure) or 500 (unexpected error).

## PhantomBuster integration: status and limitations
- `PhantomBusterScraper` implements the PhantomBuster v2 flow: launch the LinkedIn Search Export
  Phantom, poll until it finishes, read the result object and map it to profiles.
- **It has not been tested against the live PhantomBuster API.** A real run needs a PhantomBuster
  workspace and a logged-in LinkedIn session cookie. The free tier is limited to 10 rows and a small
  amount of execution time, and scraping with a personal LinkedIn account can get it restricted,
  so it was not run.
- The app therefore runs with the `mock` profile by default. `MockProfileScraper` returns sample
  alumni, so the rest of the system (endpoints, validation, PostgreSQL storage, de-duplication,
  error handling, tests) works end to end.
- To use the real scraper, set `SPRING_PROFILES_ACTIVE=default` and these environment variables:
  `PHANTOMBUSTER_API_KEY`, `PHANTOMBUSTER_AGENT_ID`, `LINKEDIN_SESSION_COOKIE`, `LINKEDIN_USER_AGENT`.
- The PhantomBuster endpoints and result field names come from its documentation and may need
  small adjustments against a live account.

## Note on LinkedIn data
LinkedIn search results do not expose graduation year, so the requested `passoutYear` is added to the
search keywords and stored with the record rather than read from the profile.