package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.shared.dto.TicketRecordDTO;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {
    public static void assertEqualsRole(RoleEntity expected, RoleEntity actual){
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRole(), actual.getRole());
        assertIterableEquals(expected.getUsers(), actual.getUsers());
    }
    
    public static void assertEqualsUser(UserEntity expected, UserEntity actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPublicId(), actual.getPublicId());
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getAvatarURL(), actual.getAvatarURL());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getEncryptedPassword(), actual.getEncryptedPassword());
        assertEquals(expected.getEmailVerificationToken(), actual.getEmailVerificationToken());
        assertEquals(expected.getEmailVerificationStatus(), actual.getEmailVerificationStatus());
        assertEquals(expected.getRoleEntity(), actual.getRoleEntity());

        assertIterableEquals(expected.getProjects(), actual.getProjects());
        assertIterableEquals(expected.getSubscribedToProjects(), actual.getReportedTickets());

        assertIterableEquals(expected.getReportedTickets(), actual.getReportedTickets());
        assertIterableEquals(expected.getSubscribedToTickets(), actual.getSubscribedToTickets());
        assertIterableEquals(expected.getComments(), actual.getComments());
    }


    public static void assertEqualsProject(ProjectEntity expected, ProjectEntity actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPublicId(), actual.getPublicId());
        assertEquals(expected.getName(), actual.getName());
        assertEqualsUser(expected.getCreator(), actual.getCreator());
        assertIterableEquals(expected.getTickets(), actual.getTickets());
        assertIterableEquals(expected.getSubscribers(), actual.getSubscribers());
        assertIterableEquals(expected.getComments(), actual.getComments());
    }

    public static void assertEqualsTicket(TicketEntity expected, TicketEntity actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPublicId(), actual.getPublicId());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getSeverity(), actual.getSeverity());
        assertEquals(expected.getPriority(), actual.getPriority());
        assertEquals(expected.getHowToReproduce(), actual.getHowToReproduce());
        assertEquals(expected.getHowToSolve(), actual.getHowToSolve());
    }

    public static void assertEqualsTicketRecord(TicketRecordDTO expected, TicketRecordDTO actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPublicId(), actual.getPublicId());
        assertEquals(expected.getCreatedTime(), actual.getCreatedTime());
        assertEquals(expected.getMainTicket(), actual.getMainTicket());
        assertEquals(expected.getCreator(), actual.getCreator());

        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getSeverity(), actual.getSeverity());
        assertEquals(expected.getPriority(), actual.getPriority());
        assertEquals(expected.getHowToReproduce(), actual.getHowToReproduce());
        assertEquals(expected.getHowToSolve(), actual.getHowToSolve());
    }

    public static void assertEqualsTicket(TicketDTO expected, TicketDTO actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getPublicId(), actual.getPublicId());
        assertEquals(expected.getShortDescription(), actual.getShortDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getSeverity(), actual.getSeverity());
        assertEquals(expected.getPriority(), actual.getPriority());
        assertEquals(expected.getHowToReproduce(), actual.getHowToReproduce());
        assertEquals(expected.getHowToSolve(), actual.getHowToSolve());
    }

}
