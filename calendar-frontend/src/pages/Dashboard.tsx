import React, { useState, useEffect } from 'react';
import { Calendar as BigCalendar, dateFnsLocalizer, type View } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay, addMonths, subMonths } from 'date-fns';
import { enUS } from 'date-fns/locale';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import { useAuth } from '../context/AuthContext';
import { CalendarService, EventService } from '../services/data.service';
import { type Calendar, type CalendarEvent } from '../types';
import CreateEventModal from '../components/CreateEventModal';
import CreateCalendarModal from '../components/CreateCalendarModal';
import ViewDayEventsModal from '../components/ViewDayEventsModal';
import AnalyticsModal from '../components/AnalyticsModal';
import DateInputModal from '../components/DateInputModal';
import GoogleCalendarSync from '../components/GoogleCalendarSync';

const locales = {
    'en-US': enUS,
};

const localizer = dateFnsLocalizer({
    format,
    parse,
    startOfWeek,
    getDay,
    locales,
});



const Dashboard: React.FC = () => {
    const { user } = useAuth();
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isViewDayModalOpen, setIsViewDayModalOpen] = useState(false);
    const [calendars, setCalendars] = useState<Calendar[]>([]);
    const [events, setEvents] = useState<CalendarEvent[]>([]);
    const [selectedCalendarId, setSelectedCalendarId] = useState<number | null>(null);
    const [currentDate, setCurrentDate] = useState(new Date());
    const [view, setView] = useState<View>('month');
    const [selectedEvent, setSelectedEvent] = useState<any>(null); // State for editing

    const [isDateInputModalOpen, setIsDateInputModalOpen] = useState(false);
    const [isCreateCalendarModalOpen, setIsCreateCalendarModalOpen] = useState(false);
    const [isAnalyticsModalOpen, setIsAnalyticsModalOpen] = useState(false);

    useEffect(() => {
        fetchCalendars();
    }, [user]);

    useEffect(() => {
        if (selectedCalendarId) {
            fetchEvents(selectedCalendarId);
        }
    }, [selectedCalendarId, currentDate]);

    const fetchCalendars = async () => {
        try {
            let cals = await CalendarService.getCalendars();

            // Auto-create default calendar if none exist
            if (cals.length === 0) {
                console.log("No calendars found, creating default 'Personal' calendar...");
                const defaultCal = await CalendarService.createCalendar({
                    name: "Personal",
                    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
                });
                cals = [defaultCal];
            }

            setCalendars(cals);

            // Select the first one if nothing selected
            if (cals.length > 0 && !selectedCalendarId) {
                setSelectedCalendarId(cals[0].id);
            }
        } catch (err) {
            console.error("Failed to load calendars", err);
        }
    };

    const fetchEvents = async (calId: number) => {
        try {
            const evts = await EventService.getEvents(calId);
            console.log("Fetched events for calendar", calId, ":", evts);
            setEvents(evts);
        } catch (err) {
            console.error("Failed to load events", err);
        }
    };

    const handleNavigate = (action: 'PREV' | 'NEXT' | 'TODAY') => {
        if (action === 'PREV') setCurrentDate(subMonths(currentDate, 1));
        if (action === 'NEXT') setCurrentDate(addMonths(currentDate, 1));
        if (action === 'TODAY') setCurrentDate(new Date());
    };

    const handleCreateEvent = () => {
        setSelectedEvent(null); // Ensure fresh create
        setIsCreateModalOpen(true);
    };

    const handleDeleteCalendar = async () => {
        if (!selectedCalendarId) return;
        if (!window.confirm("Are you sure you want to delete this calendar? All events will be lost.")) return;

        try {
            await CalendarService.deleteCalendar(selectedCalendarId);
            // Refresh list
            const cals = await CalendarService.getCalendars();
            setCalendars(cals);
            if (cals.length > 0) {
                setSelectedCalendarId(cals[0].id);
            } else {
                setSelectedCalendarId(null);
                // Optionally auto-create personal again or leave empty
                fetchCalendars(); // reusing the auto-create logic
            }
        } catch (err) {
            console.error("Failed to delete calendar", err);
            alert("Failed to delete calendar");
        }
    };

    const handleViewDayEvents = () => {
        setIsDateInputModalOpen(true);
    };

    const handleCreateCalendarSubmit = async (name: string, timezone: string) => {
        try {
            // Pass timezone to service
            await CalendarService.createCalendar({ name, timezone });
            await fetchCalendars();
            setIsCreateCalendarModalOpen(false);
        } catch (err: any) {
            console.error("Error creating calendar:", err);
            alert("Error creating calendar: " + (err.response?.data?.message || err.message));
        }
    };

    const handleDateInputSubmit = (dateStr: string) => {
        const selectedDate = new Date(dateStr + 'T00:00:00'); // Ensure local time parsing
        setCurrentDate(selectedDate);
        setIsDateInputModalOpen(false);
        setIsViewDayModalOpen(true);
    };

    const handleSaveEvent = async (eventData: any) => {
        console.log("handleSaveEvent called with:", eventData);
        if (!selectedCalendarId) {
            console.error("No selectedCalendarId!");
            alert("No calendar selected!");
            return;
        }

        try {
            // FIX: Send simpler string format acceptable by LocalDateTime (YYYY-MM-DDTHH:mm:ss)
            // without the 'Z' or timezone offset, assuming backend treats it as "calendar time"
            const startDateTime = `${eventData.date}T${eventData.startTime}:00`;
            const endDateTime = `${eventData.date}T${eventData.endTime}:00`;

            const payload = {
                subject: eventData.subject,
                calendarId: selectedCalendarId,
                startTime: startDateTime, // Send strict ISO local string
                endTime: endDateTime,     // Send strict ISO local string
                description: eventData.description,
                location: eventData.location,
                recurrence: eventData.recurrence
                // isAllDay: eventData.isAllDay // Backend ignores this for now, but we'll include it.
            };

            console.log("Sending event payload:", payload);

            let result;
            if (eventData.id) {
                result = await EventService.updateEvent(eventData.id, payload);
                console.log("Event updated successfully:", result);
            } else {
                result = await EventService.createEvent(selectedCalendarId, payload);
                console.log("Event created successfully:", result);
            }

            setIsCreateModalOpen(false);
            setSelectedEvent(null); // Clear selection
            fetchEvents(selectedCalendarId);
        } catch (err: any) {
            console.error("Error in handleSaveEvent:", err);
            alert("Error saving event: " + (err.response?.data?.message || err.message));
        }
    };

    // Convert to BigCalendar format with strict parsing
    // IMPORTANT: react-big-calendar expects Javascript Date objects
    const calendarEvents = events.map(evt => {
        const start = new Date(evt.startTime);
        const end = new Date(evt.endTime);

        console.log(`Mapping event: "${evt.subject}" | Start: ${start.toLocaleString()} | CalendarID: ${evt.calendarId}`);

        return {
            title: evt.subject,
            start: start,
            end: end,
            resource: evt
        };
    });

    const components = React.useMemo(() => ({
        month: {
            dateHeader: ({ date, label }: any) => {
                const dateStr = date.toDateString();
                const isToday = new Date().toDateString() === dateStr;


                return (
                    <div className={`rbc-date-cell-content ${isToday ? 'rbc-now' : ''}`}>
                        <span className="rbc-button-link">
                            {label}
                        </span>
                    </div>
                );
            }
        }
    }), [events]);

    const currentCalendar = calendars.find(c => c.id === selectedCalendarId);

    // Clicking a date slot usually sets the current date 
    const handleSelectSlot = (slotInfo: any) => {
        setCurrentDate(slotInfo.start);
    };

    return (
        <div className="h-screen flex flex-col bg-[#F8F8F8] font-sans text-[#1D1D1F]">
            {/* Top Panel */}
            <div className="bg-white border-b border-[#E5E5EA] px-8 py-4 flex flex-col gap-4">
                {/* Navigation Row */}
                <div className="flex justify-center items-center gap-6 relative">
                    <button
                        onClick={() => handleNavigate('PREV')}
                        className="text-3xl text-[#007AFF] hover:text-[#0051A8] transition-colors pb-1"
                    >
                        ‹
                    </button>
                    <h2 className="text-2xl font-bold min-w-[200px] text-center">
                        {format(currentDate, 'MMMM yyyy')}
                    </h2>
                    <button
                        onClick={() => handleNavigate('NEXT')}
                        className="text-3xl text-[#007AFF] hover:text-[#0051A8] transition-colors pb-1"
                    >
                        ›
                    </button>

                    <button
                        onClick={() => handleNavigate('TODAY')}
                        className="absolute right-0 text-sm text-[#007AFF] border border-[#E5E5EA] bg-white px-3 py-1.5 rounded hover:bg-gray-50 transition-colors"
                    >
                        Today
                    </button>
                </div>

                {/* Selector Row */}
                <div className="flex items-center gap-3">
                    <label className="text-sm text-[#8E8E93]">Calendar:</label>
                    <select
                        className="bg-white border border-[#E5E5EA] rounded px-3 py-1 text-sm outline-none focus:border-[#007AFF]"
                        value={selectedCalendarId || ''}
                        onChange={(e) => setSelectedCalendarId(Number(e.target.value))}
                    >
                        {calendars.map(cal => (
                            <option key={cal.id} value={cal.id}>{cal.name} (id: {cal.id})</option>
                        ))}
                    </select>
                    <span className="text-xs text-[#8E8E93]">
                        ({currentCalendar?.timezone || 'UTC'})
                    </span>

                    <div className="flex-1"></div>

                    <GoogleCalendarSync onSyncComplete={() => {
                        fetchCalendars();
                        if (selectedCalendarId) fetchEvents(selectedCalendarId);
                    }} />

                    <button
                        onClick={handleDeleteCalendar}
                        className="text-sm text-[#FF3B30] border border-[#E5E5EA] bg-white px-2 py-1 rounded hover:bg-gray-50 ml-1"
                        title="Delete selected calendar"
                    >
                        🗑️
                    </button>
                    <button
                        onClick={() => setIsCreateCalendarModalOpen(true)}
                        className="text-sm text-[#007AFF] border border-[#E5E5EA] bg-white px-3 py-1 rounded hover:bg-gray-50 ml-2"
                    >
                        + New Calendar
                    </button>
                </div>
            </div>

            {/* Main Calendar Grid */}
            <div className="flex-1 p-8 overflow-hidden">
                <div className="bg-white h-full rounded border border-[#E5E5EA] shadow-sm p-4">
                    <BigCalendar
                        localizer={localizer}
                        events={calendarEvents}
                        date={currentDate}
                        onNavigate={date => setCurrentDate(date)}
                        view={view}
                        onView={setView}
                        components={components}
                        toolbar={false} // Hiding default toolbar to use our custom Top Panel
                        startAccessor="start"
                        endAccessor="end"
                        className="apple-calendar"
                        style={{ height: '75vh' }}
                        selectable
                        onSelectSlot={handleSelectSlot}
                        onSelectEvent={(event) => {
                            setSelectedEvent(event);
                            setIsCreateModalOpen(true);
                        }}
                    />
                </div>
            </div>

            {/* Bottom Panel */}
            <div className="bg-white border-t border-[#E5E5EA] p-4 flex justify-center gap-4">
                <button
                    onClick={handleCreateEvent}
                    className="bg-[#007AFF] text-white px-4 py-2 rounded font-bold text-sm hover:bg-[#0051A8] transition-colors shadow-sm"
                >
                    Create Event
                </button>
                <button
                    onClick={handleViewDayEvents}
                    className="bg-white text-[#007AFF] border border-[#E5E5EA] px-4 py-2 rounded text-sm hover:bg-gray-50"
                >
                    View Day Events
                </button>
                <button
                    onClick={() => setIsAnalyticsModalOpen(true)}
                    className="bg-white text-[#007AFF] border border-[#E5E5EA] px-4 py-2 rounded text-sm hover:bg-gray-50"
                >
                    Detailed Analytics
                </button>
                <button
                    onClick={async () => {
                        if (!selectedCalendarId) {
                            alert("Please select a calendar to export.");
                            return;
                        }
                        try {
                            const blob = await CalendarService.exportCalendar(selectedCalendarId);
                            const url = window.URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            a.href = url;
                            a.download = `calendar_${selectedCalendarId}.ics`;
                            document.body.appendChild(a);
                            a.click();
                            window.URL.revokeObjectURL(url);
                            document.body.removeChild(a);
                        } catch (err) {
                            console.error("Export failed", err);
                            alert("Failed to export calendar.");
                        }
                    }}
                    className="bg-white text-[#007AFF] border border-[#E5E5EA] px-4 py-2 rounded text-sm hover:bg-gray-50"
                >
                    Export Calendar
                </button>
            </div>

            {/* Modals */}
            <CreateEventModal
                isOpen={isCreateModalOpen}
                onClose={() => {
                    setIsCreateModalOpen(false);
                    setSelectedEvent(null);
                }}
                onSave={handleSaveEvent}
                initialEvent={selectedEvent}
            />
            <DateInputModal
                isOpen={isDateInputModalOpen}
                onClose={() => setIsDateInputModalOpen(false)}
                onSubmit={handleDateInputSubmit}
                initialDate={currentDate}
            />
            <ViewDayEventsModal
                isOpen={isViewDayModalOpen}
                onClose={() => setIsViewDayModalOpen(false)}
                currentDate={currentDate}
                events={events} // Pass raw events for filtering
            />
            <CreateCalendarModal
                isOpen={isCreateCalendarModalOpen}
                onClose={() => setIsCreateCalendarModalOpen(false)}
                onSubmit={handleCreateCalendarSubmit}
            />
            <AnalyticsModal
                isOpen={isAnalyticsModalOpen}
                onClose={() => setIsAnalyticsModalOpen(false)}
            />
        </div >
    );
};

export default Dashboard;
