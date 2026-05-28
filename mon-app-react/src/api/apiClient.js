import axios from 'axios';
import useAuthStore from '../store/useAuthStore';

// Create Axios instance with customizable base URL using Vite environment variables
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds timeout for standard requests
});

// Request Interceptor: Automatically inject JWT access token from Zustand store
apiClient.interceptors.request.use(
  (config) => {
    // Non-reactive access to the Zustand store state outside of React component lifecycle
    const token = useAuthStore.getState().token;
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Capture global API errors, specifically handling 401 Unauthorized
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Intercept 401 Unauthorized errors to automatically flush authentication state and force a logout
    if (error.response && error.response.status === 401) {
      console.warn('Unauthorized API access detected (401). Flushing session and redirecting...');
      
      // Clear the Zustand store global state (which automatically updates localStorage)
      useAuthStore.getState().logout();
      
      // Optionally, you can trigger a physical redirection if you are not handling route guards reactively:
      // window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
