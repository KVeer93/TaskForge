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

# Backend Concepts Learned from the Task Worker Project

## 1. CommandLineRunner

### What is it?

`CommandLineRunner` is a Spring Boot interface that allows code to run automatically when the application starts.

```java
public class WorkerService implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("Application Started");
    }
}
```

### Why is it used?

Normally Spring Boot starts the application and waits for incoming requests.

However, some applications need to start performing work immediately after startup. Examples include:

* Background workers
* Scheduled jobs
* Data migration scripts
* Queue consumers

`CommandLineRunner` provides a hook to execute code as soon as Spring finishes initializing.

### In Our Project

When the Worker application starts:

```text
Spring Boot Starts
        ↓
Creates Beans
        ↓
Calls run()
        ↓
Worker Begins Polling
```

The worker continuously checks for new tasks without waiting for any user request.

---

## 2. RestTemplate

### What is it?

`RestTemplate` is Spring's HTTP client used for communicating with other services.

It allows Java applications to make:

* GET requests
* POST requests
* PUT requests
* DELETE requests

### Example

```java
TaskResponse task =
    restTemplate.getForObject(
        "http://localhost:8080/tasks",
        TaskResponse.class
    );
```

Equivalent HTTP request:

```http
GET /tasks
```

### Why is it used?

Modern applications are often split into multiple services.

For example:

```text
Worker Service
      ↓
Task API
      ↓
Database
```

The worker needs a way to communicate with the API.

`RestTemplate` provides that communication mechanism.

### In Our Project

The worker:

1. Fetches tasks using GET.
2. Marks tasks completed using PUT.

---

## 3. Polling

### What is Polling?

Polling means repeatedly asking a system for updates at regular intervals.

Example:

```text
Do you have work?
Do you have work?
Do you have work?
Do you have work?
```

### Example

```java
while(true){
    checkForTask();
    Thread.sleep(5000);
}
```

### Advantages

* Easy to implement
* Simple architecture
* No external tools required

### Disadvantages

* Unnecessary requests when no work exists
* Increased server load
* Higher latency compared to real-time systems

### In Our Project

Every 5 seconds the worker asks:

```http
GET /tasks
```

to check if new work is available.

---

## 4. Background Workers

### What is a Worker?

A worker is a process responsible for executing long-running tasks outside the main application flow.

### Why Workers Exist

Imagine a user uploads a large video.

Processing may take:

```text
30 seconds
1 minute
5 minutes
```

The user should not wait for that processing.

Instead:

```text
User Request
      ↓
Task Created
      ↓
Worker Processes Task
      ↓
Result Generated
```

### Examples

* Sending emails
* Generating PDFs
* Processing videos
* AI inference
* Data exports

### In Our Project

The worker fetches tasks and processes them independently from the API.

---

## 5. REST APIs

### What is REST?

REST (Representational State Transfer) is an architectural style used for communication between systems.

### Common HTTP Methods

| Method | Purpose     |
| ------ | ----------- |
| GET    | Read data   |
| POST   | Create data |
| PUT    | Update data |
| DELETE | Remove data |

### Examples

Fetch task:

```http
GET /tasks
```

Create task:

```http
POST /tasks
```

Complete task:

```http
PUT /tasks/1/complete
```

### In Our Project

The Worker communicates with the API entirely through REST endpoints.

---

## 6. Long-Running Processes

### What are Long-Running Processes?

Operations that take noticeable time to complete.

Examples:

* Sending thousands of emails
* Video processing
* AI model inference
* Report generation

### Problem

Users should not wait for these operations.

Bad:

```text
User Clicks Button
      ↓
Wait 2 Minutes
      ↓
Response Received
```

Good:

```text
User Clicks Button
      ↓
Task Created
      ↓
Immediate Response
      ↓
Worker Handles Task
```

### In Our Project

This line simulates long-running work:

```java
Thread.sleep(5000);
```

It represents actual processing that could take several seconds.

---

## 7. Task Queues

### What is a Task Queue?

A task queue stores work that needs to be processed later.

### Example

```text
Task 1 → Send Email
Task 2 → Generate Report
Task 3 → Resize Image
```

Workers consume tasks from the queue.

### Why Use Queues?

They provide:

* Scalability
* Reliability
* Separation of concerns

### Architecture

```text
Client
  ↓
API
  ↓
Queue
  ↓
Worker
```

### In Our Project

The database currently behaves like a simple queue.

Pending tasks are picked and processed by the worker.

In production, systems often use:

* RabbitMQ
* Apache Kafka
* Amazon SQS

---

## 8. Reliability

### What is Reliability?

Reliability means the system continues working correctly even when failures occur.

### Failures That Can Happen

* Worker crashes
* Network failures
* Database outages
* API downtime

### Goal

The task should eventually complete even if temporary failures occur.

### Reliable System

```text
Task Created
      ↓
Failure
      ↓
Retry
      ↓
Success
```

### In Our Project

If the worker cannot fetch a task, it retries after 5 seconds.

This simple retry mechanism improves reliability.

---

## 9. Crashes and Retries

### What Happens When Systems Crash?

Suppose the worker starts processing:

```text
Processing Task 1
```

Then:

```text
Worker Crashes
```

before marking the task completed.

### Problem

Did the task finish?

Maybe.

Was it marked complete?

No.

The system cannot know.

### Retry Strategy

When the worker restarts:

```text
Task Still Pending
      ↓
Process Again
```

This ensures work is not permanently lost.

### In Production

Systems often track:

* Retry count
* Failure reason
* Last attempt timestamp

to manage retries safely.

---

## 10. Idempotency

### What is Idempotency?

An operation is idempotent if executing it multiple times produces the same final result.

### Example

Setting status:

```java
task.setStatus("COMPLETED");
```

Running once:

```text
COMPLETED
```

Running 100 times:

```text
COMPLETED
```

Same result.

### Non-Idempotent Example

Sending email:

```java
sendEmail();
```

Running twice:

```text
Email Sent Twice
```

This causes duplicate work.

### Why It Matters

Imagine:

```text
Worker Processes Task
      ↓
Worker Crashes
      ↓
Task Retries
```

Without idempotency:

* Duplicate emails
* Duplicate payments
* Duplicate notifications

can occur.

### Idempotent Approach

Before performing work:

```java
if(emailAlreadySent()){
    return;
}
```

This guarantees the task is safe to retry.

### Importance

Idempotency is one of the most important concepts in distributed systems because retries are inevitable.

---

# Complete Flow of Our System

```text
Application Starts
        ↓
CommandLineRunner Executes
        ↓
Worker Polls API Using RestTemplate
        ↓
GET /tasks
        ↓
Task Retrieved
        ↓
Background Processing Begins
        ↓
Long Running Work Executes
        ↓
PUT /tasks/{id}/complete
        ↓
Task Marked Completed
        ↓
Worker Sleeps
        ↓
Polling Continues
```

# Key Takeaway

This small project introduces many real-world backend engineering concepts:

1. CommandLineRunner
2. RestTemplate
3. Polling
4. Background Workers
5. REST APIs
6. Long-Running Processes
7. Task Queues
8. Reliability
9. Crashes and Retries
10. Idempotency

These concepts form the foundation of large-scale distributed systems used by companies such as Netflix, Uber, Amazon, and many modern cloud-native applications.

# TaskForge Learning Notes - Day 4

# Worker Architecture

Until now TaskForge only had an API service.

The API was responsible for:

- accepting HTTP requests
- creating tasks
- storing tasks in PostgreSQL
- exposing endpoints to interact with tasks


Architecture:

```
Client
   |
   |
   v

TaskForge API

   |
   |
   v

PostgreSQL Database
```

The problem with this architecture:

If a task takes a long time (image processing, video processing, report generation), the API server should not perform the heavy work itself.

The API should stay fast and only accept requests.

Heavy processing should happen separately.

This is where workers come in.


Updated architecture:

```
Client

  |

TaskForge API

  |

Database


  ^

  |

Worker Service
```


The API manages tasks.

The worker executes tasks.

Responsibilities are separated.


---

# Worker Service

A worker is a separate application/process whose job is to execute background tasks.

Worker flow:

```
while(true){

    Ask for task

    If task exists:

        Process task

        Mark completed

    Wait

}
```

The worker does not create tasks.

The worker does not directly modify the database.

The worker communicates with the API.

Example:

```
Worker

GET /tasks/next

        |

        v

API


returns task


Worker processes


PUT /tasks/{id}/complete
```

The API remains the owner of task state.


---

# Why Worker Should Not Directly Access Database

Possible design:

```
Worker

   |

Database
```


Problem:

Business logic gets duplicated.

Example:

API says:

```
COMPLETED
```

Worker says:

```
DONE
```

Different services start modifying state differently.


Better:

```
Worker

   |

API

   |

Database
```


The API becomes the single source of truth.

All rules about:

- status changes
- timestamps
- retries
- validation

stay in one place.


---

# Ports and Servers

A port is required when an application needs to receive incoming communication.

A port means:

"I am listening here. Other applications can contact me."


Example:

Computer:

```
localhost
```

Different applications:

```
Spring API       -> 8080

PostgreSQL       -> 5432

Frontend         -> 3000

Redis            -> 6379
```


The port tells the operating system which application should receive the request.


---

# Why API Needs A Port

The API receives requests.

Example:

```
Frontend

    |

POST /tasks

    |

API : 8080
```


The API must listen.

Therefore:

API needs:

- Tomcat
- HTTP server
- port


Flow:

```
HTTP Request

      |

      v

Tomcat

      |

      v

DispatcherServlet

      |

      v

Controller

      |

      v

Service

      |

      v

Database
```


Tomcat acts as the HTTP entry point.

It receives network requests and passes them into Spring.


---

# Why Worker Usually Does Not Need A Port

The worker is not receiving requests.

The worker itself starts communication.

Example:

```
Worker

   |

   |  GET /tasks/next

   |

   v

API : 8080
```


Nobody calls:

```
localhost:8081/worker
```


Therefore worker does not need a web server.

A worker can simply be:

```
Spring Boot starts

        |

CommandLineRunner starts

        |

Infinite processing loop
```


A Spring Boot application does not always mean a web application.


---

# Pull Based Worker Model

In pull architecture:

Workers ask for tasks.


Example:

```
Worker 1 ---> Any work?

Worker 2 ---> Any work?


            API


Worker 3 ---> Any work?
```


Advantages:

- Easy scaling
- Workers can join anytime
- Workers can disappear
- API does not track worker addresses


Adding a worker:

Just start another worker process.


---

# Push Based Worker Model

In push architecture:

The API sends work to workers.


Example:

```
API

 |
 |
 v

Worker
```


Now workers must expose endpoints.

Example:

```
POST worker-1:9001/process
```


The API needs to know:

- worker address
- worker health
- worker availability


Workers need:

- ports
- registration
- health endpoints


---

# Worker Registration

In push systems, workers usually register themselves.


Example:

Worker starts:

```
POST /workers/register


{
    workerId:"worker-1",
    url:"localhost:9001"
}
```


API stores:

```
worker_id     status

worker-1      ACTIVE
```


Now API knows where workers exist.


---

# Heartbeats

A heartbeat is a signal that says:

"I am alive."


Example:

Every few seconds:

Worker sends:

```
POST /workers/heartbeat
```


API stores:

```
worker_id     last_seen

worker-1      12:00:10
```


If:

```
current_time - last_seen > limit
```

worker is considered dead.


Important:

Even in pull systems, heartbeat does not require worker ports.

The worker sends heartbeat to API.


---

# Worker Failure Problem

Problem:

Worker gets a task:

```
Task #10

status = PROCESSING
```


Worker starts processing.


Then:

```
Worker crashes
```


Database:

```
Task #10

PROCESSING
```


forever.


The system does not know what happened.


---

# Solution 1 - Timeout

Store:

```
started_at
```


Example:

```
Task started:

10:00


Current time:

10:30
```


If timeout exceeded:

Change:

```
PROCESSING

     |

     v

QUEUED
```


Another worker can retry.


This idea is called visibility timeout.


---

# Solution 2 - Acknowledgement (ACK)

Worker does not instantly complete a task.

Flow:

```
Worker gets task


        |

Process


        |

Send completion ACK


        |

API marks COMPLETED
```


Until ACK arrives:

System does not fully trust that task finished.


---

# The Dangerous Retry Problem

Scenario:

Worker:

```
Receives task

      |

Does work successfully

      |

Crashes before sending COMPLETED
```


Example:

```
Money transferred

but

completion request failed
```


System thinks:

Task failed.


It retries.


Danger:

Money may transfer twice.


---

# Idempotency

Definition:

Idempotency means performing the same operation multiple times results in the same final state as performing it once.


Example:

Not idempotent:

```
balance = balance + 100
```


Run once:

```
100 -> 200
```


Run twice:

```
200 -> 300
```


Bad.


Idempotent:

```
Process transaction TXN-123
```


First time:

```
TXN-123 completed
```


Second time:

System checks:

```
TXN-123 already completed
```

No duplicate action.


---

# Idempotency Key

An idempotency key uniquely identifies an operation.

Example:

Payment:

```
idempotency_key = PAY-12345
```


Request:

```
{
 amount:1000,
 key:"PAY-12345"
}
```


Every retry uses the same key.


---

# Where Idempotency Key Exists

Frontend:

Temporary storage.

Purpose:

Multiple button clicks or retries send same key.


API:

Stores request identity.


Example:

```
payment_requests


key          status

PAY123       PROCESSING
```


Worker:

Reads the key.

Usually does not store it.


Result system:

Stores proof.


Example:

```
completed_operations


key          result

PAY123       SUCCESS
```


---

# Worker Idempotency Flow

Worker receives:

```
Task

idempotency_key = ABC123
```


Worker checks:

```
Is ABC123 already completed?
```


If yes:

Return success.


If no:

Execute task.


Then save:

```
ABC123 completed
```


---

# Why Checking Task Existence Is Not Enough

Wrong:

```
Does task ABC exist?

Yes.

Skip.
```


Because task may exist but not be completed.


Correct:

Check:

```
Does RESULT for ABC exist?
```


Task existence means:

"We know about this work."


Result existence means:

"The work actually happened."


---

# Database Unique Constraints And Idempotency

Example:

```
task_results


idempotency_key UNIQUE
```


Worker 1:

```
INSERT ABC123

success
```


Worker 2:

```
INSERT ABC123

duplicate rejected
```


Database prevents double completion.


---

# Progress vs Idempotency

Progress answers:

"How much work is finished?"


Example:

```
Video encoding:

70%
```


Idempotency answers:

"Can I safely repeat this work?"


They solve different problems.


---

# Important Lessons

1. Workers execute tasks, APIs manage state.

2. Applications need ports only when they accept incoming connections.

3. Pull systems scale easily because workers request work.

4. Failed workers are handled using timeout, heartbeat, and retries.

5. Retrying without idempotency can create duplicate effects.

6. Idempotency requires designing every operation with a unique identity.

7. In distributed systems, knowing whether something happened is sometimes harder than doing the task itself.

# TaskForge Day 5 - Concurrency & Worker Coordination

## Goal

Make multiple workers process tasks safely without two workers executing the same task.

---

## Problem: Race Condition

When multiple workers request tasks at the same time:

Worker 1:
- Finds Task 10 as QUEUED

Worker 2:
- Also finds Task 10 as QUEUED

Both workers can start processing the same task.

This happens because finding a task and updating its status are separate operations.

---

## Solution: Transactions + Database Locking

Implemented pessimistic locking.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This locks the selected database row while a worker claims it.

Flow :

```aiignore
BEGIN TRANSACTION

Find QUEUED task

Lock task row

Update:
QUEUED -> PROCESSING

Assign worker

COMMIT

Release lock
```

Now only one worker can claim a task.

Transaction Learning

@Transactional does not mean only one request can enter the function.

It means multiple database operations are treated as one unit.

Transaction provides:

consistency
rollback support
controlled database changes

Locking controls access.

Transaction controls how long the lock exists.

Both are required together.

Worker Ownership

Added:

assignedWorker

to Task entity.

Purpose:

track which worker owns a task
debugging
monitoring
future worker failure recovery

Example:

Task 5
status = PROCESSING
assignedWorker = worker-2
Hibernate Schema Update

Adding a new field in Entity:

private String assignedWorker;

automatically created a new database column because:

spring.jpa.hibernate.ddl-auto=update

Hibernate compares Java entities with database tables and updates the schema.