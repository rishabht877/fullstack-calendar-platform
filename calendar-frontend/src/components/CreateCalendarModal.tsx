import React, { useState } from 'react';

interface CreateCalendarModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (name: string, timezone: string) => void;
}

const CreateCalendarModal: React.FC<CreateCalendarModalProps> = ({ isOpen, onClose, onSubmit }) => {
    const [name, setName] = useState('');
    const [timezone, setTimezone] = useState(Intl.DateTimeFormat().resolvedOptions().timeZone);

    if (!isOpen) return null;

    const handleSubmit = () => {
        if (!name.trim()) {
            alert("Calendar name is required");
            return;
        }
        onSubmit(name, timezone);
        setName('');
    };

    const timezones = [
        "UTC",
        "America/New_York",
        "America/Los_Angeles",
        "America/Chicago",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Kolkata",
        "Australia/Sydney",
        // Add more as needed or use a library
    ];

    // Ensure current timezone is in the list
    if (!timezones.includes(Intl.DateTimeFormat().resolvedOptions().timeZone)) {
        timezones.push(Intl.DateTimeFormat().resolvedOptions().timeZone);
    }
    timezones.sort();

    return (
        <div className="fixed inset-0 z-50 flex items-start pt-[100px] justify-center font-sans">
            <div className="absolute inset-0 bg-black/20" onClick={onClose}></div>
            <div className="relative z-10 w-[400px] bg-[#ECECEC] rounded-lg shadow-2xl border border-[#BDBDBD] flex flex-col box-border overflow-hidden">

                {/* Header */}
                <div className="h-[28px] bg-gradient-to-b from-[#EFEFEF] to-[#DCDCDC] border-b border-[#BDBDBD] flex items-center justify-center relative select-none">
                    <div className="flex space-x-2 absolute left-2.5">
                        <div className="w-3 h-3 rounded-full bg-[#FF5F57] border border-[#E0443E] shadow-sm cursor-pointer hover:brightness-90" onClick={onClose}></div>
                        <div className="w-3 h-3 rounded-full bg-[#FEBC2E] border border-[#D89E24] shadow-sm"></div>
                        <div className="w-3 h-3 rounded-full bg-[#28C840] border border-[#1AAB29] shadow-sm"></div>
                    </div>
                    <div className="font-semibold text-[13px] text-[#424242] shadow-white drop-shadow-sm">
                        New Calendar
                    </div>
                </div>

                <div className="p-5 flex flex-col gap-4 text-[13px] text-[#333]">
                    {/* Name */}
                    <div className="grid grid-cols-[100px_1fr] items-center">
                        <label className="text-right pr-4 font-medium">Name:</label>
                        <input
                            type="text"
                            className="w-full h-[22px] px-1.5 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none focus:ring-2 focus:ring-[#62A8F6] focus:border-[#62A8F6]"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            autoFocus
                        />
                    </div>

                    {/* Timezone */}
                    <div className="grid grid-cols-[100px_1fr] items-center">
                        <label className="text-right pr-4 font-medium">Timezone:</label>
                        <select
                            className="w-full h-[22px] px-1 border border-[#BDBDBD] bg-white text-[13px] shadow-sm focus:outline-none"
                            value={timezone}
                            onChange={(e) => setTimezone(e.target.value)}
                        >
                            {timezones.map(tz => (
                                <option key={tz} value={tz}>{tz}</option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="px-5 pb-5 flex gap-4 mt-2">
                    <button onClick={handleSubmit} className="flex-1 h-[26px] bg-[#007AFF] text-white rounded shadow-sm text-[13px] font-medium hover:bg-[#0051A8]">
                        Create Calendar
                    </button>
                    <button onClick={onClose} className="flex-1 h-[26px] bg-white border border-[#BDBDBD] text-black rounded shadow-sm text-[13px] hover:bg-gray-50">
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CreateCalendarModal;
