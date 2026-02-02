package com.ciaranchaney.mcpserver;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class ToolsConfig {

    private final TaskRepository taskRepository;

    public ToolsConfig(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @McpTool(name = "sayHello", description = "Say hello to a person.")
    public Map<String, Object> sayHello(
            @McpToolParam(description = "Name of the person", required = true) String name
    ) {
        return Map.of(
                "message", "Hello " + name + " 👋",
                "timestamp", Instant.now().toString()
        );
    }

    @McpTool(name = "add", description = "Add two integers.")
    public Map<String, Object> add(
            @McpToolParam(description = "First number", required = true) int a,
            @McpToolParam(description = "Second number", required = true) int b
    ) {
        return Map.of(
                "a", a,
                "b", b,
                "sum", a + b
        );
    }

    @McpTool(name = "createTask", description = "Create a new task in the database.")
    public Map<String, Object> createTask(
            @McpToolParam(description = "Title of the task", required = true) String title,
            @McpToolParam(description = "Description of the task", required = false) String description
    ) {
        Task task = new Task(title, description);
        Task savedTask = taskRepository.save(task);
        
        return Map.of(
                "id", savedTask.getId(),
                "title", savedTask.getTitle(),
                "description", savedTask.getDescription() != null ? savedTask.getDescription() : "",
                "completed", savedTask.getCompleted(),
                "createdAt", savedTask.getCreatedAt().toString()
        );
    }

    @McpTool(name = "getAllTasks", description = "Get all tasks from the database.")
    public Map<String, Object> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        
        List<Map<String, Object>> taskList = tasks.stream()
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("description", task.getDescription() != null ? task.getDescription() : "");
                    taskMap.put("completed", task.getCompleted());
                    taskMap.put("createdAt", task.getCreatedAt().toString());
                    if (task.getCompletedAt() != null) {
                        taskMap.put("completedAt", task.getCompletedAt().toString());
                    }
                    return taskMap;
                })
                .collect(Collectors.toList());
        
        return Map.of(
                "tasks", taskList,
                "count", tasks.size()
        );
    }

    @McpTool(name = "getTaskById", description = "Get a task by its ID from the database.")
    public Map<String, Object> getTaskById(
            @McpToolParam(description = "ID of the task", required = true) Long id
    ) {
        return taskRepository.findById(id)
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("description", task.getDescription() != null ? task.getDescription() : "");
                    taskMap.put("completed", task.getCompleted());
                    taskMap.put("createdAt", task.getCreatedAt().toString());
                    if (task.getCompletedAt() != null) {
                        taskMap.put("completedAt", task.getCompletedAt().toString());
                    }
                    return (Map<String, Object>) taskMap;
                })
                .orElse(Map.of("error", "Task not found with id: " + id));
    }

    @McpTool(name = "updateTask", description = "Update a task in the database.")
    public Map<String, Object> updateTask(
            @McpToolParam(description = "ID of the task", required = true) Long id,
            @McpToolParam(description = "New title of the task", required = false) String title,
            @McpToolParam(description = "New description of the task", required = false) String description,
            @McpToolParam(description = "Whether the task is completed", required = false) Boolean completed
    ) {
        return taskRepository.findById(id)
                .map(task -> {
                    if (title != null) {
                        task.setTitle(title);
                    }
                    if (description != null) {
                        task.setDescription(description);
                    }
                    if (completed != null) {
                        task.setCompleted(completed);
                    }
                    Task updatedTask = taskRepository.save(task);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", updatedTask.getId());
                    result.put("title", updatedTask.getTitle());
                    result.put("description", updatedTask.getDescription() != null ? updatedTask.getDescription() : "");
                    result.put("completed", updatedTask.getCompleted());
                    result.put("createdAt", updatedTask.getCreatedAt().toString());
                    if (updatedTask.getCompletedAt() != null) {
                        result.put("completedAt", updatedTask.getCompletedAt().toString());
                    }
                    return (Map<String, Object>) result;
                })
                .orElse(Map.of("error", "Task not found with id: " + id));
    }

    @McpTool(name = "deleteTask", description = "Delete a task from the database.")
    public Map<String, Object> deleteTask(
            @McpToolParam(description = "ID of the task to delete", required = true) Long id
    ) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return Map.of(
                    "success", true,
                    "message", "Task with id " + id + " was deleted successfully"
            );
        } else {
            return Map.of(
                    "success", false,
                    "error", "Task not found with id: " + id
            );
        }
    }

    @McpTool(name = "searchTasks", description = "Search tasks by title.")
    public Map<String, Object> searchTasks(
            @McpToolParam(description = "Search term for task title", required = true) String searchTerm
    ) {
        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCase(searchTerm);
        
        List<Map<String, Object>> taskList = tasks.stream()
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("description", task.getDescription() != null ? task.getDescription() : "");
                    taskMap.put("completed", task.getCompleted());
                    taskMap.put("createdAt", task.getCreatedAt().toString());
                    if (task.getCompletedAt() != null) {
                        taskMap.put("completedAt", task.getCompletedAt().toString());
                    }
                    return taskMap;
                })
                .collect(Collectors.toList());
        
        return Map.of(
                "tasks", taskList,
                "count", tasks.size(),
                "searchTerm", searchTerm
        );
    }

    @McpTool(name = "getTasksByStatus", description = "Get tasks filtered by completion status.")
    public Map<String, Object> getTasksByStatus(
            @McpToolParam(description = "Completion status (true for completed, false for incomplete)", required = true) Boolean completed
    ) {
        List<Task> tasks = taskRepository.findByCompleted(completed);
        
        List<Map<String, Object>> taskList = tasks.stream()
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("description", task.getDescription() != null ? task.getDescription() : "");
                    taskMap.put("completed", task.getCompleted());
                    taskMap.put("createdAt", task.getCreatedAt().toString());
                    if (task.getCompletedAt() != null) {
                        taskMap.put("completedAt", task.getCompletedAt().toString());
                    }
                    return taskMap;
                })
                .collect(Collectors.toList());
        
        return Map.of(
                "tasks", taskList,
                "count", tasks.size(),
                "status", completed ? "completed" : "incomplete"
        );
    }
}
