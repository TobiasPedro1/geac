package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.EventStatisticsResponseDTO;
import br.com.geac.backend.aplication.dtos.response.OrganizationEngagementResponseDTO;
import br.com.geac.backend.aplication.services.EventStatisticsService;
import br.com.geac.backend.aplication.services.OrganizationEngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/views")
@RequiredArgsConstructor
public class ViewController {

    private final EventStatisticsService eventStatisticsService;
    private final OrganizationEngagementService organizationEngagementService;

    @GetMapping("/eventstatistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventStatisticsResponseDTO>> getAllEventStatistics() {
        return ResponseEntity.ok(eventStatisticsService.getAllEventStatistics());
    }

    @GetMapping("/organization-engagement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrganizationEngagementResponseDTO>> getAllOrganizationEngagement() {
        return ResponseEntity.ok(organizationEngagementService.getAllOrganizationEngagement());
    }
}