"use client";

import { useState, useMemo, useCallback } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  deleteEventAction,
  getEventByIdAction,
} from "@/app/actions/eventActions";
import { EventResponseDTO } from "@/types/event";
import { OrganizerResponseDTO } from "@/types/organizer";
import EditEventModal from "./EditEventModal";
import DeleteConfirmModal from "./DeleteConfirmModal";
import {
  Plus,
  Search,
  Pencil,
  Trash2,
  Eye,
  MessageSquareText,
  Calendar,
  Users,
  MapPin,
  Filter,
  RefreshCw,
  CheckCircle,
  XCircle,
  Clock,
  AlertTriangle,
} from "lucide-react";

interface EventManageContentProps {
  initialEvents: EventResponseDTO[];
  categories: { id: number; name: string }[];
  locations: {
    id: number;
    name: string;
    street: string;
    number: string;
    neighborhood: string;
    city: string;
    state: string;
    zipCode: string;
    campus: string;
    referencePoint: string;
    capacity: number;
  }[];
  requirements: { id: number; description: string }[];
  tags: { id: number; name: string }[];
  speakers: { id: number; name: string }[];
  organizers: OrganizerResponseDTO[];
}

const statusConfig: Record<
  string,
  { label: string; className: string; icon: React.ReactNode }
> = {
  ACTIVE: {
    label: "Ativo",
    className:
      "bg-green-100 text-green-800 border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-800",
    icon: <CheckCircle className="w-3 h-3" />,
  },
  UPCOMING: {
    label: "Próximo",
    className:
      "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800",
    icon: <Clock className="w-3 h-3" />,
  },
  IN_PROGRESS: {
    label: "Em Andamento",
    className:
      "bg-yellow-100 text-yellow-800 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-300 dark:border-yellow-800",
    icon: <RefreshCw className="w-3 h-3" />,
  },
  COMPLETED: {
    label: "Finalizado",
    className:
      "bg-zinc-100 text-zinc-600 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:border-zinc-700",
    icon: <CheckCircle className="w-3 h-3" />,
  },
  CANCELLED: {
    label: "Cancelado",
    className:
      "bg-red-100 text-red-800 border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-800",
    icon: <XCircle className="w-3 h-3" />,
  },
};

export default function EventManageContent({
  initialEvents,
  categories,
  locations,
  requirements,
  tags,
  speakers,
  organizers,
}: Readonly<EventManageContentProps>) {
  const router = useRouter();
  const [events, setEvents] = useState<EventResponseDTO[]>(initialEvents);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Edit modal state
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<EventResponseDTO | null>(
    null,
  );
  const [loadingEventId, setLoadingEventId] = useState<string | null>(null);

  // Delete modal state
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deletingEvent, setDeletingEvent] = useState<{
    id: string;
    title: string;
  } | null>(null);

  // Success/Error messages
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const showSuccess = useCallback((msg: string) => {
    setSuccessMessage(msg);
    setTimeout(() => setSuccessMessage(""), 4000);
  }, []);

  const showError = useCallback((msg: string) => {
    setErrorMessage(msg);
    setTimeout(() => setErrorMessage(""), 5000);
  }, []);

  const filteredEvents = useMemo(() => {
    return events.filter((event) => {
      const searchLower = searchTerm.toLowerCase();
      const matchesSearch =
        searchTerm === "" ||
        event.title?.toLowerCase().includes(searchLower) ||
        event.description?.toLowerCase().includes(searchLower) ||
        event.organizerName?.toLowerCase().includes(searchLower);

      const matchesStatus =
        statusFilter === "" || event.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [events, searchTerm, statusFilter]);

  const handleRefresh = useCallback(async () => {
    setIsRefreshing(true);
    router.refresh();
    // Small delay to show the spinner
    setTimeout(() => setIsRefreshing(false), 1000);
  }, [router]);

  const handleEditClick = useCallback(
    async (eventId: string) => {
      setLoadingEventId(eventId);
      try {
        const eventData = await getEventByIdAction(eventId);
        if (eventData) {
          setEditingEvent(eventData);
          setEditModalOpen(true);
        } else {
          showError("Não foi possível carregar os dados do evento.");
        }
      } catch {
        showError("Erro ao carregar evento para edição.");
      } finally {
        setLoadingEventId(null);
      }
    },
    [showError],
  );

  const handleEditSuccess = useCallback(() => {
    showSuccess("Evento atualizado com sucesso!");
    router.refresh();
  }, [showSuccess, router]);

  const handleDeleteClick = useCallback(
    (eventId: string, eventTitle: string) => {
      setDeletingEvent({ id: eventId, title: eventTitle });
      setDeleteModalOpen(true);
    },
    [],
  );

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingEvent) return;

    try {
      const result = await deleteEventAction(deletingEvent.id);
      if (result.success) {
        setEvents((prev) => prev.filter((e) => e.id !== deletingEvent.id));
        showSuccess("Evento excluído com sucesso!");
        setDeleteModalOpen(false);
        setDeletingEvent(null);
      } else {
        showError(result.error || "Erro ao excluir evento.");
      }
    } catch {
      showError("Erro de conexão ao excluir evento.");
    }
  }, [deletingEvent, showSuccess, showError]);

  const formatDateTime = (dateStr: string | undefined) => {
    if (!dateStr) return "—";
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString("pt-BR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return dateStr;
    }
  };

  const getStatusBadge = (status: string) => {
    const config = statusConfig[status] || statusConfig.ACTIVE;
    return (
      <span
        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium border ${config.className}`}
      >
        {config.icon}
        {config.label}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Success/Error Messages */}
      {successMessage && (
        <div className="p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-xl flex items-center gap-3 animate-in fade-in slide-in-from-top-2 duration-300">
          <CheckCircle className="w-5 h-5 text-green-600 dark:text-green-400 flex-shrink-0" />
          <p className="text-sm font-medium text-green-800 dark:text-green-300">
            {successMessage}
          </p>
        </div>
      )}

      {errorMessage && (
        <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl flex items-center gap-3 animate-in fade-in slide-in-from-top-2 duration-300">
          <AlertTriangle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0" />
          <p className="text-sm font-medium text-red-800 dark:text-red-300">
            {errorMessage}
          </p>
        </div>
      )}

      {/* Toolbar */}
      <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm p-4">
        <div className="flex flex-col md:flex-row gap-4 items-start md:items-center justify-between">
          {/* Search & Filter */}
          <div className="flex flex-col sm:flex-row gap-3 flex-1 w-full md:w-auto">
            <div className="relative flex-1 min-w-[200px]">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400" />
              <input
                type="text"
                placeholder="Buscar eventos..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow text-zinc-900 dark:text-white"
              />
            </div>

            <div className="relative min-w-[160px]">
              <Filter className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow text-zinc-900 dark:text-white appearance-none"
              >
                <option value="">Todos os status</option>
                <option value="ACTIVE">Ativo</option>
                <option value="UPCOMING">Próximo</option>
                <option value="IN_PROGRESS">Em Andamento</option>
                <option value="COMPLETED">Finalizado</option>
                <option value="CANCELLED">Cancelado</option>
              </select>
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3">
            <button
              onClick={handleRefresh}
              disabled={isRefreshing}
              className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-zinc-700 dark:text-zinc-300 bg-zinc-100 dark:bg-zinc-800 hover:bg-zinc-200 dark:hover:bg-zinc-700 rounded-lg transition-colors disabled:opacity-50"
            >
              <RefreshCw
                className={`w-4 h-4 ${isRefreshing ? "animate-spin" : ""}`}
              />
              Atualizar
            </button>

            <Link
              href="/events/new"
              className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors shadow-sm"
            >
              <Plus className="w-4 h-4" />
              Novo Evento
            </Link>
          </div>
        </div>
      </div>

      {/* Stats Summary */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        {[
          {
            label: "Total",
            value: events.length,
            color: "text-zinc-900 dark:text-white",
            bg: "bg-zinc-100 dark:bg-zinc-800",
          },
          {
            label: "Ativos",
            value: events.filter((e) => e.status === "ACTIVE").length,
            color: "text-green-700 dark:text-green-400",
            bg: "bg-green-50 dark:bg-green-900/20",
          },
          {
            label: "Próximos",
            value: events.filter((e) => e.status === "UPCOMING").length,
            color: "text-blue-700 dark:text-blue-400",
            bg: "bg-blue-50 dark:bg-blue-900/20",
          },
          {
            label: "Finalizados",
            value: events.filter((e) => e.status === "COMPLETED").length,
            color: "text-zinc-600 dark:text-zinc-400",
            bg: "bg-zinc-50 dark:bg-zinc-800/50",
          },
          {
            label: "Cancelados",
            value: events.filter((e) => e.status === "CANCELLED").length,
            color: "text-red-700 dark:text-red-400",
            bg: "bg-red-50 dark:bg-red-900/20",
          },
        ].map((stat) => (
          <div
            key={stat.label}
            className={`${stat.bg} rounded-xl p-4 border border-zinc-200 dark:border-zinc-800`}
          >
            <p className="text-xs font-medium text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
              {stat.label}
            </p>
            <p className={`text-2xl font-bold mt-1 ${stat.color}`}>
              {stat.value}
            </p>
          </div>
        ))}
      </div>

      {/* Events Table */}
      <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm overflow-hidden">
        {filteredEvents.length > 0 ? (
          <>
            {/* Desktop Table */}
            <div className="hidden md:block overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="bg-zinc-50 dark:bg-zinc-800/50 border-b border-zinc-200 dark:border-zinc-800">
                    <th className="text-left px-6 py-3 text-xs font-semibold text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
                      Evento
                    </th>
                    <th className="text-left px-6 py-3 text-xs font-semibold text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
                      Data
                    </th>
                    <th className="text-left px-6 py-3 text-xs font-semibold text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
                      Local
                    </th>
                    <th className="text-center px-6 py-3 text-xs font-semibold text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
                      Inscritos
                    </th>
                    <th className="text-center px-6 py-3 text-xs font-semibold text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="text-right px-6 py-3 text-xs font-semibold text-zinc-500 dark:text-zinc-400 uppercase tracking-wider">
                      Ações
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
                  {filteredEvents.map((event) => (
                    <tr
                      key={event.id}
                      className="hover:bg-zinc-50 dark:hover:bg-zinc-800/30 transition-colors"
                    >
                      <td className="px-6 py-4">
                        <div className="max-w-xs">
                          <p className="text-sm font-semibold text-zinc-900 dark:text-white truncate">
                            {event.title}
                          </p>
                          <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5">
                            <span className="capitalize">
                              {event.categoryName}
                            </span>
                            {event.organizerName && ` • ${event.organizerName}`}
                          </p>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-1.5 text-sm text-zinc-600 dark:text-zinc-400">
                          <Calendar className="w-3.5 h-3.5 text-zinc-400" />
                          <span>{formatDateTime(event.startTime)}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-1.5 text-sm text-zinc-600 dark:text-zinc-400 max-w-[150px]">
                          <MapPin className="w-3.5 h-3.5 text-zinc-400 flex-shrink-0" />
                          <span className="truncate">
                            {event.onlineLink
                              ? "Online"
                              : event.location?.name || "—"}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <div className="flex items-center justify-center gap-1.5 text-sm text-zinc-600 dark:text-zinc-400">
                          <Users className="w-3.5 h-3.5 text-zinc-400" />
                          <span>
                            {event.registeredCount ?? 0}/{event.maxCapacity}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-center">
                        {getStatusBadge(event.status)}
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center justify-end gap-2">
                          <Link
                            href={`/events/${event.id}`}
                            className="p-2 text-zinc-400 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg transition-colors"
                            title="Ver detalhes"
                          >
                            <Eye className="w-4 h-4" />
                          </Link>
                          {event.status === "COMPLETED" && (
                            <Link
                              href={`/events/${event.id}/feedback`}
                              className="p-2 text-zinc-400 hover:text-emerald-600 dark:hover:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 rounded-lg transition-colors"
                              title="Ver feedbacks"
                            >
                              <MessageSquareText className="w-4 h-4" />
                            </Link>
                          )}
                          <button
                            onClick={() => handleEditClick(event.id)}
                            disabled={loadingEventId === event.id}
                            className="p-2 text-zinc-400 hover:text-amber-600 dark:hover:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-900/20 rounded-lg transition-colors disabled:opacity-50"
                            title="Editar"
                          >
                            {loadingEventId === event.id ? (
                              <svg
                                className="animate-spin w-4 h-4"
                                fill="none"
                                viewBox="0 0 24 24"
                              >
                                <circle
                                  className="opacity-25"
                                  cx="12"
                                  cy="12"
                                  r="10"
                                  stroke="currentColor"
                                  strokeWidth="4"
                                />
                                <path
                                  className="opacity-75"
                                  fill="currentColor"
                                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                                />
                              </svg>
                            ) : (
                              <Pencil className="w-4 h-4" />
                            )}
                          </button>
                          <button
                            onClick={() =>
                              handleDeleteClick(event.id, event.title)
                            }
                            className="p-2 text-zinc-400 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                            title="Excluir"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Mobile Cards */}
            <div className="md:hidden divide-y divide-zinc-100 dark:divide-zinc-800">
              {filteredEvents.map((event) => (
                <div key={event.id} className="p-4 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-semibold text-zinc-900 dark:text-white truncate">
                        {event.title}
                      </p>
                      <p className="text-xs text-zinc-500 dark:text-zinc-400 mt-0.5 capitalize">
                        {event.categoryName}
                        {event.organizerName && ` • ${event.organizerName}`}
                      </p>
                    </div>
                    {getStatusBadge(event.status)}
                  </div>

                  <div className="grid grid-cols-2 gap-2 text-xs text-zinc-500 dark:text-zinc-400">
                    <div className="flex items-center gap-1.5">
                      <Calendar className="w-3 h-3" />
                      {formatDateTime(event.startTime)}
                    </div>
                    <div className="flex items-center gap-1.5">
                      <Users className="w-3 h-3" />
                      {event.registeredCount ?? 0}/{event.maxCapacity}
                    </div>
                    <div className="flex items-center gap-1.5 col-span-2">
                      <MapPin className="w-3 h-3" />
                      {event.onlineLink
                        ? "Online"
                        : event.location?.name || "—"}
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2 pt-1">
                    <Link
                      href={`/events/${event.id}`}
                      className="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-zinc-700 dark:text-zinc-300 bg-zinc-100 dark:bg-zinc-800 hover:bg-zinc-200 dark:hover:bg-zinc-700 rounded-lg transition-colors"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      Ver
                    </Link>
                    {event.status === "COMPLETED" && (
                      <Link
                        href={`/events/${event.id}/feedback`}
                        className="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-900/20 hover:bg-emerald-100 dark:hover:bg-emerald-900/30 rounded-lg transition-colors"
                      >
                        <MessageSquareText className="w-3.5 h-3.5" />
                        Feedbacks
                      </Link>
                    )}
                    <button
                      onClick={() => handleEditClick(event.id)}
                      disabled={loadingEventId === event.id}
                      className="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-900/20 hover:bg-amber-100 dark:hover:bg-amber-900/30 rounded-lg transition-colors disabled:opacity-50"
                    >
                      <Pencil className="w-3.5 h-3.5" />
                      Editar
                    </button>
                    <button
                      onClick={() => handleDeleteClick(event.id, event.title)}
                      className="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-medium text-red-700 dark:text-red-300 bg-red-50 dark:bg-red-900/20 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg transition-colors"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      Excluir
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </>
        ) : (
          <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
            <div className="w-16 h-16 bg-zinc-100 dark:bg-zinc-800 rounded-full flex items-center justify-center mb-4">
              <span className="text-2xl">📋</span>
            </div>
            <h3 className="text-lg font-medium text-zinc-900 dark:text-white mb-1">
              {events.length === 0
                ? "Nenhum evento cadastrado"
                : "Nenhum evento encontrado"}
            </h3>
            <p className="text-zinc-500 dark:text-zinc-400 max-w-md mx-auto mb-6 text-sm">
              {events.length === 0
                ? "Comece criando seu primeiro evento clicando no botão acima."
                : "Tente ajustar seus filtros de busca ou limpar os critérios."}
            </p>
            {events.length > 0 && (
              <button
                onClick={() => {
                  setSearchTerm("");
                  setStatusFilter("");
                }}
                className="text-blue-600 dark:text-blue-400 hover:underline font-medium text-sm"
              >
                Limpar Filtros
              </button>
            )}
          </div>
        )}
      </div>

      {/* Footer info */}
      <div className="flex items-center justify-between text-xs text-zinc-500 dark:text-zinc-400">
        <span>
          Exibindo {filteredEvents.length} de {events.length} eventos
        </span>
      </div>

      {/* Edit Modal */}
      <EditEventModal
        isOpen={editModalOpen}
        event={editingEvent}
        categories={categories}
        locations={locations}
        requirements={requirements}
        tags={tags}
        speakers={speakers}
        organizers={organizers}
        daysBeforeNotify=""
        onClose={() => {
          setEditModalOpen(false);
          setEditingEvent(null);
        }}
        onSuccess={handleEditSuccess}
      />

      {/* Delete Confirm Modal */}
      <DeleteConfirmModal
        isOpen={deleteModalOpen}
        eventTitle={deletingEvent?.title || ""}
        onClose={() => {
          setDeleteModalOpen(false);
          setDeletingEvent(null);
        }}
        onConfirm={handleDeleteConfirm}
      />
    </div>
  );
}
