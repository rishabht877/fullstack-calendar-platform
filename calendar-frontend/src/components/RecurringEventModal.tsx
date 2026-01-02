import React, { useState } from 'react';

interface RecurringEventModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (recurrenceData: RecurrenceData) => void;
}

export interface RecurrenceData {
    pattern: string; // DAILY, WEEKLY, MONTHLY, YEARLY
    interval: number;
    daysOfWeek: string[];
    occurrences?: number;
    untilDate?: string;
    type: 'COUNT' | 'DATE';
}

const RecurringEventModal: React.FC<RecurringEventModalProps> = ({ isOpen, onClose, onSubmit }) => {
    const [pattern, setPattern] = useState('WEEKLY');
    const [interval, setInterval] = useState(1);
    const [selectedDays, setSelectedDays] = useState<string[]>(['MONDAY', 'WEDNESDAY', 'FRIDAY']);
    const [type, setType] = useState<'COUNT' | 'DATE'>('COUNT');
    const [occurrences, setOccurrences] = useState('5');
    const [untilDate, setUntilDate] = useState('');

    if (!isOpen) return null;

    const daysOptions = [
        { label: 'S', value: 'SUNDAY' },
        { label: 'M', value: 'MONDAY' },
        { label: 'T', value: 'TUESDAY' },
        { label: 'W', value: 'WEDNESDAY' },
        { label: 'T', value: 'THURSDAY' },
        { label: 'F', value: 'FRIDAY' },
        { label: 'S', value: 'SATURDAY' },
    ];

    const toggleDay = (day: string) => {
        if (selectedDays.includes(day)) {
            setSelectedDays(selectedDays.filter(d => d !== day));
        } else {
            setSelectedDays([...selectedDays, day]);
        }
    };

    const handleSubmit = () => {
        const data: RecurrenceData = {
            pattern,
            interval,
            daysOfWeek: pattern === 'WEEKLY' ? selectedDays : [],
            type
        };

        if (type === 'COUNT') {
            if (!occurrences) {
                alert("Number of occurrences is required");
                return;
            }
            data.occurrences = parseInt(occurrences, 10);
        } else {
            if (!untilDate) {
                alert("Until date is required");
                return;
            }
            data.untilDate = untilDate;
        }

        onSubmit(data);
    };

    return (
        <div className="fixed inset-0 z-[60] flex items-start pt-[120px] justify-center font-sans">
            <div className="absolute inset-0 bg-transparent" onClick={onClose}></div>
            <div className="relative z-10 w-[450px] bg-[#ECECEC] rounded-lg shadow-2xl border border-[#BDBDBD] flex flex-col box-border">

                {/* Header */}
                <div className="h-[28px] bg-gradient-to-b from-[#EFEFEF] to-[#DCDCDC] border-b border-[#BDBDBD] flex items-center justify-center relative select-none rounded-t-lg">
                    <div className="flex space-x-2 absolute left-2.5">
                        <div className="w-3 h-3 rounded-full bg-[#FF5F57] border border-[#E0443E] shadow-sm cursor-pointer hover:brightness-90" onClick={onClose}></div>
                        <div className="w-3 h-3 rounded-full bg-[#FEBC2E] border border-[#D89E24] shadow-sm"></div>
                        <div className="w-3 h-3 rounded-full bg-[#28C840] border border-[#1AAB29] shadow-sm"></div>
                    </div>
                    <div className="font-semibold text-[13px] text-[#424242]">Recurring Event Details</div>
                </div>

                <div className="p-5 flex flex-col gap-4 text-[13px] text-[#333]">

                    {/* Frequency */}
                    <div className="grid grid-cols-[140px_1fr] items-center">
                        <label className="text-right pr-4 font-medium">Frequency:</label>
                        <select
                            className="w-full h-[26px] px-1 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none rounded-sm"
                            value={pattern}
                            onChange={(e) => setPattern(e.target.value)}
                        >
                            <option value="DAILY">Daily</option>
                            <option value="WEEKLY">Weekly</option>
                            <option value="MONTHLY">Monthly</option>
                            <option value="YEARLY">Yearly</option>
                        </select>
                    </div>

                    {/* Interval */}
                    <div className="grid grid-cols-[140px_1fr] items-center">
                        <label className="text-right pr-4 font-medium">Every:</label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                min="1"
                                className="w-[60px] h-[26px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none rounded-sm"
                                value={interval}
                                onChange={(e) => setInterval(parseInt(e.target.value) || 1)}
                            />
                            <span>{pattern.toLowerCase().replace('ly', '(s)')}</span>
                        </div>
                    </div>

                    {/* Weekly Days */}
                    {pattern === 'WEEKLY' && (
                        <div className="grid grid-cols-[140px_1fr] items-start">
                            <label className="text-right pr-4 font-medium pt-1">On days:</label>
                            <div className="flex gap-1.5 flex-wrap">
                                {daysOptions.map(day => (
                                    <button
                                        key={day.value}
                                        onClick={() => toggleDay(day.value)}
                                        className={`w-[26px] h-[26px] rounded-full text-xs font-medium border transition-colors ${selectedDays.includes(day.value)
                                                ? 'bg-[#007AFF] text-white border-[#007AFF]'
                                                : 'bg-white text-[#333] border-[#BDBDBD] hover:bg-gray-50'
                                            }`}
                                    >
                                        {day.label}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="border-t border-gray-300 my-1"></div>

                    {/* End Condition */}
                    <div className="grid grid-cols-[140px_1fr] items-center">
                        <label className="text-right pr-4 font-medium">Ends:</label>
                        <select
                            className="w-full h-[26px] px-1 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none rounded-sm"
                            value={type}
                            onChange={(e) => setType(e.target.value as 'COUNT' | 'DATE')}
                        >
                            <option value="COUNT">After number of occurrences</option>
                            <option value="DATE">On date</option>
                        </select>
                    </div>

                    {type === 'COUNT' ? (
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">Occurrences:</label>
                            <input
                                type="number"
                                min="1"
                                className="w-full h-[26px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none rounded-sm"
                                value={occurrences}
                                onChange={(e) => setOccurrences(e.target.value)}
                            />
                        </div>
                    ) : (
                        <div className="grid grid-cols-[140px_1fr] items-center">
                            <label className="text-right pr-4 font-medium">Date:</label>
                            <input
                                type="date"
                                className="w-full h-[26px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none rounded-sm"
                                value={untilDate}
                                onChange={(e) => setUntilDate(e.target.value)}
                            />
                        </div>
                    )}

                </div>

                <div className="px-5 pb-5 flex gap-4 mt-2">
                    <button onClick={handleSubmit} className="flex-1 h-[28px] bg-[#007AFF] text-white rounded shadow-sm text-[13px] font-medium hover:bg-[#0051A8]">
                        Apply
                    </button>
                    <button onClick={onClose} className="flex-1 h-[28px] bg-white border border-[#BDBDBD] text-black rounded shadow-sm text-[13px] hover:bg-gray-50">
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RecurringEventModal;
