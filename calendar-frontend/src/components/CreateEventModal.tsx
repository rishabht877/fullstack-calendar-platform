import React, { useState } from 'react';
import RecurringEventModal, { type RecurrenceData } from './RecurringEventModal';

interface CreateEventModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (eventData: any) => void;
    onDelete?: (eventId: number) => void;
    initialEvent?: any; // For editing
}

const CreateEventModal: React.FC<CreateEventModalProps> = ({ isOpen, onClose, onSave, onDelete, initialEvent }) => {
    // Default form state
    const [subject, setSubject] = useState('');
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    const [startTime, setStartTime] = useState('09:00');
    const [endTime, setEndTime] = useState('10:00');
    const [location, setLocation] = useState('');
    const [description, setDescription] = useState('');
    const [isAllDay, setIsAllDay] = useState(false);
    const [isRecurring, setIsRecurring] = useState(false);
    const [eventType, setEventType] = useState<'in-person' | 'online'>('in-person');

    // Recurring Modal State
    const [showRecurringModal, setShowRecurringModal] = useState(false);
    const [recurrenceData, setRecurrenceData] = useState<RecurrenceData | null>(null);

    React.useEffect(() => {
        if (isOpen && initialEvent) {
            setSubject(initialEvent.subject || '');
            if (initialEvent.start) {
                const d = new Date(initialEvent.start);
                setDate(d.toISOString().split('T')[0]);
                setStartTime(d.toTimeString().substring(0, 5));
            }
            if (initialEvent.end) {
                const d = new Date(initialEvent.end);
                setEndTime(d.toTimeString().substring(0, 5));
            }

            let loc = initialEvent.resource?.location || '';
            // Check if location implies online
            if (loc.startsWith('[Online] ')) {
                setEventType('online');
                setLocation(loc.replace('[Online] ', ''));
            } else {
                setEventType('in-person');
                setLocation(loc);
            }

            setDescription(initialEvent.resource?.description || '');
            setIsAllDay(initialEvent.resource?.isAllDay || false);
            if (initialEvent.resource?.recurrence) {
                setIsRecurring(true);
                setRecurrenceData(initialEvent.resource.recurrence);
            }
        } else if (isOpen && !initialEvent) {
            // Reset if opening fresh
            setSubject('');
            const now = new Date();
            setDate(now.toISOString().split('T')[0]);
            setStartTime('09:00');
            setEndTime('10:00');
            setLocation('');
            setDescription('');
            setIsAllDay(false);
            setIsRecurring(false);
            setRecurrenceData(null);
            setEventType('in-person');
        }
    }, [isOpen, initialEvent]);

    if (!isOpen) return null;

    const handleSubmit = () => {
        if (!subject || !date) {
            alert('Subject and date are required!');
            return;
        }

        // Prepend [Online] to location if online type is selected
        const finalLocation = eventType === 'online' ? `[Online] ${location}` : location;

        const eventData = {
            subject,
            date,
            startTime,
            endTime,
            location: finalLocation,
            description,
            isAllDay,
            isRecurring,
            recurrence: isRecurring ? recurrenceData : null,
            id: initialEvent?.resource?.id // Pass ID if editing
        };

        onSave(eventData);
        resetForm();
    };

    const handleDelete = () => {
        if (initialEvent?.resource?.id && onDelete) {
            if (window.confirm("Are you sure you want to delete this event? This action cannot be undone.")) {
                onDelete(initialEvent.resource.id);
                onClose(); // Close modal immediately
            }
        }
    };

    const resetForm = () => {
        setSubject('');
        setLocation('');
        setDescription('');
        setIsRecurring(false);
        setRecurrenceData(null);
        setEventType('in-person');
    };

    const handleRecurringChange = (checked: boolean) => {
        if (checked) {
            setShowRecurringModal(true);
        } else {
            setIsRecurring(false);
            setRecurrenceData(null);
        }
    };

    const handleRecurringSubmit = (data: RecurrenceData) => {
        setRecurrenceData(data);
        setIsRecurring(true);
        setShowRecurringModal(false);
    };

    return (
        <>
            <div className="fixed inset-0 z-50 flex items-start pt-[100px] justify-center font-sans">
                {/* Backdrop */}
                <div
                    className="absolute inset-0 bg-black/20"
                    onClick={onClose}
                ></div>

                {/* Modal Window */}
                <div className="relative z-10 w-[500px] bg-[#ECECEC] rounded-lg shadow-2xl border border-[#BDBDBD] overflow-hidden flex flex-col box-border">

                    {/* Title Bar */}
                    <div className="h-[28px] bg-gradient-to-b from-[#EFEFEF] to-[#DCDCDC] border-b border-[#BDBDBD] flex items-center justify-center relative select-none">
                        <div className="flex space-x-2 absolute left-2.5">
                            <div className="w-3 h-3 rounded-full bg-[#FF5F57] border border-[#E0443E] shadow-sm cursor-pointer hover:brightness-90" onClick={onClose}></div>
                            <div className="w-3 h-3 rounded-full bg-[#FEBC2E] border border-[#D89E24] shadow-sm"></div>
                            <div className="w-3 h-3 rounded-full bg-[#28C840] border border-[#1AAB29] shadow-sm"></div>
                        </div>
                        <div className="font-semibold text-[13px] text-[#424242] shadow-white drop-shadow-sm">
                            {initialEvent ? 'Edit Event' : 'Create Event'}
                        </div>
                    </div>

                    {/* Content Area */}
                    <div className="p-5 flex flex-col gap-4 text-[13px] text-[#333]">

                        {/* Subject */}
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">Subject:</label>
                            <input
                                type="text"
                                className="w-full h-[22px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6]"
                                value={subject}
                                onChange={(e) => setSubject(e.target.value)}
                                autoFocus
                            />
                        </div>

                        {/* Event Type */}
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">Event Type:</label>
                            <div className="flex items-center space-x-4">
                                <label className="flex items-center space-x-1 cursor-pointer">
                                    <input
                                        type="radio"
                                        checked={eventType === 'in-person'}
                                        onChange={() => setEventType('in-person')}
                                        className="text-[#007AFF] focus:ring-0"
                                    />
                                    <span>In-Person</span>
                                </label>
                                <label className="flex items-center space-x-1 cursor-pointer">
                                    <input
                                        type="radio"
                                        checked={eventType === 'online'}
                                        onChange={() => setEventType('online')}
                                        className="text-[#007AFF] focus:ring-0"
                                    />
                                    <span>Online (Virtual)</span>
                                </label>
                            </div>
                        </div>

                        {/* Date */}
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">Date (YYYY-MM-DD):</label>
                            <input
                                type="text"
                                className="w-full h-[22px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6]"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                            />
                        </div>

                        {/* Start Time */}
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">Start Time (HH:MM):</label>
                            <input
                                type="text"
                                className={`w-full h-[22px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6] ${isAllDay ? 'opacity-50 cursor-not-allowed text-gray-400' : ''}`}
                                value={startTime}
                                onChange={(e) => setStartTime(e.target.value)}
                                disabled={isAllDay}
                            />
                        </div>

                        {/* End Time */}
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">End Time (HH:MM):</label>
                            <input
                                type="text"
                                className={`w-full h-[22px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6] ${isAllDay ? 'opacity-50 cursor-not-allowed text-gray-400' : ''}`}
                                value={endTime}
                                onChange={(e) => setEndTime(e.target.value)}
                                disabled={isAllDay}
                            />
                        </div>

                        {/* Location / Meeting Link */}
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">
                                {eventType === 'online' ? 'Meeting Link:' : 'Location:'}
                            </label>
                            <input
                                type="text"
                                className="w-full h-[22px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6]"
                                value={location}
                                onChange={(e) => setLocation(e.target.value)}
                                placeholder={eventType === 'online' ? 'https://zoom.us/...' : 'Office, Room 303, etc.'}
                            />
                        </div>

                        {/* Description */}
                        <div className="grid grid-cols-[140px_1fr] items-start">
                            <label className="text-right pr-4 font-medium mt-1">Description:</label>
                            <textarea
                                className="w-full h-[60px] px-1.5 py-1 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6] resize-none"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            />
                        </div>

                        {/* Checkboxes */}
                        <div className="grid grid-cols-2 mt-2 ml-[140px]">
                            <label className="flex items-center space-x-2 cursor-pointer select-none">
                                <input
                                    type="checkbox"
                                    className="rounded-sm border-gray-400 text-[#007AFF] focus:ring-0 focus:ring-offset-0 h-3.5 w-3.5"
                                    checked={isAllDay}
                                    onChange={(e) => setIsAllDay(e.target.checked)}
                                />
                                <span>All-day event</span>
                            </label>
                            <label className="flex items-center space-x-2 cursor-pointer select-none">
                                <input
                                    type="checkbox"
                                    className="rounded-sm border-gray-400 text-[#007AFF] focus:ring-0 focus:ring-offset-0 h-3.5 w-3.5"
                                    checked={isRecurring}
                                    onChange={(e) => handleRecurringChange(e.target.checked)}
                                />
                                <span>Recurring event</span>
                            </label>
                        </div>
                        {isRecurring && recurrenceData && (
                            <div className="ml-[140px] text-xs text-blue-600">
                                Repeats {recurrenceData.pattern} {recurrenceData.pattern === 'WEEKLY' && recurrenceData.daysOfWeek.length > 0 ? `on ${recurrenceData.daysOfWeek.map(d => d.substring(0, 3)).join(',')}` : ''}  ({recurrenceData.type === 'COUNT' ? `${recurrenceData.occurrences} times` : `Until ${recurrenceData.untilDate}`})
                            </div>
                        )}

                    </div>

                    {/* Footer Buttons */}
                    <div className="px-5 pb-5 flex gap-4 justify-between mt-2">
                        {/* Delete Button (Only if editing) */}
                        {initialEvent && (
                            <button
                                onClick={handleDelete}
                                className="h-[26px] px-4 bg-red-50 border border-red-200 rounded shadow-sm text-[13px] text-red-600 font-medium hover:bg-red-100 active:bg-red-200 transition-all"
                            >
                                Delete
                            </button>
                        )}

                        <div className="flex gap-4 ml-auto">
                            <button
                                onClick={onClose}
                                className="h-[26px] px-4 bg-white border border-[#BDBDBD] rounded shadow-sm text-[13px] text-black font-normal hover:bg-[#F6F6F6] active:bg-[#ECECEC] active:shadow-inner transition-all bg-gradient-to-b from-white to-[#F6F6F6]"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSubmit}
                                className="h-[26px] px-4 bg-blue-500 border border-blue-600 rounded shadow-sm text-[13px] text-white font-medium hover:bg-blue-600 active:bg-blue-700 active:shadow-inner transition-all bg-gradient-to-b from-blue-400 to-blue-600"
                            >
                                {initialEvent ? 'Update' : 'Create'}
                            </button>
                        </div>
                    </div>

                </div>
            </div>

            <RecurringEventModal
                isOpen={showRecurringModal}
                onClose={() => setShowRecurringModal(false)}
                onSubmit={handleRecurringSubmit}
            />
        </>
    );
};

export default CreateEventModal;
