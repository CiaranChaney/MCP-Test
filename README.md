# MCP Database Tool

This project implements a Model Context Protocol (MCP) server with database access capabilities using Spring Boot.

## Overview

The MCP server provides tools to interact with an in-memory H2 database for managing tasks. This demonstrates how to create MCP tools that can perform CRUD operations on a database.

## Features

- **Database Integration**: Uses Spring Data JPA with H2 in-memory database
- **MCP Tools**: Exposes database operations as MCP tools
- **Task Management**: Create, read, update, delete, and search tasks

## MCP Tools Available

### 1. createTask
Create a new task in the database.
- **Parameters**:
  - `title` (required): Title of the task
  - `description` (optional): Description of the task
- **Returns**: Created task with ID and timestamp

### 2. getAllTasks
Get all tasks from the database.
- **Returns**: List of all tasks and count

### 3. getTaskById
Get a specific task by its ID.
- **Parameters**:
  - `id` (required): ID of the task
- **Returns**: Task details or error if not found

### 4. updateTask
Update an existing task.
- **Parameters**:
  - `id` (required): ID of the task to update
  - `title` (optional): New title
  - `description` (optional): New description
  - `completed` (optional): Completion status
- **Returns**: Updated task details

### 5. deleteTask
Delete a task from the database.
- **Parameters**:
  - `id` (required): ID of the task to delete
- **Returns**: Success/failure status

### 6. searchTasks
Search tasks by title.
- **Parameters**:
  - `searchTerm` (required): Search term for task title
- **Returns**: Matching tasks

### 7. getTasksByStatus
Get tasks filtered by completion status.
- **Parameters**:
  - `completed` (required): true for completed tasks, false for incomplete
- **Returns**: Filtered tasks

## Database Schema

The application uses a `Task` entity with the following fields:
- `id`: Auto-generated Long (primary key)
- `title`: String (required)
- `description`: String (optional, max 1000 chars)
- `completed`: Boolean (default: false)
- `createdAt`: Timestamp (auto-generated)
- `completedAt`: Timestamp (set when task is marked complete)

## Technology Stack

- **Spring Boot 3.4.2**: Application framework
- **Spring Data JPA**: Database access layer
- **H2 Database**: In-memory database
- **Spring AI MCP Server**: MCP server implementation
- **Java 21**: Programming language

## Building and Running

### Prerequisites
- Java 21 or higher
- Gradle (wrapper included)

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Run Application
```bash
./gradlew :mcp-server:bootRun
```

The server will start on port 8081 (configured in application.yml).

## Configuration

Database configuration is in `mcp-server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:mcpdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console
```

### H2 Console Access

When the application is running, you can access the H2 console at:
- URL: http://localhost:8081/h2-console
- JDBC URL: jdbc:h2:mem:mcpdb
- Username: sa
- Password: (leave empty)

## Project Structure

```
mcp-server/
├── src/
│   ├── main/
│   │   ├── java/com/ciaranchaney/mcpserver/
│   │   │   ├── McpServerApplication.java    # Main application class
│   │   │   ├── ToolsConfig.java              # MCP tools definition
│   │   │   ├── Task.java                      # Task entity
│   │   │   └── TaskRepository.java           # Database repository
│   │   └── resources/
│   │       └── application.yml                # Configuration
│   └── test/
│       └── java/com/ciaranchaney/mcpserver/
│           └── DatabaseToolsTest.java         # Database tool tests
└── build.gradle                               # Project dependencies
```

## Testing

The project includes comprehensive tests for all database operations:
- Task creation
- Task retrieval (all and by ID)
- Task updates
- Task deletion
- Task search by title
- Task filtering by status

Run tests with:
```bash
./gradlew :mcp-server:test
```

## Example Usage

Once the server is running, MCP clients can call the tools. Here are some examples:

1. **Create a task**:
   - Tool: `createTask`
   - Args: `{"title": "Write documentation", "description": "Create README file"}`

2. **Get all tasks**:
   - Tool: `getAllTasks`
   - Args: `{}`

3. **Update a task**:
   - Tool: `updateTask`
   - Args: `{"id": 1, "completed": true}`

4. **Search tasks**:
   - Tool: `searchTasks`
   - Args: `{"searchTerm": "documentation"}`

## Extending the Database

To add more database functionality:

1. Create new entities in the same package
2. Create corresponding repository interfaces
3. Add new MCP tools in `ToolsConfig.java` using `@McpTool` annotations
4. Update `application.yml` if needed for additional configuration

## License

This project is for demonstration purposes.
