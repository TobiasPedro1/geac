package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.MemberResponseDTO;
import br.com.geac.backend.aplication.dtos.request.AddMemberRequestDTO;
import br.com.geac.backend.aplication.services.OrganizerMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizers/{organizerId}/members")
@RequiredArgsConstructor
public class OrganizerMemberController {

    private final OrganizerMemberService memberService;

    @PostMapping
    public ResponseEntity<Void> addMember(
            @PathVariable UUID organizerId,
            @RequestBody @Valid AddMemberRequestDTO dto) {
        memberService.addMember(organizerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getMembers(
            @PathVariable UUID organizerId) {
        return ResponseEntity.ok(memberService.getMembersByOrganizerId(organizerId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID organizerId,
            @PathVariable UUID userId) {
        memberService.removeMember(organizerId, userId);
        return ResponseEntity.noContent().build();
    }

}