import { cookies } from "next/headers";
import { Event } from "@/types/event";
import { mockEvents } from "@/data/mockData";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * Mapeia o DTO retornado pelo backend para o tipo Event do frontend.
 * Campos que o backend não possui recebem valores padrão.
 */
function mapBackendToEvent(dto: any): Event {
  // Extrai a data (YYYY-MM-DD) do startTime (ex: "2026-03-15T14:00:00")
  const startDate = dto.startTime ? dto.startTime.split("T")[0] : "";
  const startHour = dto.startTime ? dto.startTime.split("T")[1]?.substring(0, 5) : "";
  const endHour = dto.endTime ? dto.endTime.split("T")[1]?.substring(0, 5) : "";

  // Mapeamento reverso de categoryId para nome
  const categoryNames: Record<number, string> = {
    1: "palestra",
    2: "seminario",
    3: "workshop",
    4: "cultural",
    5: "feira",
    6: "livre",
    7: "conferencia",
    8: "festival",
    9: "outro",
  };

  const campusNames: Record<number, string> = {
    1: "reitoria",
    2: "ondina",
    3: "sao lazaro",
    4: "canela",
    5: "graca",
    6: "federacao",
  };

  return {
    id: String(dto.id),
    title: dto.title ?? "",
    description: dto.description ?? "",
    category: (categoryNames[dto.categoryId] ?? "outro") as Event["category"],
    date: startDate,
    startTime: startHour,
    endTime: endHour,
    location: campusNames[dto.locationId] ?? "Campus",
    campus: (campusNames[dto.locationId] ?? "ondina") as Event["campus"],
    speakers: [],
    capacity: dto.maxCapacity ?? 0,
    registered: 0,
    requirements: [],
    organizer: dto.organizerName ?? "",
    organizerType: "Professor",
    tags: [],
    isRegistered: false,
  };
}

/**
 * Serviço server-only — usar apenas em Server Components e Route Handlers.
 * Para funções client-side, use eventService.client.ts
 */
export const eventService = {
  getAllEvents: async (): Promise<Event[]> => {
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    const res = await fetch(`${API_URL}/events/`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      cache: "no-store",
    });

    if (!res.ok) {
      console.warn("Falha ao buscar eventos. Retornando dados mockados.");
      return mockEvents;
    }

    const data: any[] = await res.json();
    return data.map(mapBackendToEvent);
  },

  getEventById: async (id: string): Promise<Event> => {
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    const res = await fetch(`${API_URL}/events/${id}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      cache: "no-store",
    });

    if (!res.ok) {
      const mockEvent = mockEvents.find((event) => event.id === id);
      if (mockEvent) {
        console.warn(`Falha ao buscar evento ${id}. Retornando dados mockados.`);
        return mockEvent;
      }
      throw new Error("Evento não encontrado");
    }

    const data = await res.json();
    return mapBackendToEvent(data);
  },
};
