package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequest */
@Tag(name = "RecommendationRequest")
@RequestMapping("/api/recommendationrequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all Recommendation Requests
   *
   * @return an iterable of RecommendationRequest
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> recommendationRequests =
        recommendationRequestRepository.findAll();
    return recommendationRequests;
  }

  /**
   * Get a single request by id
   *
   * @param id the id of the request
   * @return a RecommendationRequest
   */
  @Operation(summary = "Get a single recommendation request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recommendationRequest;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesteremail the email of the requester
   * @param professoremail the email of the professor
   * @param explanation explanation
   * @param daterequested the date requested
   * @param dateneeded the date needed
   * @param done if the request is complete
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesteremail") @RequestParam String requesteremail,
      @Parameter(name = "professoremail") @RequestParam String professoremail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(
              name = "daterequested",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("daterequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime daterequested,
      @Parameter(
              name = "dateneeded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateneeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateneeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateRequested={}", daterequested);
    log.info("dateNeeded={}", dateneeded);

    RecommendationRequest recRequest = new RecommendationRequest();
    recRequest.setRequesteremail(requesteremail);
    recRequest.setProfessoremail(professoremail);
    recRequest.setExplanation(explanation);
    recRequest.setDaterequested(daterequested);
    recRequest.setDateneeded(dateneeded);
    recRequest.setDone(done);

    RecommendationRequest savedRecRequest = recommendationRequestRepository.save(recRequest);

    return savedRecRequest;
  }

  /**
   * Delete a RecommendationRequest
   *
   * @param id the id of the request to delete
   * @return a message indicating the request was deleted
   */
  @Operation(summary = "Delete a recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recommendationRequestRepository.delete(recRequest);
    return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
  }

  /**
   * Update a single recommendation request
   *
   * @param id id of the request to update
   * @param incoming the new request
   * @return the updated request object
   */
  @Operation(summary = "Update a single recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid RecommendationRequest incoming) {

    RecommendationRequest recRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recRequest.setRequesteremail(incoming.getRequesteremail());
    recRequest.setProfessoremail(incoming.getProfessoremail());
    recRequest.setExplanation(incoming.getExplanation());
    recRequest.setDaterequested(incoming.getDaterequested());
    recRequest.setDateneeded(incoming.getDateneeded());
    recRequest.setDone(incoming.getDone());

    recommendationRequestRepository.save(recRequest);

    return recRequest;
  }
}
