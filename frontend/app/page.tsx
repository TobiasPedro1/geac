import { Navbar } from "@/components/navbar";

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col bg-zinc-50 dark:bg-black font-sans">
      <Navbar />
      <main className="flex flex-1 items-center justify-center p-4">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-zinc-900 dark:text-white mb-4">
            Bem-vindo ao GEAC
          </h1>
          <p className="text-zinc-600 dark:text-zinc-400 text-lg">
            Sistema de Gestão de Eventos Acadêmicos e Culturais
          </p>
        </div>
      </main>
    </div>
  );
}