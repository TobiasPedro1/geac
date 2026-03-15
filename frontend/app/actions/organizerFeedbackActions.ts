"use server";

import { cookies } from "next/headers";
import { API_URL } from "./configs";
import { OrganizerEventFeedbackResponseDTO } from "@/types/evaluations";

interface OrganizerFeedbackActionResult {
  data: OrganizerEventFeedbackResponseDTO | null;
  error?: string;
  status?: number;
}

export async function getOrganizerEventFeedbackAction(
  eventId: string,
): Promise<OrganizerFeedbackActionResult> {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    return {
      data: null,
      error: "Não autorizado. Faça login novamente.",
      status: 401,
    };
  }

  try {
    const response = await fetch(`${API_URL}/evaluation/event/${eventId}/organizer`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      cache: "no-store",
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      return {
        data: null,
        error:
          errorData?.message ||
          "Não foi possível carregar os feedbacks deste evento.",
        status: response.status,
      };
    }

    return {
      data: (await response.json()) as OrganizerEventFeedbackResponseDTO,
      status: response.status,
    };
  } catch (error) {
    console.error("Erro ao buscar feedbacks do organizador:", error);
    return {
      data: null,
      error: "Erro de conexão com o servidor.",
      status: 500,
    };
  }
}
