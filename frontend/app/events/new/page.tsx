import { RoleGuard } from "@/components/auth/RoleGuard";
import CreateEventForm from "@/app/events/CreateEventForm"; // Componente do formulário

export default function NewEventPage() {
  return (
    <RoleGuard allowedRoles={["PROFESSOR"]}>
      <div className="min-h-screen bg-gray-50 dark:bg-black py-12">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-8 text-center sm:text-left">
            <h1 className="text-3xl font-extrabold text-zinc-900 dark:text-white">
              Cadastrar Novo Evento
            </h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              Preencha os dados abaixo para divulgar um novo evento acadêmico.
            </p>
          </div>
          
          <CreateEventForm />
        </div>
      </div>
    </RoleGuard>
  );
}