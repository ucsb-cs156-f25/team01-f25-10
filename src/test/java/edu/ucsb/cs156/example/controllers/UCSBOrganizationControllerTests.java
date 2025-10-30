package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
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
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
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

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsborganization/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization?orgcode=ACM"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for /api/ucsborganization/post
  // (Perhaps should also have these for put and delete)
  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsborganization/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/ucsborganization/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // Tests with mocks for database actions
  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgcode("ACM")
            .orgTranslationShort("ACM")
            .orgTranslation("Association for Computing Machinery")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ACM"))).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgcode=ACM"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("ACM"));
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("XYZ"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization?orgcode=XYZ"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById(eq("XYZ"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id XYZ not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    // arrange
    UCSBOrganization org1 =
        UCSBOrganization.builder()
            .orgcode("ACM")
            .orgTranslationShort("ACM")
            .orgTranslation("Association for Computing Machinery")
            .inactive(false)
            .build();

    UCSBOrganization org2 =
        UCSBOrganization.builder()
            .orgcode("SWE")
            .orgTranslationShort("SWE")
            .orgTranslation("Society of Women Engineers")
            .inactive(false)
            .build();
    ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>(Arrays.asList(org1, org2));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrgs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    // arrange

    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgcode("ACM")
            .orgTranslationShort("ACM")
            .orgTranslation("Association for Computing Machinery")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(org))).thenReturn(org);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post?orgcode=ACM&orgTranslationShort=ACM&orgTranslation=Association for Computing Machinery&inactive=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(eq(org));
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
    UCSBOrganization responseOrg = mapper.readValue(responseString, UCSBOrganization.class);
    assertEquals("ACM", responseOrg.getOrgcode());
    assertEquals("ACM", responseOrg.getOrgTranslationShort());
    assertEquals("Association for Computing Machinery", responseOrg.getOrgTranslation());
    assertEquals(true, responseOrg.getInactive());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_organization() throws Exception {
    // arrange

    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgcode("ACM")
            .orgTranslationShort("ACM")
            .orgTranslation("Association for Computing Machinery")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ACM"))).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgcode=ACM").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ACM");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id ACM deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbOrganizationRepository.findById(eq("XYZ"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization?orgcode=XYZ").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("XYZ");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id XYZ not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    // arrange

    UCSBOrganization orgOrig =
        UCSBOrganization.builder()
            .orgcode("ACM")
            .orgTranslationShort("ACM")
            .orgTranslation("Association for Computing Machinery")
            .inactive(false)
            .build();

    UCSBOrganization orgEdited =
        UCSBOrganization.builder()
            .orgcode("ACM")
            .orgTranslationShort("Association for Computing Machinery")
            .orgTranslation("The ACM Student Chapter at UCSB")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(orgEdited);
    when(ucsbOrganizationRepository.findById(eq("ACM"))).thenReturn(Optional.of(orgOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgcode=ACM")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ACM");
    verify(ucsbOrganizationRepository, times(1))
        .save(orgEdited); // should be saved with updated info
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    // arrange

    UCSBOrganization editedOrg =
        UCSBOrganization.builder()
            .orgcode("XYZ")
            .orgTranslationShort("XYZ Club")
            .orgTranslation("Example Student Organization")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrg);

    when(ucsbOrganizationRepository.findById(eq("XYZ"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization?orgcode=XYZ")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("XYZ");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id XYZ not found", json.get("message"));
  }
}
