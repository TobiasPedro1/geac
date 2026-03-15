import { render, screen } from "@testing-library/react";
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
});
