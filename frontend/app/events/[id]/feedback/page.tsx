import Link from "next/link";
import { ArrowLeft, AlertTriangle, FileText } from "lucide-react";
import { RoleGuard } from "@/components/auth/RoleGuard";
import { getOrganizerEventFeedbackAction } from "@/app/actions/organizerFeedbackActions";
import OrganizerEventFeedbackContent from "./OrganizerEventFeedbackContent";

export const dynamic = "force-dynamic";

export default async function EventFeedbackPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const result = await getOrganizerEventFeedbackAction(id);

  return (
    <RoleGuard allowedRoles={["ORGANIZER", "ADMIN"]}>
      <div className="min-h-screen bg-zinc-50 dark:bg-black py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-8">
            <div>
              <Link
                href="/events/manage"
                className="inline-flex items-center text-sm text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white transition-colors"
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                Voltar para Gerenciar Eventos
              </Link>
              <h1 className="mt-4 text-3xl font-bold text-zinc-900 dark:text-white">
                Feedbacks do Evento
              </h1>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                Visualize as avaliações deixadas pelos participantes do evento.
              </p>
            </div>

            <Link
              href={`/events/${id}`}
              className="inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 text-sm font-medium text-zinc-700 dark:text-zinc-300 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors"
            >
              <FileText className="w-4 h-4" />
              Ver detalhes do evento
            </Link>
          </div>

          {result.data ? (
            <OrganizerEventFeedbackContent feedbackData={result.data} />
          ) : (
            <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm p-10 text-center">
              <div className="w-14 h-14 rounded-full bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 mx-auto flex items-center justify-center mb-4">
                <AlertTriangle className="w-6 h-6" />
              </div>
              <h2 className="text-xl font-bold text-zinc-900 dark:text-white">
                Não foi possível carregar os feedbacks
              </h2>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400 max-w-xl mx-auto">
                {result.error ||
                  "O evento não foi encontrado ou você não tem permissão para visualizar estes dados."}
              </p>
            </div>
          )}
        </div>
      </div>
    </RoleGuard>
  );
}
