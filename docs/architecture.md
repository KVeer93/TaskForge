# TaskForge Architecture

## Why am I building TaskForge?

I started this project to understand how backend systems handle long-running operations, background processing, workers, queues, and scaling.

Normally, when a user sends a request to a backend server, the server receives the request, performs the complete operation, and then sends a response.

For small operations this works.

Example:

User Request
→ Backend Server
→ Process Task
→ Return Result

But problems appear when the task becomes expensive:

* processing large files
* sending thousands of emails
* generating reports
* running time-consuming operations

The backend remains occupied until the task finishes.

This can create problems:

* slow responses
* request timeouts
* poor scalability
* inefficient resource usage

## New Approach

Instead of making the API server execute everything, TaskForge separates task creation from task execution.

The API server's responsibility:

1. Receive the user's request
2. Validate it
3. Create a task
4. Store it in the queue/database
5. Immediately return a response

The worker application's responsibility:

1. Find available tasks
2. Pick a task safely
3. Execute it
4. Update the result

## Version 1 Architecture

Client
    ↓
Spring Boot API
    ↓
PostgreSQL Database Queue
    ↓
Worker Application
    ↓
Task Completed

In the first version, PostgreSQL itself acts as the queue.

Tasks are stored with statuses:

* QUEUED
* PROCESSING
* COMPLETED
* FAILED

Workers continuously look for queued tasks and process them.

## Why start with a database queue?

The goal is not only to use existing tools but understand why those tools exist.

By creating our own database-backed queue, I want to understand problems like:

* multiple workers selecting the same task
* race conditions
* locking
* scaling limitations

After understanding these problems, Redis will be introduced as a proper queue system.

## Future Improvements

Planned improvements:

* Multiple worker instances
* Priority based scheduling
* Retry mechanism
* Failed task handling
* Redis queue
* Monitoring dashboard
* Docker deployment

The goal of TaskForge is not just to build a project.

The goal is to understand why modern backend architectures evolved the way they did.