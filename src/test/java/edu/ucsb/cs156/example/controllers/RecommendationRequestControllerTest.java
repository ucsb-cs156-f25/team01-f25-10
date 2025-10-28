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
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
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

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTest extends ControllerTestCase {

  @MockBean RecommendationRequestRepository recommendationRequestRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/recommendationrequest/admin/all
  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequest/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(200)); // logged
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesteremail("reqmail@mail.com")
            .professoremail("profmail@mail.com")
            .explanation("Test")
            .daterequested(ldt1)
            .dateneeded(ldt2)
            .done(true)
            .build();

    LocalDateTime ldt3 = LocalDateTime.parse("2025-01-03T00:00:01");
    LocalDateTime ldt4 = LocalDateTime.parse("2025-01-03T00:00:01");

    RecommendationRequest recommendationRequest2 =
        RecommendationRequest.builder()
            .requesteremail("reqmail123@mail.com")
            .professoremail("profmail321@mail.com")
            .explanation("Test2")
            .daterequested(ldt3)
            .dateneeded(ldt4)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for get by id
  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequest?id=7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesteremail("reqmail@mail.com")
            .professoremail("profmail@mail.com")
            .explanation("Test")
            .daterequested(ldt1)
            .dateneeded(ldt2)
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(eq(7L)))
        .thenReturn(Optional.of(recommendationRequest1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest?id=7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(recommendationRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  // Authorization tests for /api/recommendationrequest/post
  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/recommendationrequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/recommendationrequest/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesteremail("reqmail@mail.com")
            .professoremail("profmail@mail.com")
            .explanation("Test")
            .daterequested(ldt1)
            .dateneeded(ldt2)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(recommendationRequest1)))
        .thenReturn(recommendationRequest1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequest/post?requesteremail=reqmail@mail.com&professoremail=profmail@mail.com&explanation=Test&daterequested=2025-01-03T00:00:00&dateneeded=2025-01-03T00:00:00&done=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(recommendationRequest1);
    String expectedJson = mapper.writeValueAsString(recommendationRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for DELETE

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_recommendationrequest() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesteremail("reqmail@mail.com")
            .professoremail("profmail@mail.com")
            .explanation("Test")
            .daterequested(ldt1)
            .dateneeded(ldt2)
            .done(true)
            .build();

    when(recommendationRequestRepository.findById(eq(15L)))
        .thenReturn(Optional.of(recommendationRequest1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequest?id=15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void
      admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
          throws Exception {
    // arrange

    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/recommendationrequest?id=15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }

  // Tests for PUT

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:00");

    RecommendationRequest recommendationRequestOrig =
        RecommendationRequest.builder()
            .requesteremail("reqmail@mail.com")
            .professoremail("profmail@mail.com")
            .explanation("Test")
            .daterequested(ldt1)
            .dateneeded(ldt2)
            .done(false)
            .build();

    LocalDateTime ldt3 = LocalDateTime.parse("2025-01-03T00:00:01");
    LocalDateTime ldt4 = LocalDateTime.parse("2025-01-03T00:00:01");

    RecommendationRequest recommendationRequestEdit =
        RecommendationRequest.builder()
            .requesteremail("reqmail123@mail.com")
            .professoremail("profmail321@mail.com")
            .explanation("Test2")
            .daterequested(ldt3)
            .dateneeded(ldt4)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(recommendationRequestEdit);

    when(recommendationRequestRepository.findById(eq(67L)))
        .thenReturn(Optional.of(recommendationRequestOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1))
        .save(recommendationRequestEdit); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2025-01-03T00:00:01");
    LocalDateTime ldt2 = LocalDateTime.parse("2025-01-03T00:00:01");

    RecommendationRequest recommendationRequestEdited =
        RecommendationRequest.builder()
            .requesteremail("reqmail123@mail.com")
            .professoremail("profmail321@mail.com")
            .explanation("Test2")
            .daterequested(ldt1)
            .dateneeded(ldt2)
            .done(true)
            .build();

    String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }
}
