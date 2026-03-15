"use client";

import { useState } from "react";
import { EventEvaluationsList } from "@/components/events/EventEvaluationsList";
import { OrganizerEventFeedbackResponseDTO } from "@/types/evaluations";
import {
  ArrowDownWideNarrow,
  CalendarRange,
  MessageSquareText,
  Star,
  TrendingUp,
} from "lucide-react";

interface OrganizerEventFeedbackContentProps {
  feedbackData: OrganizerEventFeedbackResponseDTO;
}

export default function OrganizerEventFeedbackContent({
  feedbackData,
}: Readonly<OrganizerEventFeedbackContentProps>) {
  const [ratingFilter, setRatingFilter] = useState("ALL");
  const [sortOption, setSortOption] = useState("date_desc");

  const averageRatingLabel =
    feedbackData.totalFeedbacks > 0
      ? feedbackData.averageRating.toFixed(1)
      : "—";

  const filteredFeedbacks = feedbackData.feedbacks.filter((feedback) => {
    if (ratingFilter === "ALL") {
      return true;
    }

    return feedback.rating === Number(ratingFilter);
  });

  const visibleFeedbacks = [...filteredFeedbacks].sort((first, second) => {
    if (sortOption === "date_asc") {
      return (
        new Date(first.createdAt).getTime() -
        new Date(second.createdAt).getTime()
      );
    }

    if (sortOption === "rating_desc") {
      return second.rating - first.rating;
    }

    if (sortOption === "rating_asc") {
      return first.rating - second.rating;
    }

    return (
      new Date(second.createdAt).getTime() -
      new Date(first.createdAt).getTime()
    );
  });

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

        <div className="mb-6 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-zinc-500 dark:text-zinc-400">
              Visualização
            </p>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              Exibindo {visibleFeedbacks.length} de {feedbackData.totalFeedbacks}{" "}
              feedbacks conforme os filtros selecionados.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full lg:w-auto">
            <label className="flex flex-col gap-2 text-sm text-zinc-700 dark:text-zinc-300">
              <span className="inline-flex items-center gap-2 font-medium">
                <Star className="w-4 h-4 text-amber-500" />
                Filtrar por nota
              </span>
              <select
                aria-label="Filtrar feedbacks por nota"
                value={ratingFilter}
                onChange={(event) => setRatingFilter(event.target.value)}
                className="min-w-[220px] rounded-lg border border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 px-3 py-2.5 text-sm text-zinc-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="ALL">Todas as notas</option>
                <option value="5">5 estrelas</option>
                <option value="4">4 estrelas</option>
                <option value="3">3 estrelas</option>
                <option value="2">2 estrelas</option>
                <option value="1">1 estrela</option>
              </select>
            </label>

            <label className="flex flex-col gap-2 text-sm text-zinc-700 dark:text-zinc-300">
              <span className="inline-flex items-center gap-2 font-medium">
                <ArrowDownWideNarrow className="w-4 h-4 text-blue-500" />
                Ordenar feedbacks
              </span>
              <select
                aria-label="Ordenar feedbacks"
                value={sortOption}
                onChange={(event) => setSortOption(event.target.value)}
                className="min-w-[220px] rounded-lg border border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 px-3 py-2.5 text-sm text-zinc-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="date_desc">Data mais recente</option>
                <option value="date_asc">Data mais antiga</option>
                <option value="rating_desc">Maior nota</option>
                <option value="rating_asc">Menor nota</option>
              </select>
            </label>
          </div>
        </div>

        {visibleFeedbacks.length > 0 ? (
          <EventEvaluationsList
            evaluations={visibleFeedbacks}
            commentsTitle="Comentários dos participantes"
          />
        ) : (
          <div className="rounded-xl border border-dashed border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800/40 p-10 text-center">
            <div className="w-12 h-12 rounded-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 mx-auto flex items-center justify-center mb-4">
              <CalendarRange className="w-5 h-5 text-zinc-500" />
            </div>
            <h3 className="text-lg font-bold text-zinc-900 dark:text-white">
              Nenhum feedback encontrado
            </h3>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              Ajuste os filtros para visualizar avaliações com outra nota ou
              ordenação.
            </p>
          </div>
        )}
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
