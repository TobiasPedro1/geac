import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import OrganizerEventFeedbackContent from "../OrganizerEventFeedbackContent";
import { OrganizerEventFeedbackResponseDTO } from "@/types/evaluations";

const feedbackData: OrganizerEventFeedbackResponseDTO = {
  eventId: "event-1",
  eventTitle: "Semana de Engenharia de Software",
  averageRating: 4.5,
  totalFeedbacks: 2,
  feedbacks: [
    {
      id: 1,
      registrationId: "reg-1",
      eventId: "event-1",
      eventTitle: "Semana de Engenharia de Software",
      userId: "user-1",
      userName: "Ana",
      rating: 5,
      comment: "Organização excelente e palestras muito relevantes.",
      createdAt: "2026-03-10T10:00:00",
    },
    {
      id: 2,
      registrationId: "reg-2",
      eventId: "event-1",
      eventTitle: "Semana de Engenharia de Software",
      userId: "user-2",
      userName: "Bruno",
      rating: 4,
      comment: "Gostei do conteúdo, mas o cronograma poderia ser mais claro.",
      createdAt: "2026-03-11T11:00:00",
    },
    {
      id: 3,
      registrationId: "reg-3",
      eventId: "event-1",
      eventTitle: "Semana de Engenharia de Software",
      userId: "user-3",
      userName: "Carla",
      rating: 2,
      comment: "O local estava lotado e o áudio ficou baixo.",
      createdAt: "2026-03-12T09:00:00",
    },
  ],
};

describe("OrganizerEventFeedbackContent", () => {
  it("deve renderizar o resumo e os comentários dos participantes", () => {
    render(<OrganizerEventFeedbackContent feedbackData={feedbackData} />);

    expect(screen.getByText("Feedbacks recebidos")).toBeInTheDocument();
    expect(screen.getByText("Média das avaliações")).toBeInTheDocument();
    expect(
      screen.getByText("Semana de Engenharia de Software"),
    ).toBeInTheDocument();
    expect(
      screen.getByText("Organização excelente e palestras muito relevantes.", {
        exact: false,
      }),
    ).toBeInTheDocument();
    expect(
      screen.getByText(
        "Gostei do conteúdo, mas o cronograma poderia ser mais claro.",
        {
          exact: false,
        },
      ),
    ).toBeInTheDocument();
  });

  it("deve filtrar os feedbacks por nota", async () => {
    const user = userEvent.setup();

    render(<OrganizerEventFeedbackContent feedbackData={feedbackData} />);

    await user.selectOptions(
      screen.getByLabelText("Filtrar feedbacks por nota"),
      "5",
    );

    expect(
      screen.getByText("Organização excelente e palestras muito relevantes.", {
        exact: false,
      }),
    ).toBeInTheDocument();
    expect(
      screen.queryByText(
        "Gostei do conteúdo, mas o cronograma poderia ser mais claro.",
        {
          exact: false,
        },
      ),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText("O local estava lotado e o áudio ficou baixo.", {
        exact: false,
      }),
    ).not.toBeInTheDocument();
  });

  it("deve ordenar os feedbacks pela maior nota", async () => {
    const user = userEvent.setup();

    render(<OrganizerEventFeedbackContent feedbackData={feedbackData} />);

    await user.selectOptions(screen.getByLabelText("Ordenar feedbacks"), [
      "rating_desc",
    ]);

    const participantNames = screen.getAllByText(/Ana|Bruno|Carla/);
    expect(participantNames[0]).toHaveTextContent("Ana");
    expect(participantNames[1]).toHaveTextContent("Bruno");
    expect(participantNames[2]).toHaveTextContent("Carla");
  });
});
