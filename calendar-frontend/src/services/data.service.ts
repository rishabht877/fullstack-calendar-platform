import api from './api';
import { type Calendar, type CalendarEvent, type Analytics } from '../types';

export const CalendarService = {
    getCalendars: async (): Promise<Calendar[]> => {
        const response = await api.get('/calendars');
        return response.data;
    },

    createCalendar: async (data: { name: string, timezone: string }): Promise<Calendar> => {
        const response = await api.post('/calendars', data);
        return response.data;
    },

    deleteCalendar: async (id: number) => {
        return api.delete(`/calendars/${id}`);
    },

    exportCalendar: async (id: number) => {
        const response = await api.get(`/calendars/${id}/export`, {
            responseType: 'blob'
        });
        return response.data;
    }
};

export const EventService = {
    getEvents: async (calendarId: number): Promise<CalendarEvent[]> => {
        const response = await api.get(`/events/calendar/${calendarId}`);
        return response.data;
    },

    createEvent: async (calendarId: number, event: Partial<CalendarEvent>): Promise<CalendarEvent> => {
        console.log(`EventService.createEvent sending to /events/calendar/${calendarId}`, event);
        const response = await api.post(`/events/calendar/${calendarId}`, event);
        console.log("EventService.createEvent response:", response.status, response.data);
        return response.data;
    },

    updateEvent: async (id: number, event: Partial<CalendarEvent>): Promise<CalendarEvent> => {
        return (await api.put(`/events/${id}`, event)).data;
    },

    deleteEvent: async (id: number) => {
        return api.delete(`/events/${id}`);
    }
};

export const AnalyticsService = {
    getAnalytics: async (): Promise<Analytics> => {
        const response = await api.get('/analytics');
        return response.data;
    }
};

export const GoogleCalendarService = {
    fetchEvents: async (): Promise<any[]> => {
        const response = await api.get('/google/events');
        return response.data;
    },

    exportEvent: async (event: any): Promise<any> => {
        const response = await api.post('/google/export', event);
        return response.data;
    }
};
