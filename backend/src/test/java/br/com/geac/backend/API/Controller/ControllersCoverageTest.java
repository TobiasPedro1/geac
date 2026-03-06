package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.request.AddMemberRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CreateOrganizerRequestDTO;
import br.com.geac.backend.aplication.dtos.request.EvaluationRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationRequestDTO;
import br.com.geac.backend.aplication.dtos.request.OrganizerRequestDTO;
import br.com.geac.backend.aplication.dtos.request.PresenceRequestDTO;
import br.com.geac.backend.aplication.dtos.request.RequirementRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerRequestDTO;
import br.com.geac.backend.aplication.dtos.request.StudentHoursResponseDTO;
import br.com.geac.backend.aplication.dtos.request.TagRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryRequestDTO;
import br.com.geac.backend.aplication.dtos.response.CategoryResponseDTO;
import br.com.geac.backend.aplication.dtos.response.EvaluationResponseDTO;
import br.com.geac.backend.aplication.dtos.response.LocationResponseDTO;
import br.com.geac.backend.aplication.dtos.response.MemberResponseDTO;
import br.com.geac.backend.aplication.dtos.response.NotificationResponseDTO;
import br.com.geac.backend.aplication.dtos.response.OrganizerResponseDTO;
import br.com.geac.backend.aplication.dtos.response.PendingRequestResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RegistrationResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RequirementsResponseDTO;
import br.com.geac.backend.aplication.dtos.response.SpeakerResponseDTO;
import br.com.geac.backend.aplication.dtos.response.TagResponseDTO;
import br.com.geac.backend.aplication.services.CategoryService;
import br.com.geac.backend.aplication.services.EvaluationService;
import br.com.geac.backend.aplication.services.ExtracurricularHoursService;
import br.com.geac.backend.aplication.services.LocationService;
import br.com.geac.backend.aplication.services.NotificationService;
import br.com.geac.backend.aplication.services.OrganizerMemberService;
import br.com.geac.backend.aplication.services.OrganizerRequestService;
import br.com.geac.backend.aplication.services.OrganizerService;
import br.com.geac.backend.aplication.services.RegistrationService;
import br.com.geac.backend.aplication.services.RequirementService;
import br.com.geac.backend.aplication.services.SpeakerService;
import br.com.geac.backend.aplication.services.TagService;
import br.com.geac.backend.domain.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllersCoverageTest {

    @Test
    @DisplayName("Deve cobrir fluxos principais do CategoryController")
    void deveCobrirCategoryController() {
        CategoryService service = mock(CategoryService.class);
        CategoryController controller = new CategoryController(service);
        CategoryResponseDTO response = mock(CategoryResponseDTO.class);

        when(service.createCategory((CategoryRequestDTO) null)).thenReturn(response);
        when(service.getCategoryById(1)).thenReturn(response);
        when(service.getAllCategory()).thenReturn(List.of(response));
        when(service.updateCategory(1, null)).thenReturn(response);

        assertThat(controller.createCategory(null).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.createCategory(null).getBody()).isSameAs(response);
        assertThat(controller.getById(1).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.getAll().getBody()).containsExactly(response);
        assertThat(controller.updateCategory(null, 1).getBody()).isSameAs(response);
        assertThat(controller.delete(1).getStatusCode().value()).isEqualTo(204);

        verify(service).deleteCategory(1);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do LocationController")
    void deveCobrirLocationController() {
        LocationService service = mock(LocationService.class);
        LocationController controller = new LocationController(service);
        LocationResponseDTO response = mock(LocationResponseDTO.class);

        when(service.createLocation((LocationRequestDTO) null)).thenReturn(response);
        when(service.getById(2)).thenReturn(response);
        when(service.getAll()).thenReturn(List.of(response));
        when(service.updateLocation(2, null)).thenReturn(response);

        assertThat(controller.createLocation(null).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getById(2).getBody()).isSameAs(response);
        assertThat(controller.getAll().getBody()).containsExactly(response);
        assertThat(controller.updateLocation(2, (LocationPatchRequestDTO) null).getBody()).isSameAs(response);
        assertThat(controller.delete(2).getStatusCode().value()).isEqualTo(204);

        verify(service).deleteLocation(2);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do RequirementController")
    void deveCobrirRequirementController() {
        RequirementService service = mock(RequirementService.class);
        RequirementController controller = new RequirementController(service);
        RequirementsResponseDTO response = mock(RequirementsResponseDTO.class);

        when(service.createRequirement((RequirementRequestDTO) null)).thenReturn(response);
        when(service.getById(3)).thenReturn(response);
        when(service.getAll()).thenReturn(List.of(response));
        when(service.updateRequirement(3, null)).thenReturn(response);

        assertThat(controller.createRequirement(null).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getById(3).getBody()).isSameAs(response);
        assertThat(controller.getAll().getBody()).containsExactly(response);
        assertThat(controller.updateRequirement(null, 3).getBody()).isSameAs(response);
        assertThat(controller.delete(3).getStatusCode().value()).isEqualTo(204);

        verify(service).deleteRequirement(3);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do TagController")
    void deveCobrirTagController() {
        TagService service = mock(TagService.class);
        TagController controller = new TagController(service);
        TagResponseDTO response = mock(TagResponseDTO.class);

        when(service.createTag((TagRequestDTO) null)).thenReturn(response);
        when(service.getById(4)).thenReturn(response);
        when(service.getAll()).thenReturn(List.of(response));
        when(service.updateSpeaker(4, null)).thenReturn(response);

        assertThat(controller.createTag(null).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getById(4).getBody()).isSameAs(response);
        assertThat(controller.getAll().getBody()).containsExactly(response);
        assertThat(controller.updateTag(4, null).getBody()).isSameAs(response);
        assertThat(controller.delete(4).getStatusCode().value()).isEqualTo(204);

        verify(service).deleteTag(4);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do OrganizerController")
    void deveCobrirOrganizerController() {
        OrganizerService service = mock(OrganizerService.class);
        OrganizerController controller = new OrganizerController(service);
        OrganizerResponseDTO response = mock(OrganizerResponseDTO.class);
        UUID id = UUID.randomUUID();

        when(service.createOrganizer((OrganizerRequestDTO) null)).thenReturn(response);
        when(service.getAllOrganizers()).thenReturn(List.of(response));
        when(service.getOrganizerById(id)).thenReturn(response);
        when(service.updateOrganizer(id, null)).thenReturn(response);
        when(service.getAllUserOrganizer(id)).thenReturn(List.of(response));

        assertThat(controller.createOrganizer(null).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getAllOrganizers().getBody()).containsExactly(response);
        assertThat(controller.getOrganizerById(id).getBody()).isSameAs(response);
        assertThat(controller.updateOrganizer(id, null).getBody()).isSameAs(response);
        assertThat(controller.deleteOrganizer(id).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.getUserOrganizers(id).getBody()).containsExactly(response);

        verify(service).deleteOrganizer(id);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do OrganizerMemberController")
    void deveCobrirOrganizerMemberController() {
        OrganizerMemberService service = mock(OrganizerMemberService.class);
        OrganizerMemberController controller = new OrganizerMemberController(service);
        MemberResponseDTO member = mock(MemberResponseDTO.class);
        UUID organizerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AddMemberRequestDTO addMemberRequestDTO = mock(AddMemberRequestDTO.class);

        when(service.getMembersByOrganizerId(organizerId)).thenReturn(List.of(member));

        assertThat(controller.addMember(organizerId, addMemberRequestDTO).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getMembers(organizerId).getBody()).containsExactly(member);
        assertThat(controller.removeMember(organizerId, userId).getStatusCode().value()).isEqualTo(204);

        verify(service).addMember(organizerId, addMemberRequestDTO);
        verify(service).removeMember(organizerId, userId);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do OrganizerRequestController")
    void deveCobrirOrganizerRequestController() {
        OrganizerRequestService service = mock(OrganizerRequestService.class);
        OrganizerRequestController controller = new OrganizerRequestController(service);
        PendingRequestResponseDTO pending = mock(PendingRequestResponseDTO.class);
        CreateOrganizerRequestDTO createRequest = mock(CreateOrganizerRequestDTO.class);

        when(service.getPendingRequests()).thenReturn(List.of(pending));

        assertThat(controller.getPendingRequests().getBody()).containsExactly(pending);
        assertThat(controller.approveRequest(10).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.rejectRequest(11).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.createRequest(createRequest).getStatusCode().value()).isEqualTo(201);

        verify(service).approveRequest(10);
        verify(service).rejectRequest(11);
        verify(service).createRequest(createRequest);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do RegistrationController")
    void deveCobrirRegistrationController() {
        RegistrationService service = mock(RegistrationService.class);
        RegistrationController controller = new RegistrationController(service);
        RegistrationResponseDTO response = mock(RegistrationResponseDTO.class);
        PresenceRequestDTO presence = mock(PresenceRequestDTO.class);
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(presence.userIds()).thenReturn(List.of(userId));
        when(presence.attended()).thenReturn(true);
        when(service.getRegistrationsByEvent(eventId)).thenReturn(List.of(response));

        assertThat(controller.markAttendanceInBulk(eventId, presence).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.getRegistrationsByEvent(eventId).getBody()).containsExactly(response);
        assertThat(controller.registerToEvent(eventId).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.cancelRegistration(eventId).getStatusCode().value()).isEqualTo(204);

        verify(service).markAttendanceInBulk(eventId, List.of(userId), true);
        verify(service).registerToEvent(eventId);
        verify(service).cancelRegistration(eventId);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do SpeakerController")
    void deveCobrirSpeakerController() {
        SpeakerService service = mock(SpeakerService.class);
        SpeakerController controller = new SpeakerController(service);
        SpeakerResponseDTO response = mock(SpeakerResponseDTO.class);

        when(service.createSpeaker((SpeakerRequestDTO) null)).thenReturn(response);
        when(service.getById(5)).thenReturn(response);
        when(service.getAll()).thenReturn(List.of(response));
        when(service.updateSpeaker(5, null)).thenReturn(response);

        assertThat(controller.createSpeaker(null).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.getById(5).getBody()).isSameAs(response);
        assertThat(controller.getAll().getBody()).containsExactly(response);
        assertThat(controller.updateSpeaker(5, (SpeakerPatchRequestDTO) null).getBody()).isSameAs(response);
        assertThat(controller.delete(5).getStatusCode().value()).isEqualTo(204);

        verify(service).deleteSpeaker(5);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do NotificationController")
    void deveCobrirNotificationController() {
        NotificationService service = mock(NotificationService.class);
        NotificationController controller = new NotificationController(service);
        NotificationResponseDTO response = mock(NotificationResponseDTO.class);

        when(service.getUnreadNotifications()).thenReturn(List.of(response));
        when(service.getUnreadCount()).thenReturn(7);

        assertThat(controller.getUnreadNotifications().getBody()).containsExactly(response);
        assertThat(controller.getUnreadCount().getBody()).isEqualTo(7);
        assertThat(controller.markAsRead(99L).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.markAllAsRead().getStatusCode().value()).isEqualTo(204);

        verify(service).markAsRead(99L);
        verify(service).markAllAsRead();
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do EvaluationController")
    void deveCobrirEvaluationController() {
        EvaluationService service = mock(EvaluationService.class);
        EvaluationController controller = new EvaluationController(service);
        EvaluationRequestDTO request = mock(EvaluationRequestDTO.class);
        EvaluationResponseDTO response = mock(EvaluationResponseDTO.class);
        User user = mock(User.class);
        UUID eventId = UUID.randomUUID();

        when(service.createEvaluation(request, user)).thenReturn(response);
        when(service.getEventEvaluations(eventId)).thenReturn(List.of(response));

        assertThat(controller.save(request, user).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.save(request, user).getBody()).isSameAs(response);
        assertThat(controller.findAllByEvent(eventId).getBody()).containsExactly(response);
    }

    @Test
    @DisplayName("Deve cobrir fluxos principais do ExtracurricularHoursController")
    void deveCobrirExtracurricularHoursController() {
        ExtracurricularHoursService service = mock(ExtracurricularHoursService.class);
        ExtracurricularHoursController controller = new ExtracurricularHoursController(service);
        StudentHoursResponseDTO response = mock(StudentHoursResponseDTO.class);
        UUID studentId = UUID.randomUUID();

        when(service.getMyHours()).thenReturn(response);
        when(service.getHoursByStudentId(studentId)).thenReturn(response);
        when(service.getAllStudentHours()).thenReturn(List.of(response));

        assertThat(controller.getMyHours().getBody()).isSameAs(response);
        assertThat(controller.getStudentHours(studentId).getBody()).isSameAs(response);
        assertThat(controller.getAllStudentHours().getBody()).containsExactly(response);
    }
}
