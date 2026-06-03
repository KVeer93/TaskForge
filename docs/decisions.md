# Engineering Decisions

## Decision 1: Separate API and Worker

Initially, it might seem easier to execute tasks directly inside the API server.

I decided not to do this.

Reason:

The API server should handle user communication.

Workers should handle heavy processing.

This allows both parts of the system to scale independently.

## Decision 2: Starting with PostgreSQL instead of Redis

Many production systems use dedicated queue technologies.

However, TaskForge starts with PostgreSQL.

Reason:

I want to experience the limitations first.

Problems I expect:

* inefficient polling
* worker conflicts
* concurrency issues

After understanding these problems, Redis will be introduced.

## Decision 3: Worker Count

One task does not mean one worker.

Workers are limited resources.

A small number of workers should process a large number of queued tasks.

Example:

10 workers can process thousands of tasks by continuously consuming from the queue.

## Decision 4: Scheduling Strategy

Initial version:

First Come First Serve.

Later versions:

Priority based scheduling can be introduced.

Higher priority tasks will execute before lower priority tasks.