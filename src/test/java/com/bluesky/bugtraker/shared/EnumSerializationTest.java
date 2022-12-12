package com.bluesky.bugtraker.shared;

import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumSerializationTest {

    private static ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }


    @ParameterizedTest
    @EnumSource(Role.class)
    void roleSerialization(Role role) throws JsonProcessingException {
        String serializedRole = objectMapper.writeValueAsString(role);
        
        assertTrue(serializedRole.contains(role.getName()), 
                "Json should contain name:" + role.getName());
        assertTrue(serializedRole.contains(role.getText()), 
                "Json should contain text:" + role.getText());

        Role deserializedRole = objectMapper.readValue(serializedRole, Role.class);
        assertEquals(role, deserializedRole, "Role is not deserialized properly");

    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void statusSerialization(Status status) throws JsonProcessingException {
        String serializedStatus = objectMapper.writeValueAsString(status);
        
        assertTrue(serializedStatus.contains(status.getName()), 
                "Json should contain name:" + status.getName());
        assertTrue(serializedStatus.contains(status.getText()), 
                "Json should contain text:" + status.getText());

        Status deserializedStatus = objectMapper.readValue(serializedStatus, Status.class);
        assertEquals(status, deserializedStatus, "Status is not deserialized properly");
    }

    @ParameterizedTest
    @EnumSource(Severity.class)
    void severitySerialization(Severity severity) throws JsonProcessingException {
        String serializedSeverity = objectMapper.writeValueAsString(severity);
        
        assertTrue(serializedSeverity.contains(severity.getName()), 
                "Json should contain name:" + severity.getName());
        assertTrue(serializedSeverity.contains(severity.getText()), 
                "Json should contain text:" + severity.getText());

        Severity deserializedSeverity = objectMapper.readValue(serializedSeverity, Severity.class);
        assertEquals(severity, deserializedSeverity, "Severity is not deserialized properly");
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void severitySerialization(Priority priority) throws JsonProcessingException {
        String serializedPriority = objectMapper.writeValueAsString(priority);
        
        assertTrue(serializedPriority.contains(priority.getName()), 
                "Json should contain name:" + priority.getName());
        assertTrue(serializedPriority.contains(priority.getText()), 
                "Json should contain text:" + priority.getText());

        Priority deserializedPriority = objectMapper.readValue(serializedPriority, Priority.class);
        assertEquals(priority, deserializedPriority, "Priority is not deserialized properly");
    }
}
