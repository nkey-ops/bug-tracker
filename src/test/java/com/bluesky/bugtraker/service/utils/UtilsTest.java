package com.bluesky.bugtraker.service.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bluesky.bugtraker.BugTrackerApplication;
import com.bluesky.bugtraker.TestConfigurations;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {BugTrackerApplication.class, TestConfigurations.class})
public class UtilsTest {

  @InjectMocks Utils utils;

  @Test
  void testTokenIsNotExpired() {
    var token = utils.getEmailVerificationToken("publicId");

    assertNotNull(token);
    assertFalse(utils.hasEmailTokenExpired(token));
  }
}
