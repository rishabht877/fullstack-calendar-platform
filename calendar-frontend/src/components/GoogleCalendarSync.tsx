import React, { useState, useEffect } from 'react';
import api from '../services/api';

interface Props {
    onSyncComplete?: () => void;
}

const GoogleCalendarSync: React.FC<Props> = ({ onSyncComplete }) => {
    const [isConnected, setIsConnected] = useState<boolean | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [lastSynced, setLastSynced] = useState<string | null>(localStorage.getItem('google_last_sync'));
    const [syncStatus, setSyncStatus] = useState<'idle' | 'syncing' | 'success' | 'error'>('idle');

    useEffect(() => {
        const checkStatus = async () => {
            try {
                const response = await api.get('/google/status');
                if (response.data.connected) {
                    setIsConnected(true);
                }
            } catch (error) {
                console.error("Failed to check Google status", error);
            }
        };

        checkStatus();

        const params = new URLSearchParams(window.location.search);
        const connectedParam = params.get('google_connected');
        const errorParam = params.get('error');

        if (connectedParam === 'true') {
            setIsConnected(true);
            alert("Successfully connected to Google Calendar!");
            window.history.replaceState({}, document.title, window.location.pathname);
        } else if (connectedParam === 'false') {
            alert(`Google Connection Failed: ${errorParam || 'Unknown error'}`);
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }, []);

    const handleConnect = async () => {
        setIsLoading(true);
        try {
            const response = await api.get('/google/auth');
            if (response.data.authUrl) {
                window.location.href = response.data.authUrl;
            }
        } catch (error) {
            console.error("Failed to initiate Google Auth", error);
            alert("Failed to start Google Sync. Check console.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleSync = async () => {
        if (isLoading) return;
        setIsLoading(true);
        setSyncStatus('syncing');
        try {
            const response = await api.post('/google/sync');
            const data = response.data;

            if (data.status === 'success') {
                const now = new Date().toLocaleString();
                setLastSynced(now);
                localStorage.setItem('google_last_sync', now);
                setSyncStatus('success');

                if (onSyncComplete) {
                    onSyncComplete();
                }
            } else {
                setSyncStatus('error');
                alert(`Sync failed: ${data.message}`);
            }
        } catch (error: any) {
            setSyncStatus('error');
            console.error("Error during sync:", error);
            const errorMsg = error.response?.data?.message || error.message || "Failed to sync events.";
            alert(`${errorMsg}`);
        } finally {
            setIsLoading(false);
            // After 3 seconds, reset status to idle if it was success
            setTimeout(() => {
                setSyncStatus(prev => prev === 'success' ? 'idle' : prev);
            }, 3000);
        }
    };

    return (
        <div className="flex flex-col items-end gap-1">
            <div className="flex items-center gap-2">
                <button
                    onClick={handleConnect}
                    disabled={isConnected || isLoading}
                    className={`
                        flex items-center gap-2 px-3 py-1.5 rounded text-sm font-medium transition-colors border
                        ${isConnected
                            ? 'bg-green-50 text-green-700 border-green-200 cursor-default'
                            : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                        }
                    `}
                >
                    {isLoading && syncStatus !== 'syncing' ? (
                        <span>Connecting...</span>
                    ) : isConnected ? (
                        <>
                            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                            </svg>
                            <span>Google Connected</span>
                        </>
                    ) : (
                        <>
                            <svg className="w-4 h-4" viewBox="0 0 24 24">
                                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
                            </svg>
                            <span>Sync Google Calendar</span>
                        </>
                    )}
                </button>

                {isConnected && (
                    <button
                        onClick={handleSync}
                        disabled={isLoading}
                        className={`text-xs font-medium px-2 py-1 rounded transition-all ${syncStatus === 'syncing' ? 'text-gray-400 cursor-not-allowed' :
                                syncStatus === 'success' ? 'text-green-600' :
                                    syncStatus === 'error' ? 'text-red-600' :
                                        'text-blue-600 hover:text-blue-800 hover:bg-blue-50'
                            }`}
                    >
                        {syncStatus === 'syncing' ? 'Syncing...' :
                            syncStatus === 'success' ? 'Synced!' :
                                syncStatus === 'error' ? 'Retry Sync' : 'Sync Now'}
                    </button>
                )}
            </div>
            {lastSynced && isConnected && (
                <span className="text-[10px] text-gray-400">
                    Last synced: {lastSynced}
                </span>
            )}
        </div>
    );
};

export default GoogleCalendarSync;
