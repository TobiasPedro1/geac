import { EvaluationResponseDTO } from "@/types/evaluations";
import { Star, MessageSquare } from "lucide-react";

interface EventEvaluationsListProps {
  evaluations: EvaluationResponseDTO[];
  commentsTitle?: string;
}

export function EventEvaluationsList({
  evaluations,
  commentsTitle = "Comentários Recentes",
}: Readonly<EventEvaluationsListProps>) {
  if (!evaluations || evaluations.length === 0) {
    return (
      <div className="bg-white dark:bg-zinc-900 rounded-xl p-8 text-center border border-zinc-200 dark:border-zinc-800">
        <MessageSquare className="w-12 h-12 text-zinc-300 dark:text-zinc-600 mx-auto mb-3" />
        <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-1">
          Nenhuma avaliação ainda
        </h3>
        <p className="text-sm text-zinc-500 dark:text-zinc-400">
          Este evento não recebeu nenhuma avaliação dos participantes.
        </p>
      </div>
    );
  }

  const totalReviews = evaluations.length;
  const averageRating = (
    evaluations.reduce((acc, curr) => acc + curr.rating, 0) / totalReviews
  ).toFixed(1);

  const ratingCounts = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
  evaluations.forEach((ev) => {
    if (ev.rating >= 1 && ev.rating <= 5) {
      ratingCounts[ev.rating as keyof typeof ratingCounts]++;
    }
  });

  return (
    <div className="space-y-8">
      <div className="bg-white dark:bg-zinc-900 rounded-xl p-6 border border-zinc-200 dark:border-zinc-800 flex flex-col md:flex-row gap-8 items-center">
        <div className="flex flex-col items-center justify-center min-w-[150px]">
          <span className="text-5xl font-extrabold text-zinc-900 dark:text-white mb-2">
            {averageRating}
          </span>
          <div className="flex text-yellow-400 mb-1">
            {[1, 2, 3, 4, 5].map((star) => (
              <Star
                key={star}
                className={`w-5 h-5 ${star <= Math.round(Number(averageRating)) ? "fill-current" : "text-zinc-300 dark:text-zinc-600 fill-transparent"}`}
              />
            ))}
          </div>
          <span className="text-sm text-zinc-500 dark:text-zinc-400">
            {totalReviews} avaliações
          </span>
        </div>

        <div className="flex-1 w-full space-y-2">
          {[5, 4, 3, 2, 1].map((star) => {
            const count = ratingCounts[star as keyof typeof ratingCounts];
            const percentage =
              totalReviews > 0 ? (count / totalReviews) * 100 : 0;

            return (
              <div key={star} className="flex items-center gap-3 text-sm">
                <span className="font-medium text-zinc-600 dark:text-zinc-400 w-3">
                  {star}
                </span>
                <Star className="w-4 h-4 text-zinc-400 dark:text-zinc-500" />
                <div className="flex-1 h-2.5 bg-zinc-100 dark:bg-zinc-800 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-yellow-400 rounded-full"
                    style={{ width: `${percentage}%` }}
                  />
                </div>
                <span className="text-zinc-500 dark:text-zinc-400 w-8 text-right">
                  {count}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      <div className="space-y-4">
        <h3 className="text-lg font-bold text-zinc-900 dark:text-white border-b border-zinc-200 dark:border-zinc-800 pb-2">
          {commentsTitle}
        </h3>

        {evaluations.map((ev) => {
          const initial = ev.userName
            ? ev.userName.charAt(0).toUpperCase()
            : "?";
          const date = new Date(ev.createdAt).toLocaleDateString("pt-BR");

          return (
            <div
              key={ev.id}
              className="bg-white dark:bg-zinc-900 p-5 rounded-xl border border-zinc-200 dark:border-zinc-800"
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-400 flex items-center justify-center font-bold text-lg">
                    {initial}
                  </div>
                  <div>
                    <p className="font-semibold text-zinc-900 dark:text-white text-sm">
                      {ev.userName}
                    </p>
                    <p className="text-xs text-zinc-500 dark:text-zinc-400">
                      {date}
                    </p>
                  </div>
                </div>
                <div className="flex gap-0.5 text-yellow-400">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                      key={star}
                      className={`w-4 h-4 ${star <= ev.rating ? "fill-current" : "text-zinc-300 dark:text-zinc-700 fill-transparent"}`}
                    />
                  ))}
                </div>
              </div>
              <p className="text-zinc-700 dark:text-zinc-300 text-sm leading-relaxed whitespace-pre-wrap">
                &quot;{ev.comment}&quot;
              </p>
            </div>
          );
        })}
      </div>
    </div>
  );
}
