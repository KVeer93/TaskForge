# TaskForge - Architecture Decisions

This document contains important design decisions taken while building TaskForge.

---

# Decision 1: Build Multiple Services Instead Of Single Application

## Decision

Separate:

- API Service
- Worker Service


Architecture:

```
Client

 |

API

 |

Database


Workers
```

## Reason

API should handle requests quickly.

Heavy processing should happen asynchronously.

---

# Decision 2: API Owns The Database

## Options

Option A:

```
Worker

 |

Database
```


Option B:

```
Worker

 |

API

 |

Database
```


Chosen:

Option B.


## Reason

Centralized business logic.

Only API changes task states.

Avoids duplicated rules.

---

# Decision 3: Use PostgreSQL As Initial Queue

## Decision

Store tasks in database:

```
tasks table
```

with:

- status
- priority
- timestamps

## Reason

Start simple before adding queue systems.

Later:

Database queue can evolve into Redis/Kafka.

---

# Decision 4: Task Lifecycle

Current lifecycle:

```
QUEUED

   |

PROCESSING

   |

COMPLETED / FAILED
```


Reason:

Need to track execution state clearly.

---

# Decision 5: Store Task Metadata

Task contains:

```
id

title

type

status

priority

createdAt

startedAt

finishedAt

errorMessage
```


Reason:

Needed for:

- debugging
- scheduling
- retries
- monitoring

---

# Decision 6: Use Pull Based Workers

Chosen:

```
Worker asks API for work
```


Instead of:

```
API pushes work to Worker
```


## Reason

Easier scaling.

Workers can join and leave independently.

No worker discovery needed.

---

# Decision 7: Workers Do Not Expose APIs Initially

Worker does not need:

```
localhost:8081
```


Reason:

Nobody communicates directly with worker.

Worker communicates outward.

---

# Decision 8: Use DTOs Between Services

Example:

API sends:

```
TaskResponse
```


Worker receives DTO.

Reason:

Avoid exposing database entities everywhere.

---

# Decision 9: Prepare For Worker Failure

Future additions:

Tasks will contain:

```
assignedWorker

heartbeat

retryCount

timeout
```


Reason:

Distributed systems fail.

Failure must be expected.

---

# Decision 10: Idempotent Execution

Future addition:

Tasks will contain:

```
idempotencyKey
```


Reason:

Retries should not accidentally execute dangerous actions multiple times.

---

# Overall Architecture Goal

TaskForge should evolve from:

Simple:

```
API

Database

Worker
```


Into:

Distributed:

```
             API

              |

            Queue

              |

     Worker Worker Worker


Monitoring

Retries

Scaling
```