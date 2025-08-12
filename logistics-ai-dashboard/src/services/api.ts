import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor
    this.api.interceptors.request.use(
      (config) => {
        console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`);
        return config;
      },
      (error: AxiosError) => {
        console.error('Request error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.api.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      (error: AxiosError) => {
        console.error('Response error:', error);

        if (error.response) {
          // Server responded with error status
          const errorMessage = error.response.data || `Request failed with status ${error.response.status}`;
          console.error('Server error:', errorMessage);
        } else if (error.request) {
          // Request made but no response received
          console.error('Network error: No response received');
        } else {
          // Something else happened
          console.error('Request setup error:', error.message);
        }

        return Promise.reject(error);
      }
    );
  }

  // Generic methods
  async get<T = any>(url: string): Promise<AxiosResponse<T>> {
    return this.api.get<T>(url);
  }

  async post<T = any>(url: string, data?: any): Promise<AxiosResponse<T>> {
    return this.api.post<T>(url, data);
  }

  async put<T = any>(url: string, data?: any): Promise<AxiosResponse<T>> {
    return this.api.put<T>(url, data);
  }

  async delete<T = any>(url: string): Promise<AxiosResponse<T>> {
    return this.api.delete<T>(url);
  }

  // Specific API methods
  async getMetrics() {
    return this.get('/api/metrics/current');
  }

  async getOrders() {
    return this.get('/api/orders/stats');
  }

  async getOrderById(id: number) {
    return this.get(`/api/orders/${id}/status`);
  }

  async submitOrder(orderData: any) {
    return this.post('/api/orders/submit', orderData);
  }

  async getWarehouseShipments() {
    return this.get('/api/warehouse/pending-shipments');
  }

  async getTodayShipments() {
    return this.get('/api/warehouse/today-shipments');
  }

  async startLoading(shipmentId: number) {
    return this.post(`/api/warehouse/shipments/${shipmentId}/start-loading`);
  }

  async completeLoading(shipmentId: number) {
    return this.post(`/api/warehouse/shipments/${shipmentId}/complete-loading`);
  }

  async dispatchShipment(shipmentId: number) {
    return this.post(`/api/warehouse/shipments/${shipmentId}/dispatch`);
  }

  async markDelivered(shipmentId: number) {
    return this.post(`/api/warehouse/shipments/${shipmentId}/delivered`);
  }

  async generateBulkOrders(count: number, intensity: string) {
    return this.post(`/api/testing/bulk-orders?count=${count}&intensity=${intensity}`);
  }

  async runTestScenario(scenario: string) {
    return this.post(`/api/testing/scenarios/${scenario}`);
  }

  async getHealthStatus() {
    return this.get('/actuator/health');
  }

  // Monitoring methods
  async startMonitoring() {
    return this.post('/api/metrics/monitoring/start');
  }

  async stopMonitoring() {
    return this.post('/api/metrics/monitoring/stop');
  }

  async getRecommendations() {
    return this.get('/api/metrics/recommendations');
  }
}

const api = new ApiService();
export default api;
