// lib/authService.ts
const API_URL = process.env.NEXT_PUBLIC_API_URL;

class AuthService {
  /**
   * Realiza o logout do usuário
   * Remove o token do localStorage e limpa a sessão
   */
  async logout(): Promise<{ success: boolean; message: string }> {
    try {
      const token = localStorage.getItem('token');
      
      if (token) {
        // Faz a requisição ao backend para invalidar a sessão
        await fetch(`${API_URL}/auth/logout`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
      }
      
      // Remove dados do localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // Remove dados do sessionStorage (se houver)
      sessionStorage.clear();
      
      return { success: true, message: 'Logout realizado com sucesso' };
      
    } catch (error) {
      // Mesmo com erro na API, limpa os dados locais
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      sessionStorage.clear();
      
      console.error('Erro ao fazer logout:', error);
      return { 
        success: false, 
        message: 'Erro ao realizar logout, mas seus dados foram limpos localmente' 
      };
    }
  }

  /**
   * Verifica se o usuário está autenticado
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    return !!token;
  }

  /**
   * Obtém o usuário atual do localStorage
   */
  getCurrentUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  /**
   * Obtém o token do localStorage
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }
}

export const authService = new AuthService();