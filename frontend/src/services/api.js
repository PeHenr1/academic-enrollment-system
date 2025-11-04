import axios from 'axios';

// Configura a URL base da sua API Spring Boot
const api = axios.create({
  baseURL: 'http://localhost:8080', // Ajuste a porta se necessário
});

// Interceptor para adicionar o token JWT a cada requisição protegida
api.interceptors.request.use(
  (config) => {
    // Pega o token do localStorage
    const token = localStorage.getItem('authToken');
    if (token) {
      // Adiciona o cabeçalho 'Authorization: Bearer <token>'
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;