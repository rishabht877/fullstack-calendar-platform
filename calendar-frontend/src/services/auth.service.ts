import api from './api';
import { type User } from '../types';

export const AuthService = {
    login: async (username: string, password: string): Promise<{ token: string; username: string; email: string; id: number }> => {
        const response = await api.post('/auth/login', { username, password });
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data));
        }
        return response.data;
    },

    register: async (username: string, email: string, password: string) => {
        return api.post('/auth/register', { username, email, password });
    },

    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    getCurrentUser: (): User | null => {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    }
};
