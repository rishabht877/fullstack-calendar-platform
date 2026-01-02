import React, { createContext, useState, useContext, useEffect } from 'react';
import { AuthService } from '../services/auth.service';
import { type User } from '../types';

interface AuthContextType {
    user: User | null;
    login: (user: User, token: string) => void;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        const currentUser = AuthService.getCurrentUser();
        if (currentUser) {
            setUser(currentUser);
        }
    }, []);

    const login = (userData: User, token: string) => {
        setUser(userData);
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(userData));
    };

    const logout = () => {
        AuthService.logout();
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
