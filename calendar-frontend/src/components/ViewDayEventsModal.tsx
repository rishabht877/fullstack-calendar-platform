import React from 'react';
import { format } from 'date-fns';
import { type CalendarEvent } from '../types';

interface ViewDayEventsModalProps {
    isOpen: boolean;
    onClose: () => void;
    currentDate: Date;
    events: CalendarEvent[];
}

const ViewDayEventsModal: React.FC<ViewDayEventsModalProps> = ({ isOpen, onClose, currentDate, events }) => {
    if (!isOpen) return null;

    // Filter events for the selected day
    // Note: strict comparison of year-month-day string
    const dateStr = format(currentDate, 'yyyy-MM-dd');
    const dayEvents = events.filter(evt => {
        // Handle ISO strings, take the YYYY-MM-DD part
        const evtDate = evt.startTime.split('T')[0];
        return evtDate === dateStr;
    });

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center font-sans">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/20"
                onClick={onClose}
            ></div>

            {/* Modal Window */}
            <div className="relative z-10 w-[400px] bg-white rounded-lg shadow-2xl border border-[#D1D1D1] overflow-hidden flex flex-col max-h-[60vh]">

                {/* Header */}
                <div className="h-10 border-b border-[#E5E5EA] flex items-center justify-between px-4 bg-[#F9F9F9]">
                    <div className="font-semibold text-sm text-[#1D1D1F]">
                        {format(currentDate, 'EEEE, MMMM d, yyyy')}
                    </div>
                    <button onClick={onClose} className="text-[#8E8E93] hover:text-[#1D1D1F] transition-colors">
                        ✕
                    </button>
                </div>

                {/* List Content */}
                <div className="flex-1 overflow-y-auto p-2">
                    {dayEvents.length === 0 ? (
                        <div className="text-center py-8 text-[#8E8E93] text-sm">
                            No events for this day.
                        </div>
                    ) : (
                        <div className="flex flex-col gap-2">
                            {dayEvents.map(evt => {
                                const start = new Date(evt.startTime);
                                const end = new Date(evt.endTime);
                                return (
                                    <div key={evt.id || Math.random()} className="bg-white border border-[#E5E5EA] rounded p-3 shadow-sm hover:border-[#007AFF] transition-colors">
                                        <div className="flex justify-between items-start mb-1">
                                            <div className="font-semibold text-[#1D1D1F] text-sm">{evt.subject}</div>
                                            {!evt.isAllDay && (
                                                <div className="text-xs text-[#8E8E93]">
                                                    {format(start, 'HH:mm')} - {format(end, 'HH:mm')}
                                                </div>
                                            )}
                                        </div>
                                        {evt.isAllDay && (
                                            <span className="inline-block bg-[#E5E5EA] text-[#1D1D1F] text-[10px] px-1.5 py-0.5 rounded font-medium mb-1">
                                                All Day
                                            </span>
                                        )}
                                        {evt.location && (
                                            <div className="text-xs text-[#8E8E93] flex items-center gap-1">
                                                📍 {evt.location}
                                            </div>
                                        )}
                                        {evt.description && (
                                            <div className="text-xs text-[#333] mt-1 line-clamp-2">
                                                {evt.description}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="p-3 border-t border-[#E5E5EA] bg-[#F9F9F9] flex justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-1.5 bg-white border border-[#C6C6C6] rounded shadow-sm text-xs font-medium hover:bg-[#F3F3F3]"
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ViewDayEventsModal;
