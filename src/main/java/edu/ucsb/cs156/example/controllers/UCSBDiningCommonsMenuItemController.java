package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
// import edu.ucsb.cs156.example.errors.EntityNotFoundException;
// import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.validation.Valid;
// import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDates */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB dates
   *
   * @return an iterable of UCSBDate
   */
  @Operation(summary = "List all ucsb dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> diningCommonsMenuItems =
        ucsbDiningCommonsMenuItemRepository.findAll();
    return diningCommonsMenuItems;
  }

  /**
   * Create a new date
   *
   * @param diningCommonsCode dining commons code
   * @param name the name of the diningcommonsmenuitem
   * @param station the station for the item
   * @return the saved ucsbdate
   */
  @Operation(summary = "Create a new UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningcommonscode") @RequestParam String diningcommonscode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    // log.info("localDateTime={}", localDateTime);

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = new UCSBDiningCommonsMenuItem();
    ucsbDiningCommonsMenuItem.setDiningcommonscode(diningcommonscode);
    ucsbDiningCommonsMenuItem.setName(name);
    ucsbDiningCommonsMenuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedUcsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

    return savedUcsbDiningCommonsMenuItem;
  }
}
