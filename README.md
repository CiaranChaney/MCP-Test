# MCP Database Tool

This project implements a Model Context Protocol (MCP) server with database access capabilities using Spring Boot, with OAuth2 authentication support.

## Overview

The MCP server provides tools to interact with an in-memory H2 database for managing tasks. This demonstrates how to create MCP tools that can perform CRUD operations on a database. The project includes a full OAuth2 implementation with an agent app client that communicates securely with the MCP server.

## Architecture

- **MCP Server** (port 8081): OAuth2 Resource Server protecting MCP tools
- **Agent App** (port 8082): OAuth2 Client that uses the MCP server tools
- **OAuth2 Provider**: Keycloak, Auth0, Okta, or any OAuth2-compliant provider

## Features

- **Database Integration**: Uses Spring Data JPA with H2 in-memory database
- **MCP Tools**: Exposes database operations as MCP tools
- **Task Management**: Create, read, update, delete, and search tasks
- **OAuth2 Security**: Full OAuth2 authentication and authorization
- **Development Mode**: Optional no-auth mode for local development
- **Flexible Providers**: Support for Keycloak, Auth0, Okta, Azure AD, and more

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
- **Spring Security**: OAuth2 Resource Server and Client
- **Java 21**: Programming language

## Quick Start

### Option 1: Development Mode (No Authentication)

For quick local testing without OAuth2:

```powershell
# Terminal 1 - Start MCP Server
.\gradlew.bat :mcp-server:bootRun --args='--spring.profiles.active=dev'

# Terminal 2 - Start Agent App
.\gradlew.bat :agent-app:bootRun --args='--spring.profiles.active=dev'

# Test
Invoke-RestMethod -Uri "http://localhost:8082/agent/ask" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"prompt": "Create a task called Test Task"}'
```

### Option 2: Production Mode (With OAuth2)

For full OAuth2 security:

1. **Set up OAuth2 provider** (see [OAUTH2_PROVIDER_COMPARISON.md](OAUTH2_PROVIDER_COMPARISON.md)):
   ```powershell
   .\setup-keycloak.ps1  # For Keycloak
   ```

2. **Configure environment**:
   ```powershell
   Copy-Item .env.template .env
   # Edit .env with your OAuth2 credentials
   ```

3. **Start applications**:
   ```powershell
   .\gradlew.bat :mcp-server:bootRun
   .\gradlew.bat :agent-app:bootRun
   ```

4. **Test OAuth2**:
   ```powershell
   .\test-oauth2.ps1
   ```

For detailed setup instructions, see:
- **[QUICK_START.md](QUICK_START.md)** - Quick reference guide
- **[OAUTH2_SETUP.md](OAUTH2_SETUP.md)** - Comprehensive OAuth2 setup
- **[OAUTH2_PROVIDER_COMPARISON.md](OAUTH2_PROVIDER_COMPARISON.md)** - Compare OAuth2 providers
- **[OAUTH2_IMPLEMENTATION_SUMMARY.md](OAUTH2_IMPLEMENTATION_SUMMARY.md)** - Implementation details

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
mcp/
├── agent-app/                                  # OAuth2 Client Application
│   ├── src/main/java/.../agentapp/
│   │   ├── AgentAppApplication.java           # Main application
│   │   ├── AgentController.java               # REST endpoint for agent
│   │   ├── SecurityConfig.java                # OAuth2 client security
│   │   ├── DevSecurityConfig.java             # Dev mode (no auth)
│   │   └── OAuth2WebClientConfig.java         # WebClient with OAuth2
│   └── src/main/resources/
│       └── application.yml                     # Client configuration
│
├── mcp-server/                                 # OAuth2 Resource Server
│   ├── src/main/java/.../mcpserver/
│   │   ├── McpServerApplication.java          # Main application
│   │   ├── ToolsConfig.java                   # MCP tools definition
│   │   ├── Task.java                          # Task entity
│   │   ├── TaskRepository.java                # Database repository
│   │   ├── SecurityConfig.java                # OAuth2 resource server
│   │   └── DevSecurityConfig.java             # Dev mode (no auth)
│   └── src/main/resources/
│       └── application.yml                     # Server configuration
│
├── OAUTH2_SETUP.md                            # Comprehensive OAuth2 guide
├── OAUTH2_PROVIDER_COMPARISON.md              # Compare OAuth2 providers
├── OAUTH2_IMPLEMENTATION_SUMMARY.md           # Implementation details
├── QUICK_START.md                             # Quick reference
├── .env.template                              # Environment variables template
├── setup-keycloak.ps1                         # Keycloak setup script
├── test-oauth2.ps1                            # OAuth2 test script
└── build.gradle                               # Root build configuration
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
