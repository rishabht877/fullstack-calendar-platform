export interface User {
    id: number;
    username: string;
    email: string;
}

export interface Calendar {
    id: number;
    name: string;
    timezone: string;
    color?: string;
    userId: number;
}

export interface CalendarEvent {
    id?: number;
    subject: string;
    startTime: string;
    endTime: string;
    description?: string;
    location?: string;
    status: 'CONFIRMED' | 'TENTATIVE' | 'CANCELLED';
    calendarId: number;
    seriesId?: string;
    isAllDay?: boolean;
}

export interface Analytics {
    totalEvents: number;
    weekEvents: number;
    monthEvents: number;
    upcomingEvents: number;
    totalCalendars: number;
    eventsByStatus: Record<string, number>;
    averageEventsPerDay: number;
    busiestDayOfWeek: string;
    eventsBySubject: Record<string, number>;
    eventsByWeekday: Record<string, number>;
    leastBusyDayOfWeek: string;
    onlinePercentage: number;
}
