# TaskForge - Problems Faced & Debugging Journey

This document contains problems faced while building TaskForge and the reasoning used to solve them.

---

# 1. Understanding Spring Boot Project Structure

## Problem

Initially Spring Boot structure felt confusing:

- Why src/main/java exists
- Why packages are needed
- Why package declaration exists inside files
- How Java finds classes

Example:

```java
package com.taskforge.api;
```

## Understanding

The folder structure follows the package namespace.

Package names create unique identities for classes.

Example:

```
com.taskforge.api.TaskService
```

This prevents class name conflicts in large projects.

The folder structure is mainly a convention that helps Java build tools and developers organize code.

---

# 2. Maven Dependency Problem

## Problem

Spring imports were showing errors:

Example:

```java
import org.springframework.boot.SpringApplication;
```

Error:

```
package org.springframework.boot does not exist
```

## Cause

Java itself does not contain Spring classes.

Spring Boot is an external dependency.

## Solution

Use Maven.

Maven reads:

```
pom.xml
```

downloads required dependencies and adds them to the project classpath.

Learning:

Import statements do not download libraries.

They only use classes already available.

---

# 3. Understanding Spring Boot Startup

## Problem

SpringApplication.run() felt like magic.

Question:

How does one line start everything?

## Understanding

When:

```java
SpringApplication.run(...)
```

executes:

Spring:

1. Creates Application Context
2. Scans classes
3. Finds annotations
4. Creates required objects
5. Starts embedded Tomcat
6. Prepares application for HTTP requests


---

# 4. Dependency Injection Confusion

## Problem

Why not create objects manually?

Example:

```java
TaskService service = new TaskService();
```

## Learning

Manual object creation tightly connects classes.

Spring manages objects in a container.

Instead of:

Controller creates Service

Spring creates both and connects them.

Flow:

```
Spring Container

    |

TaskController

    |

TaskService
```

Benefits:

- less coupling
- easier testing
- centralized lifecycle management

---

# 5. PostgreSQL Connection Failure

## Problem

Application failed because Spring tried creating a datasource but database configuration was missing.

Error:

```
Failed to determine suitable driver class
```

## Cause

JPA dependency existed.

Spring assumed:

"This application needs a database."

## Solution

Configured PostgreSQL properly:

Database URL

Username

Driver

Hibernate configuration

---

# 6. Repository Bean Not Found

## Problem

Error:

```
No qualifying bean of type TaskRepository found
```

## Cause

Spring was unable to create Repository bean.

Possible causes:

- wrong package location
- repository not extending JpaRepository
- JPA auto configuration disabled

## Learning

Spring creates repository implementations automatically.

But only if:

- package scanning works
- JPA configuration is enabled

---

# 7. Understanding DTO vs Entity

## Problem

Should incoming requests directly use database entities?

## Decision

No.

Created DTOs.

Example:

```
CreateTaskRequest
```

Reason:

External API structure and internal database structure should remain separate.

---

# 8. HTTP Method Problems

## Problem

Opening:

```
localhost:8080/tasks
```

returned:

```
405 Method Not Allowed
```

## Cause

Endpoint supported POST.

Browser sends GET.

## Solution

Created:

```java
@GetMapping
```

for fetching tasks.

Learning:

Same URL can behave differently depending on HTTP method.

---

# 9. Worker Architecture Confusion

## Problem

Should worker directly access database?

## Decision

No.

Worker talks to API.

Architecture:

```
Worker

  |

API

  |

Database
```

Reason:

API remains owner of business logic.

---

# 10. Multiple Workers Port Conflict

## Problem

Starting multiple workers caused:

```
Port already in use
```

## Cause

Each worker started embedded Tomcat.

## Learning

Workers do not actually need ports because nobody calls them.

Ports are needed only for applications accepting incoming connections.

Worker only makes outgoing API calls.

---

# 11. Worker Crash Problem

## Problem

Worker can crash after picking task.

Task remains:

```
PROCESSING
```

forever.

## Future Solutions

- timeout
- heartbeat
- retry mechanism
- worker monitoring

---

# 12. Duplicate Execution Problem

## Problem

Worker finishes task but crashes before sending completion message.

System retries.

Same task may execute twice.

## Solution Concept

Idempotency.

Every operation should have a unique identity.

Retries should produce the same final result.

```
Run once = Run multiple times
```
Worker Port Problem

Issue:

Multiple workers failed because port 8080 was already used.

Reason:

Worker was starting Tomcat server.

But current architecture:

Worker ---> API

Worker only sends requests.

It does not receive requests.

Solution:

spring.main.web-application-type=none

Worker now runs without a web server.

Worker Identity

Workers are started with unique IDs:

./mvnw spring-boot:run \
-Dspring-boot.run.arguments="--worker.id=worker-1"

Example:

worker-1
worker-2
worker-3

Each worker gets a unique identity for task ownership.

Final Task Flow
Worker asks for task

        ↓

API receives request

        ↓

Database transaction starts

        ↓

Highest priority QUEUED task is locked

        ↓

Task updated:

PROCESSING
assignedWorker set
startedAt set

        ↓

Worker processes task

        ↓

Worker marks task COMPLETED

### Problems Faced
1. Multiple workers could process same task

Fixed using:

transactions
pessimistic database locks

2. Confusion between transaction and locking

Learning:

Transaction groups operations.// either everyone or nothing

Lock prevents concurrent access.

3. Worker started unnecessary server

Fixed by disabling Tomcat for worker.

4. Multiple workers had same identity

Fixed using worker IDs.

5. Old tasks had assignedWorker=null

Reason:

Column was added after old tasks were already completed.

6. DELETE request failed

Wrong: ```curl DELETE url```

Correct: ```curl -X DELETE url```

because curl needs explicit HTTP method selection.

Architecture After Day 5 : 

             API
              |

          PostgreSQL

              |

      Transaction + Lock

              |
    Worker-1  Worker-2  Worker-3