// hooks/useAuth.ts
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/lib/authService';

export function useAuth() {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {

    const checkAuth = () => {
      const isAuth = authService.isAuthenticated();
      
      if (isAuth) {
        const currentUser = authService.getCurrentUser();
        setUser(currentUser);
      } else {
        setUser(null);
      }
      
      setLoading(false);
    };

    checkAuth();
  }, []);

  const logout = async () => {
    try {
      await authService.logout();
      setUser(null);
      router.push('/signin');
      router.refresh();
    } catch (error) {
      console.error('Erro ao fazer logout:', error);
      // Garante que o estado seja atualizado mesmo com erro
      setUser(null);
      router.push('/signin');
    }
  };

  return {
    user,
    loading,
    isAuthenticated: !!user,
    logout
  };
}