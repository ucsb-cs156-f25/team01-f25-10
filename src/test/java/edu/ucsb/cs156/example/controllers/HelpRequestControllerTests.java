package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockBean HelpRequestRepository helpRequestRepository;

  @MockBean UserRepository userRepository;

  // test for GET /api/helprequest/all

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_helprequests() throws Exception {
    // arrange
    LocalDateTime t1 = LocalDateTime.parse("2025-10-01T10:00:00");
    LocalDateTime t2 = LocalDateTime.parse("2025-10-02T11:30:00");

    HelpRequest hr1 =
        HelpRequest.builder()
            .requesterEmail("test1@ucsb.edu")
            .teamId("f25-01")
            .tableOrBreakoutRoom("Table 7")
            .requestTime(t1)
            .explanation("Need help with setup")
            .solved(false)
            .build();

    HelpRequest hr2 =
        HelpRequest.builder()
            .requesterEmail("test2@ucsb.edu")
            .teamId("f25-02")
            .tableOrBreakoutRoom("Breakout Room 1")
            .requestTime(t2)
            .explanation("Unable to git clone repo")
            .solved(true)
            .build();

    var expected = new ArrayList<>(Arrays.asList(hr1, hr2));
    when(helpRequestRepository.findAll()).thenReturn(expected);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequest/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_all_returns_empty_list_when_none_exist() throws Exception {
    when(helpRequestRepository.findAll()).thenReturn(new ArrayList<>());

    MvcResult response =
        mockMvc.perform(get("/api/helprequest/all")).andExpect(status().isOk()).andReturn();

    assertEquals("[]", response.getResponse().getContentAsString());
    verify(helpRequestRepository, times(1)).findAll();
  }

  // test for GET /api/helprequest?id=

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime t1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("test@ucsb.edu")
            .teamId("f25-10")
            .tableOrBreakoutRoom("Table 5")
            .requestTime(t1)
            .explanation("Need help with git")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest));

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequest?id=7")).andExpect(status().isOk()).andReturn();

    // assert

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    // arrange
    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequest?id=7")).andExpect(status().isNotFound()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  // tests for POST /api/helprequest/post

  @WithMockUser(roles = {"USER"})
  @Test
  public void user_can_post_new_helprequest() throws Exception {
    // arrange
    String requesterEmail = "test1@ucsb.edu";
    String teamId = "f25-10";
    String table = "Table 3";
    String when = "2025-10-03T14:45:00";
    String explanation = "Swagger POST failing";
    String solved = "true";

    HelpRequest expected =
        HelpRequest.builder()
            .requesterEmail(requesterEmail)
            .teamId(teamId)
            .tableOrBreakoutRoom(table)
            .requestTime(LocalDateTime.parse(when))
            .explanation(explanation)
            .solved(Boolean.parseBoolean(solved)) // true
            .build();

    when(helpRequestRepository.save(org.mockito.ArgumentMatchers.refEq(expected, "id")))
        .thenReturn(expected);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/helprequest/post")
                    .with(csrf())
                    .param("requesterEmail", requesterEmail)
                    .param("teamId", teamId)
                    .param("tableOrBreakoutRoom", table)
                    .param("requestTime", when)
                    .param("explanation", explanation)
                    .param("solved", solved)) // "true"
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1))
        .save(org.mockito.ArgumentMatchers.refEq(expected, "id"));
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // test for DELETE /api/helprequest?id=

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void test_user_can_delete_a_helprequest() throws Exception {
    // arrange

    LocalDateTime t1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("test@ucsb.edu")
            .teamId("f25-10")
            .tableOrBreakoutRoom("Table 5")
            .requestTime(t1)
            .explanation("Need help with mac")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.of(helpRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/helprequest?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(15L);
    verify(helpRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void user_tries_to_delete_non_existant_helprequest_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/helprequest?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 15 not found", json.get("message"));
  }

  // test for PUT /api/helprequest?id=

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_user_can_update_existing_helprequest() throws Exception {
    // arrange
    LocalDateTime oldTime = LocalDateTime.parse("2025-10-01T10:00:00");
    LocalDateTime newTime = LocalDateTime.parse("2025-10-02T11:30:00");

    HelpRequest original =
        HelpRequest.builder()
            .requesterEmail("test@ucsb.edu")
            .teamId("f25-10")
            .tableOrBreakoutRoom("Table 1")
            .requestTime(oldTime)
            .explanation("Old explanation")
            .solved(false)
            .build();

    HelpRequest edited =
        HelpRequest.builder()
            .requesterEmail("test2@ucsb.edu")
            .teamId("f25-12")
            .tableOrBreakoutRoom("Table 2")
            .requestTime(newTime)
            .explanation("New explanation")
            .solved(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(original));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(67L);
    verify(helpRequestRepository, times(1)).save(edited);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_user_cannot_update_nonexistent_helprequest() throws Exception {
    // arrange
    HelpRequest edited =
        HelpRequest.builder()
            .requesterEmail("test@ucsb.edu")
            .teamId("f25-10")
            .tableOrBreakoutRoom("Table 2")
            .requestTime(LocalDateTime.parse("2025-10-02T11:30:00"))
            .explanation("New explanation")
            .solved(true)
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(67L);

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 67 not found", json.get("message"));
  }
}
