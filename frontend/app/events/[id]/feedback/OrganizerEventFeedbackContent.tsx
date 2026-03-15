"use client";

import { EventEvaluationsList } from "@/components/events/EventEvaluationsList";
import { OrganizerEventFeedbackResponseDTO } from "@/types/evaluations";
import { MessageSquareText, Star, TrendingUp } from "lucide-react";

interface OrganizerEventFeedbackContentProps {
  feedbackData: OrganizerEventFeedbackResponseDTO;
}

export default function OrganizerEventFeedbackContent({
  feedbackData,
}: Readonly<OrganizerEventFeedbackContentProps>) {
  const averageRatingLabel =
    feedbackData.totalFeedbacks > 0
      ? feedbackData.averageRating.toFixed(1)
      : "—";

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <SummaryCard
          icon={<MessageSquareText className="w-5 h-5 text-blue-500" />}
          label="Feedbacks recebidos"
          value={feedbackData.totalFeedbacks}
          subtitle="comentários publicados pelos participantes"
        />
        <SummaryCard
          icon={<Star className="w-5 h-5 text-amber-500" />}
          label="Média das avaliações"
          value={averageRatingLabel}
          subtitle={
            feedbackData.totalFeedbacks > 0
              ? "nota consolidada do evento"
              : "sem avaliações registradas"
          }
        />
        <SummaryCard
          icon={<TrendingUp className="w-5 h-5 text-emerald-500" />}
          label="Status da coleta"
          value={feedbackData.totalFeedbacks > 0 ? "Ativa" : "Aguardando"}
          subtitle="novos feedbacks aparecem aqui automaticamente"
        />
      </div>

      <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm p-6">
        <div className="mb-6">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-zinc-500 dark:text-zinc-400">
            Evento
          </p>
          <h2 className="mt-2 text-2xl font-bold text-zinc-900 dark:text-white">
            {feedbackData.eventTitle}
          </h2>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
            Acompanhe o que os participantes acharam da experiência para
            identificar pontos fortes e oportunidades de melhoria.
          </p>
        </div>

        <EventEvaluationsList evaluations={feedbackData.feedbacks} />
      </div>
    </div>
  );
}

function SummaryCard({
  icon,
  label,
  value,
  subtitle,
}: Readonly<{
  icon: React.ReactNode;
  label: string;
  value: string | number;
  subtitle: string;
}>) {
  return (
    <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm p-5">
      <div className="flex items-start gap-3">
        <div className="flex-shrink-0 p-2 rounded-lg bg-zinc-50 dark:bg-zinc-800">
          {icon}
        </div>
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.14em] text-zinc-500 dark:text-zinc-400">
            {label}
          </p>
          <p className="mt-2 text-2xl font-bold text-zinc-900 dark:text-white">
            {value}
          </p>
          <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
            {subtitle}
          </p>
        </div>
      </div>
    </div>
  );
}
