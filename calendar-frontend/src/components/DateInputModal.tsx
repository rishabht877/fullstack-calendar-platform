import React, { useState, useEffect } from 'react';

interface DateInputModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (date: string) => void;
    initialDate?: Date;
}

const DateInputModal: React.FC<DateInputModalProps> = ({ isOpen, onClose, onSubmit, initialDate }) => {
    const [dateStr, setDateStr] = useState('');

    useEffect(() => {
        if (isOpen && initialDate) {
            setDateStr(initialDate.toISOString().split('T')[0]);
        }
    }, [isOpen, initialDate]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[60] flex items-start pt-[100px] justify-center font-sans">
            {/* Backdrop - lighter usage for native feel or invisible if desired, but user screenshot implies modal */}
            {/* Using transparent backdrop to simulate window-like behavior but blocking interaction */}
            <div className="absolute inset-0 bg-transparent" onClick={() => { /* prevent click through */ }}></div>

            {/* Modal Window - Precise Java Swing "Input" dialog replication */}
            <div className="relative z-10 w-[300px] bg-[#ECECEC] rounded-lg shadow-2xl border border-[#BDBDBD] flex flex-col box-border">

                {/* Title Bar - Native macOS style */}
                <div className="h-[28px] bg-gradient-to-b from-[#EFEFEF] to-[#DCDCDC] border-b border-[#BDBDBD] flex items-center justify-center relative select-none rounded-t-lg">
                    {/* Traffic Lights - Only Close button is active in dialogs usually */}
                    <div className="flex space-x-2 absolute left-2.5">
                        <div className="w-3 h-3 rounded-full bg-[#FF5F57] border border-[#E0443E] shadow-sm cursor-pointer hover:brightness-90" onClick={onClose}></div>
                        <div className="w-3 h-3 rounded-full bg-[#D1D1D1] border border-[#BDBDBD] shadow-sm"></div>
                        <div className="w-3 h-3 rounded-full bg-[#D1D1D1] border border-[#BDBDBD] shadow-sm"></div>
                    </div>
                    {/* Title */}
                    <div className="font-semibold text-[13px] text-[#424242] shadow-white drop-shadow-sm">
                        Input
                    </div>
                </div>

                {/* Content Area */}
                <div className="p-4 flex flex-row gap-4 items-start">
                    {/* Icon - Using a generic image placeholder or emoji to mimic the Java Duke icon */}
                    <div className="w-12 h-12 flex-shrink-0">
                        {/* Placeholder for Java icon - using an img if available or a similar emoji/icon */}
                        <div className="w-12 h-12 bg-white border border-[#BDBDBD] rounded shadow-sm flex items-center justify-center">
                            <span className="text-2xl">☕️</span>
                        </div>
                    </div>

                    <div className="flex flex-col gap-2 flex-1">
                        <label className="text-[13px] text-[#333]">Enter date (YYYY-MM-DD):</label>
                        <input
                            type="text"
                            className="w-full h-[24px] px-1.5 border border-[#62A8F6] ring-2 ring-[#62A8F6] ring-opacity-50 bg-white text-[13px] shadow-sm focus:outline-none"
                            value={dateStr}
                            onChange={(e) => setDateStr(e.target.value)}
                            autoFocus
                        />
                    </div>
                </div>

                {/* Footer Buttons */}
                <div className="px-4 pb-4 flex gap-3 justify-end mt-2">
                    <button
                        onClick={onClose}
                        className="min-w-[70px] h-[26px] bg-white border border-[#BDBDBD] rounded shadow-sm text-[13px] text-black font-normal hover:bg-[#F6F6F6] active:bg-[#ECECEC] active:shadow-inner transition-all bg-gradient-to-b from-white to-[#F6F6F6]"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={() => onSubmit(dateStr)}
                        className="min-w-[70px] h-[26px] bg-[#007AFF] border border-[#0062CC] rounded shadow-sm text-[13px] text-white font-normal hover:bg-[#0062CC] active:bg-[#0051A8] active:shadow-inner transition-all"
                    >
                        OK
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DateInputModal;
