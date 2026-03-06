package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.request.CategoryPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryRequestDTO;
import br.com.geac.backend.aplication.dtos.request.EventPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.EventRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationRequestDTO;
import br.com.geac.backend.aplication.dtos.request.OrganizerRequestDTO;
import br.com.geac.backend.aplication.dtos.request.QualificationRequestDTO;
import br.com.geac.backend.aplication.dtos.request.RegisterRequestDTO;
import br.com.geac.backend.aplication.dtos.request.RequirementRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerRequestDTO;
import br.com.geac.backend.aplication.dtos.request.TagRequestDTO;
import br.com.geac.backend.aplication.dtos.response.EventResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RegisterResponseDTO;
import br.com.geac.backend.aplication.dtos.response.UserRegistrationContextResponseDTO;
import br.com.geac.backend.domain.entities.Category;
import br.com.geac.backend.domain.entities.Evaluation;
import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.EventRequirement;
import br.com.geac.backend.domain.entities.EventStatistics;
import br.com.geac.backend.domain.entities.Location;
import br.com.geac.backend.domain.entities.Notification;
import br.com.geac.backend.domain.entities.OrganizationEngagement;
import br.com.geac.backend.domain.entities.Organizer;
import br.com.geac.backend.domain.entities.Qualification;
import br.com.geac.backend.domain.entities.Registration;
import br.com.geac.backend.domain.entities.Speaker;
import br.com.geac.backend.domain.entities.StudentExtracurricularHours;
import br.com.geac.backend.domain.entities.Tag;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Campus;
import br.com.geac.backend.domain.enums.DaysBeforeNotify;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MapperCoverageTest {

    @Test
    @DisplayName("EventMapper deve mapear e atualizar caminhos nulos e nao nulos")
    void eventMapper_Coverage() {
        EventMapperImpl mapper = new EventMapperImpl();
        setField(mapper, "locationMapper", new LocationMapperImpl());

        assertThat(mapper.toResponseDTO(null, null)).isNull();
        assertThat(mapper.toResponseDTO(null, null, null)).isNull();
        assertThat(mapper.mapTags(null)).isEmpty();
        assertThat(mapper.toRequirementDTO(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();

        Category category = new Category();
        category.setId(1);
        category.setName("Category");

        Organizer organizer = new Organizer();
        organizer.setId(UUID.randomUUID());
        organizer.setName("Org");
        organizer.setContactEmail("org@test.com");

        Location location = new Location();
        location.setId(1);
        location.setName("Room A");
        location.setStreet("Street");
        location.setNumber("10");
        location.setNeighborhood("N");
        location.setCity("City");
        location.setState("PE");
        location.setZipCode("55000-000");
        location.setCampus(Campus.CAMPUS_RECIFE_CENTRAL);
        location.setCapacity(30);

        EventRequirement req = new EventRequirement();
        req.setId(2);
        req.setDescription("Notebook");
        Tag tag = new Tag();
        tag.setId(3);
        tag.setName("Tech");
        Speaker speaker = new Speaker();
        speaker.setName("Speaker 1");

        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setCategory(category);
        event.setOrganizer(organizer);
        event.setLocation(location);
        event.setTitle("Event");
        event.setDescription("Description");
        event.setStatus(EventStatus.ACTIVE);
        event.setStartTime(LocalDateTime.now());
        event.setEndTime(LocalDateTime.now().plusHours(1));
        event.setWorkloadHours(2);
        event.setMaxCapacity(50);
        event.setCreatedAt(LocalDateTime.now());
        event.setRequirements(Set.of(req));
        event.setTags(Set.of(tag));
        event.setSpeakers(Set.of(speaker));
        event.setDaysBeforeNotify(DaysBeforeNotify.ONE_DAY_BEFORE);

        UserRegistrationContextResponseDTO context = new UserRegistrationContextResponseDTO(true, "CONFIRMED", true);
        assertThat(mapper.toResponseDTO(null, context).isRegistered()).isTrue();
        assertThat(mapper.toResponseDTO(event, null).title()).isEqualTo("Event");
        EventResponseDTO dto = mapper.toResponseDTO(event, context, 7);
        assertThat(dto.categoryId()).isEqualTo(1);
        assertThat(dto.organizerName()).isEqualTo("Org");
        assertThat(dto.registeredCount()).isEqualTo(7);
        assertThat(dto.speakers()).contains("Speaker 1");
        assertThat(mapper.mapTags(Set.of(tag))).contains("Tech");

        Event partial = new Event();
        partial.setTitle("Only title");
        partial.setSpeakers(null);
        partial.setStatus(null);
        partial.setRequirements(null);
        partial.setTags(null);
        EventResponseDTO partialDto = mapper.toResponseDTO(partial, null, null);
        assertThat(partialDto.registeredCount()).isEqualTo(0);
        assertThat(mapper.mapSpeakers(partial)).isEmpty();

        EventRequestDTO withDefaultNotify = new EventRequestDTO(
                "Title",
                "Desc",
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2,
                10,
                1,
                Set.of(1),
                Set.of(1),
                null,
                Set.of(1),
                UUID.randomUUID(),
                null
        );
        Event createdDefault = mapper.toEntity(withDefaultNotify);
        assertThat(createdDefault.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(createdDefault.getDaysBeforeNotify()).isEqualTo(DaysBeforeNotify.ONE_DAY_BEFORE);

        EventRequestDTO withExplicitNotify = new EventRequestDTO(
                "Title",
                "Desc",
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2,
                10,
                1,
                Set.of(1),
                Set.of(1),
                null,
                Set.of(1),
                UUID.randomUUID(),
                DaysBeforeNotify.ONE_WEEK_BEFORE
        );
        Event createdExplicit = mapper.toEntity(withExplicitNotify);
        assertThat(createdExplicit.getDaysBeforeNotify()).isEqualTo(DaysBeforeNotify.ONE_WEEK_BEFORE);

        Event toUpdate = new Event();
        toUpdate.setTitle("Old");
        toUpdate.setDescription("Old");
        toUpdate.setWorkloadHours(1);

        EventPatchRequestDTO patchFull = new EventPatchRequestDTO(
                "New",
                "New Desc",
                "https://example.com",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(3),
                4,
                100,
                1,
                Set.of(1),
                Set.of(1),
                1,
                Set.of(1),
                UUID.randomUUID(),
                DaysBeforeNotify.ONE_WEEK_BEFORE
        );
        mapper.updateEventFromDto(patchFull, toUpdate);
        assertThat(toUpdate.getTitle()).isEqualTo("New");
        assertThat(toUpdate.getDaysBeforeNotify()).isEqualTo(DaysBeforeNotify.ONE_WEEK_BEFORE);

        EventPatchRequestDTO patchNulls = new EventPatchRequestDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        mapper.updateEventFromDto(patchNulls, toUpdate);
        mapper.updateEventFromDto(null, toUpdate);
        assertThat(toUpdate.getTitle()).isEqualTo("New");
    }

    @Test
    @DisplayName("LocationMapper deve mapear e aplicar patch em todos os caminhos")
    void locationMapper_Coverage() {
        LocationMapperImpl mapper = new LocationMapperImpl();

        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();

        LocationRequestDTO dto = new LocationRequestDTO(
                "Room",
                "Street",
                "10",
                "Neighborhood",
                "City",
                "PE",
                "55000-000",
                "Near",
                40,
                Campus.CAMPUS_SURUBIM_CENTRAL
        );
        Location entity = mapper.toEntity(dto);
        assertThat(entity.getName()).isEqualTo("Room");

        LocationPatchRequestDTO patch = new LocationPatchRequestDTO(
                "Lab",
                "Another",
                "20",
                "Bairro",
                "Recife",
                "PE",
                "50000-000",
                "Ref",
                100,
                Campus.CAMPUS_RECIFE_SUL
        );
        mapper.updateEntityFromDTO(patch, entity);
        assertThat(entity.getName()).isEqualTo("Lab");
        assertThat(entity.getCapacity()).isEqualTo(100);

        LocationPatchRequestDTO patchNull = new LocationPatchRequestDTO(
                null, null, null, null, null, null, null, null, null, null
        );
        mapper.updateEntityFromDTO(patchNull, entity);
        mapper.updateEntityFromDTO(null, entity);
        assertThat(entity.getName()).isEqualTo("Lab");
    }

    @Test
    @DisplayName("EvaluationMapper deve mapear valores aninhados e valores aninhados nulos")
    void evaluationMapper_Coverage() {
        EvaluationMapperImpl mapper = new EvaluationMapperImpl();
        assertThat(mapper.toDTO(null)).isNull();

        Evaluation evalNoRegistration = new Evaluation();
        evalNoRegistration.setId(1L);
        evalNoRegistration.setComment("Comment");
        assertThat(mapper.toDTO(evalNoRegistration).registrationId()).isNull();

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("User");
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Event");
        Registration registration = new Registration();
        registration.setId(UUID.randomUUID());
        registration.setUser(user);
        registration.setEvent(event);

        Evaluation eval = new Evaluation();
        eval.setId(2L);
        eval.setRegistration(registration);
        eval.setRating(5);
        eval.setComment("Great");
        eval.setCreatedAt(LocalDateTime.now());

        assertThat(mapper.toDTO(eval).eventTitle()).isEqualTo("Event");

        registration.setEvent(null);
        registration.setUser(null);
        assertThat(mapper.toDTO(eval).eventId()).isNull();
        assertThat(mapper.toDTO(eval).userId()).isNull();
    }

    @Test
    @DisplayName("SpeakerMapper deve mapear com qualification mapper e aplicar patch nos campos")
    void speakerMapper_Coverage() {
        SpeakerMapperImpl mapper = new SpeakerMapperImpl();
        setField(mapper, "qualificationMapper", new QualificationMapperImpl());

        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();

        SpeakerRequestDTO requestDTO = new SpeakerRequestDTO(
                "Speaker",
                "Bio text",
                Set.of(new QualificationRequestDTO("MSc", "UFAPE")),
                "speaker@test.com"
        );
        Speaker entity = mapper.toEntity(requestDTO);
        assertThat(entity.getName()).isEqualTo("Speaker");

        Qualification q = new Qualification();
        q.setId(1);
        q.setTitleName("MSc");
        q.setInstitution("UFAPE");
        Speaker speaker = new Speaker();
        speaker.setId(10);
        speaker.setName("Speaker");
        speaker.setBio("Bio");
        speaker.setEmail("speaker@test.com");
        speaker.setQualifications(Set.of(q));
        assertThat(mapper.toDto(speaker).qualifications()).hasSize(1);

        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO("New Name", "New Bio", "new@test.com", Set.of());
        mapper.updateEntityFromDTO(patch, speaker);
        assertThat(speaker.getName()).isEqualTo("New Name");
        assertThat(speaker.getEmail()).isEqualTo("new@test.com");

        SpeakerPatchRequestDTO patchNull = new SpeakerPatchRequestDTO(null, null, null, null);
        mapper.updateEntityFromDTO(patchNull, speaker);
        mapper.updateEntityFromDTO(null, speaker);
        assertThat(speaker.getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("CategoryMapper deve mapear e aplicar patch")
    void categoryMapper_Coverage() {
        CategoryMapperImpl mapper = new CategoryMapperImpl();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();

        Category category = mapper.toEntity(new CategoryRequestDTO("Name", "Description 10"));
        assertThat(category.getName()).isEqualTo("Name");

        mapper.updateEntityFromDto(new CategoryPatchRequestDTO("New", "Another desc"), category);
        assertThat(category.getName()).isEqualTo("New");
        mapper.updateEntityFromDto(new CategoryPatchRequestDTO(null, null), category);
        mapper.updateEntityFromDto(null, category);
        assertThat(category.getDescription()).isEqualTo("Another desc");
    }

    @Test
    @DisplayName("UserMapper deve mapear registro e conversao de papel")
    void userMapper_Coverage() {
        UserMapperImpl mapper = new UserMapperImpl();
        assertThat(mapper.registerToUser(null)).isNull();
        assertThat(mapper.userToRegisterResponse(null)).isNull();
        assertThat(mapper.mapStringToRole(null)).isNull();
        assertThat(mapper.mapStringToRole("student")).isEqualTo(Role.STUDENT);

        RegisterRequestDTO request = new RegisterRequestDTO("User", "user@test.com", "123456", "organizer");
        User user = mapper.registerToUser(request);
        assertThat(user.getRole()).isEqualTo(Role.ORGANIZER);

        RegisterResponseDTO response = mapper.userToRegisterResponse(user);
        assertThat(response.message()).isEqualTo("User registered successfully");
    }

    @Test
    @DisplayName("Mappers simples devem cobrir ramos nulos e nao nulos")
    void simpleMappers_Coverage() {
        TagMapperImpl tagMapper = new TagMapperImpl();
        OrganizerMapperImpl organizerMapper = new OrganizerMapperImpl();
        RequirementMapperImpl requirementMapper = new RequirementMapperImpl();
        QualificationMapperImpl qualificationMapper = new QualificationMapperImpl();
        NotificationMapperImpl notificationMapper = new NotificationMapperImpl();
        OrganizationEngagementMapperImpl orgEngagementMapper = new OrganizationEngagementMapperImpl();
        StudentHoursMapperImpl studentHoursMapper = new StudentHoursMapperImpl();
        EventStatisticsMapperImpl eventStatisticsMapper = new EventStatisticsMapperImpl();

        assertThat(tagMapper.toEntity(null)).isNull();
        assertThat(tagMapper.toDTO(null)).isNull();
        Tag tag = tagMapper.toEntity(new TagRequestDTO("Tag 1"));
        assertThat(tag.getName()).isEqualTo("Tag 1");
        assertThat(tagMapper.toDTO(tag).name()).isEqualTo("Tag 1");

        assertThat(organizerMapper.toEntity(null)).isNull();
        assertThat(organizerMapper.toResponseDTO(null)).isNull();
        Organizer organizer = organizerMapper.toEntity(new OrganizerRequestDTO("Org", "org@test.com"));
        assertThat(organizer.getName()).isEqualTo("Org");

        assertThat(requirementMapper.toEntity(null)).isNull();
        assertThat(requirementMapper.toDTO(null)).isNull();
        EventRequirement req = requirementMapper.toEntity(new RequirementRequestDTO("Need notebook"));
        assertThat(req.getDescription()).isEqualTo("Need notebook");

        assertThat(qualificationMapper.toEntity(null)).isNull();
        assertThat(qualificationMapper.toDTO(null)).isNull();
        Qualification q = qualificationMapper.toEntity(new QualificationRequestDTO("MSc", "UFAPE"));
        assertThat(q.getTitleName()).isEqualTo("MSc");

        assertThat(notificationMapper.toDTO(null)).isNull();
        Notification notification = new Notification();
        notification.setId(1);
        notification.setType("INFO");
        assertThat(notificationMapper.toDTO(notification).type()).isEqualTo("INFO");

        assertThat(orgEngagementMapper.toResponseDTO(null)).isNull();
        OrganizationEngagement orgEngagement = new OrganizationEngagement();
        setField(orgEngagement, "organizerId", UUID.randomUUID());
        setField(orgEngagement, "organizerName", "Org");
        setField(orgEngagement, "totalEventosRealizados", 10L);
        setField(orgEngagement, "totalParticipantesEngajados", 100L);
        assertThat(orgEngagementMapper.toResponseDTO(orgEngagement).organizerName()).isEqualTo("Org");

        assertThat(studentHoursMapper.toResponseDTO(null)).isNull();
        StudentExtracurricularHours hours = new StudentExtracurricularHours();
        setField(hours, "studentId", UUID.randomUUID());
        setField(hours, "studentName", "Student");
        setField(hours, "studentEmail", "s@test.com");
        setField(hours, "totalCertificadosEmitidos", 2L);
        setField(hours, "totalHorasAcumuladas", 20L);
        assertThat(studentHoursMapper.toResponseDTO(hours).studentName()).isEqualTo("Student");

        assertThat(eventStatisticsMapper.toResponseDTO(null)).isNull();
        EventStatistics stats = new EventStatistics();
        setField(stats, "eventId", UUID.randomUUID());
        setField(stats, "eventTitle", "Event");
        setField(stats, "eventStatus", "ACTIVE");
        setField(stats, "totalInscritos", 15);
        setField(stats, "totalPresentes", 10);
        setField(stats, "mediaAvaliacao", 4.5d);
        assertThat(eventStatisticsMapper.toResponseDTO(stats).totalInscritos()).isEqualTo(15L);

        EventStatistics statsNullCounts = new EventStatistics();
        setField(statsNullCounts, "eventId", UUID.randomUUID());
        assertThat(eventStatisticsMapper.toResponseDTO(statsNullCounts).totalInscritos()).isNull();
    }

    @Test
    @DisplayName("Interfaces MapStruct com metodos default devem ser cobertas")
    void mapperInterfaceDefaultMethods_Coverage() {
        UserMapperImpl mapper = new UserMapperImpl();
        assertThatCode(() -> mapper.mapStringToRole("ADMIN")).doesNotThrowAnyException();
        assertThat(mapper.mapStringToRole("ADMIN")).isEqualTo(Role.ADMIN);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalArgumentException("Field not found: " + name);
    }
}

