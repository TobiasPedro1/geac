"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { authService } from "@/lib/authService";

export function Navbar() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<any>(null);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    
    // Verifica autenticação ao montar o componente
    const checkAuth = () => {
      try {
        const isAuth = authService.isAuthenticated();
        setIsAuthenticated(isAuth);
        
        if (isAuth) {
          const currentUser = authService.getCurrentUser();
          setUser(currentUser);
        }
      } catch (error) {
        console.error('Erro ao verificar autenticação:', error);
        setIsAuthenticated(false);
        setUser(null);
      }
    };

    checkAuth();

    // Atualiza quando o localStorage muda (em outra aba, por exemplo)
    const handleStorageChange = () => {
      checkAuth();
    };

    window.addEventListener('storage', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const handleLogout = async () => {
    const confirmLogout = window.confirm('Deseja realmente sair do sistema?');
    
    if (!confirmLogout) return;
    
    setIsLoggingOut(true);
    
    try {
      await authService.logout();
      setIsAuthenticated(false);
      setUser(null);
      router.push('/signin');
      router.refresh();
    } catch (error) {
      console.error('Erro ao fazer logout:', error);
      setIsAuthenticated(false);
      setUser(null);
      router.push('/signin');
    } finally {
      setIsLoggingOut(false);
    }
  };

  if (!mounted) {
    return (
      <nav className="bg-white dark:bg-zinc-900 shadow-lg border-b border-zinc-200 dark:border-zinc-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link href="/" className="flex items-center">
                <span className="text-2xl font-bold text-blue-600 dark:text-blue-500">
                  GEAC
                </span>
              </Link>
            </div>
          </div>
        </div>
      </nav>
    );
  }

  return (
    <nav className="bg-white dark:bg-zinc-900 shadow-lg border-b border-zinc-200 dark:border-zinc-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link href="/" className="flex items-center">
              <span className="text-2xl font-bold text-blue-600 dark:text-blue-500">
                GEAC
              </span>
            </Link>
            
            {isAuthenticated && (
              <div className="hidden md:ml-10 md:flex md:space-x-8">
                <Link
                  href="/"
                  className="text-zinc-900 dark:text-zinc-100 hover:text-blue-600 dark:hover:text-blue-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  Eventos
                </Link>
                <Link
                  href="/meus-eventos"
                  className="text-zinc-900 dark:text-zinc-100 hover:text-blue-600 dark:hover:text-blue-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  Meus Eventos
                </Link>
                <Link
                  href="/certificados"
                  className="text-zinc-900 dark:text-zinc-100 hover:text-blue-600 dark:hover:text-blue-500 px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  Certificados
                </Link>
              </div>
            )}
          </div>

          <div className="flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <div className="hidden md:block text-sm">
                  <span className="text-zinc-700 dark:text-zinc-300">Olá, </span>
                  <span className="font-semibold text-zinc-900 dark:text-white">
                    {user?.name || 'Usuário'}
                  </span>
                </div>
                
                <button
                  onClick={handleLogout}
                  disabled={isLoggingOut}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  {isLoggingOut ? (
                    <>
                      <svg 
                        className="animate-spin h-4 w-4" 
                        xmlns="http://www.w3.org/2000/svg" 
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
                      <span className="hidden sm:inline">Saindo...</span>
                    </>
                  ) : (
                    <>
                      <svg 
                        xmlns="http://www.w3.org/2000/svg" 
                        className="h-4 w-4" 
                        viewBox="0 0 20 20" 
                        fill="currentColor"
                      >
                        <path 
                          fillRule="evenodd" 
                          d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" 
                          clipRule="evenodd" 
                        />
                      </svg>
                      <span className="hidden sm:inline">Sair</span>
                    </>
                  )}
                </button>
              </>
            ) : (
              <div className="flex gap-2">
                <Link
                  href="/signin"
                  className="px-4 py-2 text-blue-600 dark:text-blue-500 hover:text-blue-800 dark:hover:text-blue-400 font-medium transition-colors"
                >
                  Entrar
                </Link>
                <Link
                  href="/signup"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors"
                >
                  Cadastrar
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}