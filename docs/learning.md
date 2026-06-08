## Maven dependency issue

Problem:
Spring imports were red.

Cause:
IDE Maven sync issue.

Learning:
Maven manages dependencies, imports only reference downloaded classes.

DispatcherServlet is Spring MVC's central router that receives incoming web requests from embedded tomcat server and 
dispatches them to the correct controller method.

## Dependency Injection (DI)

Dependency Injection is a design pattern where an object does not create the objects it depends on. Instead, those dependencies are created and provided (injected) by an external system.

In Spring Boot, this external system is the Spring Container.

Without Dependency Injection:

TaskController creates TaskService:

TaskController → new TaskService()

This creates tight coupling because TaskController is responsible for knowing how TaskService is created.

With Dependency Injection:

TaskController only declares that it needs a TaskService.

Spring:
1. Creates the TaskService object (bean)
2. Stores it inside the Spring Container
3. Injects it wherever it is required

Flow:

Spring Container

TaskService Bean
|
v
TaskController


Benefits:

- Loose coupling: Classes depend on behavior, not object creation details.
- Easier testing: Real dependencies can be replaced with mock/fake ones.
- Lifecycle management: Spring manages object creation, reuse, and destruction.

Example:

Instead of:

TaskService service = new TaskService();

We write:

private final TaskService taskService;

public TaskController(TaskService taskService) {
this.taskService = taskService;
}

The controller does not create TaskService.
It receives it from Spring.

This inversion of object creation control is called Inversion of Control (IoC), and Dependency Injection is the technique Spring uses to achieve it.

Dependency Injection separates object usage from object creation.
Classes only declare what they need, while Spring manages how those dependencies are created and connected.

## Enum

An enum (short for enumeration) is a special Java type used when a variable can have only a fixed set of predefined values.


# Why Production APIs Return DTOs Instead of Entities

## Entity

An Entity represents a database table and is used by JPA/Hibernate to interact with the database.

Example:

```java
@Entity
public class User {

    @Id
    private Long id;

    private String name;
    private String password;
    private String email;
}
```

The primary purpose of an Entity is database persistence.

---

## Returning an Entity Directly

Controller:

```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

Spring Boot automatically converts the returned object into JSON:

```json
{
    "id": 1,
    "name": "KV",
    "password": "12345",
    "email": "kv@gmail.com"
}
```

Problem:

The password field is exposed to the client.

This creates a security risk because every field inside the Entity may be sent in the API response.

---

## DTO (Data Transfer Object)

A DTO is a separate class designed specifically for API requests and responses.

Example:

```java
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
}
```

Notice that the password field is intentionally omitted.

---

## Returning a DTO

Controller:

```java
@GetMapping("/{id}")
public UserResponseDTO getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

Response:

```json
{
    "id": 1,
    "name": "KV",
    "email": "kv@gmail.com"
}
```

The password remains hidden.

---

## Typical Flow in Production

Request JSON
↓
Request DTO
↓
Controller
↓
Service
↓
Entity
↓
Database

Database
↓
Entity
↓
Response DTO
↓
Response JSON

---

## Example with Task Application

Current Entity:

```java
@Entity
public class Task {

    private Long id;
    private String title;
    private TaskStatus status;
    private Integer priority;
    private String errorMessage;
}
```

Instead of returning:

```java
public Task getTask(...)
```

Production applications often return:

```java
public TaskResponseDTO getTask(...)
```

Example DTO:

```java
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String status;
}
```

Fields such as errorMessage, internal IDs, audit fields, or other sensitive data can be excluded from the API response.

---

## Why Use DTOs?

1. Security

    * Prevents exposing sensitive fields (passwords, tokens, internal data).

2. API Control

    * Decide exactly what the client receives.

3. Flexibility

    * Database structure can change without affecting API responses.

4. Clean Architecture

    * Database models and API models remain separate.

---

## Rule of Thumb

Learning Projects:
Entity → Response

Production Projects:
Entity → DTO → Response

Returning Entities directly is acceptable for learning and small projects.

Returning DTOs is the preferred approach in production systems.

## Control flow as on day 2
POST /tasks

JSON arrives  ->  dispatcherControl -> Controller -> DTO(through @requestBody)
-> Service -> repositry > ORM > hibernate -> Database row created

## How does a task move:

QUEUED

↓

PROCESSING

↓

COMPLETED