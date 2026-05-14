# Backend — Spring Boot / Java

## Stack
- Java 17
- Spring Boot 3.4
- Spring Data JPA + Hibernate
- PostgreSQL
- Gradle
- Bean Validation (jakarta.validation)

## Architecture Layers
Controller (infrastructure) → Service (Domain) → Repository (infrastructure) → Entity
Never skip layers. Controllers never touch repositories directly.
Domain layer uses spi for Repository access without having dependency to repository in `infrastructure/repository`.
Repository implementation implements spi in domain layer.
Domain layer uses api for sharing service api with Controller in `infrastructure/controller`.
Domain service is annotated with @DomainService

## Conventions
- DTOs for all request/response — never expose entities directly
- Services are @Transactional where needed
- Exceptions: throw specific custom exceptions (extend RuntimeException)
- All custom exceptions go in `exception/` package
- Global exception handling via @ControllerAdvice in `infrastructure/controller/exception/GlobalExceptionHandler.java`
- Use constructor injection, never @Autowired field injection

## Naming
- Controllers: `ReadPaymentController`, `WritePaymentController`, `ReadPaymentController`, `PaymentController`, `BoatController`
- Services: `ReadReservationServiceImpl`, `ReservationServiceImpl`, `ReadPaymentServiceImpl`, `WritePaymetnServiceImpl`
- Repositories: `BookingRepository`, `BoatRepository`
- DTOs: `CreateReservationDTO`, `UpdateReservationDTO`
- Entities: `Reservation`, `Boat`, `User`, `Payment`, `Debt`

## Validatio
- Always add Bean Validation annotations on request DTOs
- Return validation errors as structured JSON via GlobalExceptionHandler

## Testing
- Unit tests with JUnit 5 + Mockito for repository
- Nested Test Class per public Method in Domain Service
- Test naming: `shouldActionSuccessfully` for happy path and `shouldThrowExceptionWhenAction` for negative case
- Always test: happy path, not found, validation failure

## Database
- JPA entities use snake_case column names explicitly via @Column(name=)

## What NOT to do
- No business logic in controllers
- No direct SQL unless in a @Query on a repository
- Never expose entity IDs without checking authorization