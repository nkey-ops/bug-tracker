package com.bluesky.bugtraker.io.specification;

import static com.bluesky.bugtraker.io.specification.Specs.projectsBySubscriber;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;

import groovyjarjarantlr4.v4.parse.ANTLRParser.parserRule_return;

@DataJpaTest
@EnableJpaRepositories(
    repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class,
    basePackages = "com.bluesky.bugtraker.io.repository")
public class SpecsTest {

  @Autowired private UserRepository userRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TicketRepository ticketRepository;

  @Test
  void testProjectBySubscriberReturnsSingleProject() {
    var subscribedProject = new ProjectEntity();
    subscribedProject.setPublicId("publicSubscribedProjectId");
    subscribedProject.setName("subscribedProjectName");

    var notSubscribedProject = new ProjectEntity();
    notSubscribedProject.setPublicId("publicNonSubscribedProjectId");
    notSubscribedProject.setName("notSubscribedprojectName");

    var subscriber = new UserEntity();
    subscriber.setPublicId("publicSubscriberId");
    subscriber.setUsername("subscriberUsername");
    subscriber.setEmail("subscriberEmail");
    subscriber.setEncryptedPassword("subscriberEncPassword");
    subscriber.setEmailVerificationStatus(true);

    subscriber = userRepository.save(subscriber);

    subscribedProject.setCreator(subscriber);
    subscribedProject.addSubscriber(subscriber);
    subscribedProject = projectRepository.save(subscribedProject);

    subscriber.getProjects().clear();

    notSubscribedProject.setCreator(subscriber);
    notSubscribedProject = projectRepository.save(notSubscribedProject);

    List<ProjectEntity> projectSubscriptions =
        projectRepository.findAll(projectsBySubscriber(subscriber));

    assertEquals(1, projectSubscriptions.size());
    assertEquals(subscribedProject, projectSubscriptions.get(0));
  }

  @Test
  void testTicketsBySubscriberReturnSingleTicket() {
    var project = new ProjectEntity();
    project.setPublicId("publicProjectId");
    project.setName("projectName");

    var subscriber = new UserEntity();
    subscriber.setPublicId("publicSubscriberId");
    subscriber.setUsername("subscriberUsername");
    subscriber.setEmail("subscriberEmail");
    subscriber.setEncryptedPassword("subscriberEncPassword");
    subscriber.setEmailVerificationStatus(true);
  
    var subscribedTicket =new TicketEntity();
    subscribedTicket.setPublicId("subscribedTicketPublicId");
    subscribedTicket.setShortDescription("description");
    subscribedTicket.setStatus(Status.TO_FIX);
    subscribedTicket.setSeverity(Severity.MINOR);
    subscribedTicket.setPriority(Priority.MEDIUM);
    subscribedTicket.setHowToReproduce("howToReproduce");
    subscribedTicket.setErroneousProgramBehaviour("programBehaviour");
    subscribedTicket.setCreatedTime(new Date());
    subscribedTicket.setLastUpdateTime(new Date());

    var notSubscribedTicket =new TicketEntity();
    notSubscribedTicket.setPublicId("notSubscribedTicketPublicId");
    notSubscribedTicket.setShortDescription("description");
    notSubscribedTicket.setStatus(Status.TO_FIX);
    notSubscribedTicket.setSeverity(Severity.MINOR);
    notSubscribedTicket.setPriority(Priority.MEDIUM);
    notSubscribedTicket.setHowToReproduce("howToReproduce");
    notSubscribedTicket.setErroneousProgramBehaviour("programBehaviour");
    notSubscribedTicket.setCreatedTime(new Date());
    notSubscribedTicket.setLastUpdateTime(new Date());

    subscriber = userRepository.save(subscriber);
    project.setCreator(subscriber);
    project = projectRepository.save(project); 

    subscribedTicket.setReporterEntity(subscriber);
    subscribedTicket.setProjectEntity(project);
    subscribedTicket.addSubscriber(subscriber);
    subscribedTicket = ticketRepository.save(subscribedTicket);

    subscriber.getReportedTickets().clear();
    project.getTickets().clear();

    notSubscribedTicket.setReporterEntity(subscriber);
    notSubscribedTicket.setProjectEntity(project);
    notSubscribedTicket = ticketRepository.save(notSubscribedTicket);

    subscribedTicket = ticketRepository.save(subscribedTicket);
    notSubscribedTicket = ticketRepository.save(notSubscribedTicket);

    List<TicketEntity> subscribedTickets =  ticketRepository.findAll(Specs.ticketsBySubscriber(subscriber));


    assertEquals(1, subscribedTickets.size());
    assertEquals(subscribedTicket, subscribedTickets.get(0));
  }
}
