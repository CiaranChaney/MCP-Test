package com.ciaranchaney.mcpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseToolsTest {

    @Autowired
    private ToolsConfig toolsConfig;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testCreateTask() {
        Map<String, Object> result = toolsConfig.createTask("Test Task", "This is a test task");
        
        assertNotNull(result);
        assertTrue(result.containsKey("id"));
        assertEquals("Test Task", result.get("title"));
        assertEquals("This is a test task", result.get("description"));
        assertEquals(false, result.get("completed"));
        assertTrue(result.containsKey("createdAt"));
    }

    @Test
    void testGetAllTasks() {
        // Create a task first
        toolsConfig.createTask("Task 1", "Description 1");
        toolsConfig.createTask("Task 2", "Description 2");
        
        Map<String, Object> result = toolsConfig.getAllTasks();
        
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        assertTrue(result.containsKey("count"));
        assertTrue((Integer) result.get("count") >= 2);
    }

    @Test
    void testUpdateTask() {
        // Create a task
        Map<String, Object> created = toolsConfig.createTask("Original Title", "Original Description");
        Long taskId = (Long) created.get("id");
        
        // Update the task
        Map<String, Object> updated = toolsConfig.updateTask(taskId, "Updated Title", "Updated Description", true);
        
        assertNotNull(updated);
        assertEquals("Updated Title", updated.get("title"));
        assertEquals("Updated Description", updated.get("description"));
        assertEquals(true, updated.get("completed"));
        assertTrue(updated.containsKey("completedAt"));
    }

    @Test
    void testDeleteTask() {
        // Create a task
        Map<String, Object> created = toolsConfig.createTask("Task to Delete", "Will be deleted");
        Long taskId = (Long) created.get("id");
        
        // Delete the task
        Map<String, Object> result = toolsConfig.deleteTask(taskId);
        
        assertNotNull(result);
        assertEquals(true, result.get("success"));
        
        // Verify it's deleted
        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    void testSearchTasks() {
        // Create tasks with specific titles
        toolsConfig.createTask("Java Programming", "Learn Java");
        toolsConfig.createTask("Python Programming", "Learn Python");
        
        // Search for Java
        Map<String, Object> result = toolsConfig.searchTasks("Java");
        
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        assertEquals("Java", result.get("searchTerm"));
    }

    @Test
    void testGetTasksByStatus() {
        // Create a completed task
        Map<String, Object> created = toolsConfig.createTask("Completed Task", "This is completed");
        Long taskId = (Long) created.get("id");
        toolsConfig.updateTask(taskId, null, null, true);
        
        // Get completed tasks
        Map<String, Object> result = toolsConfig.getTasksByStatus(true);
        
        assertNotNull(result);
        assertTrue(result.containsKey("tasks"));
        assertEquals("completed", result.get("status"));
    }

    @Test
    void testCompletedAtTimestamp() {
        // Create a task
        Map<String, Object> created = toolsConfig.createTask("Task with timestamp", "Test");
        Long taskId = (Long) created.get("id");
        
        // Mark as completed
        Map<String, Object> completed = toolsConfig.updateTask(taskId, null, null, true);
        assertTrue(completed.containsKey("completedAt"));
        
        // Mark as incomplete again
        Map<String, Object> incompleted = toolsConfig.updateTask(taskId, null, null, false);
        assertFalse(incompleted.containsKey("completedAt"));
    }
}
