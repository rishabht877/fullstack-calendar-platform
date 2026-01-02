import React, { useEffect, useState } from 'react';
import { AnalyticsService } from '../services/data.service';
import { type Analytics } from '../types';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
} from 'chart.js';
import { Bar, Pie } from 'react-chartjs-2';

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

interface AnalyticsModalProps {
    isOpen: boolean;
    onClose: () => void;
}

const AnalyticsModal: React.FC<AnalyticsModalProps> = ({ isOpen, onClose }) => {
    const [data, setData] = useState<Analytics | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (isOpen) {
            setLoading(true);
            AnalyticsService.getAnalytics()
                .then(d => {
                    setData(d);
                    setLoading(false);
                })
                .catch(err => {
                    console.error("Failed to load analytics", err);
                    setLoading(false);
                });
        }
    }, [isOpen]);

    if (!isOpen) return null;

    if (loading || !data) {
        return (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                <div className="bg-white p-6 rounded-lg">Loading Analytics...</div>
            </div>
        );
    }

    const weekDayOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

    // Sort weekday data correctly
    const weekdayLabels = weekDayOrder;
    const weekdayData = weekDayOrder.map(day => data.eventsByWeekday?.[day] || 0);

    const weekdayChartData = {
        labels: weekdayLabels.map(d => d.substring(0, 3)),
        datasets: [
            {
                label: 'Events by Day',
                data: weekdayData,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1,
            },
        ],
    };

    const statusChartData = {
        labels: Object.keys(data.eventsByStatus),
        datasets: [
            {
                label: '# of Events',
                data: Object.values(data.eventsByStatus),
                backgroundColor: [
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(255, 99, 132, 0.2)',
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(255, 99, 132, 1)',
                ],
                borderWidth: 1,
            },
        ],
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center pt-10 pb-10 font-sans">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose}></div>
            <div className="relative z-10 w-[90%] max-w-[1000px] h-[90vh] bg-[#F5F5F7] rounded-xl shadow-2xl border border-[#D1D1D1] flex flex-col overflow-hidden">

                {/* Header */}
                <div className="h-[40px] bg-white border-b border-[#E5E5EA] flex items-center justify-between px-4 select-none">
                    <div className="font-semibold text-lg text-[#1D1D1F]">
                        Analytics Dashboard
                    </div>
                    <button onClick={onClose} className="text-[#007AFF] hover:bg-gray-100 px-3 py-1 rounded">
                        Close
                    </button>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-6">

                    {/* Top Stats Cards */}
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <StatCard title="Total Events" value={data.totalEvents} />
                        <StatCard title="Upcoming" value={data.upcomingEvents} />
                        <StatCard title="This Month" value={data.monthEvents} />
                        <StatCard title="This Week" value={data.weekEvents} />
                        <StatCard title="Avg Events/Day" value={data.averageEventsPerDay.toFixed(1)} />
                        <StatCard title="Busiest Day" value={data.busiestDayOfWeek.substring(0, 3)} highlight />
                        <StatCard title="Least Busy" value={data.leastBusyDayOfWeek.substring(0, 3)} />
                        <StatCard title="Online %" value={data.onlinePercentage.toFixed(1) + '%'} />
                    </div>

                    {/* Charts Row */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="bg-white p-4 rounded-xl shadow-sm border border-[#E5E5EA]">
                            <h3 className="text-sm font-semibold text-gray-500 mb-4 uppercase tracking-wide">Weekly Distribution</h3>
                            <div className="h-[250px] flex items-center justify-center">
                                <Bar data={weekdayChartData} options={{ maintainAspectRatio: false }} />
                            </div>
                        </div>
                        <div className="bg-white p-4 rounded-xl shadow-sm border border-[#E5E5EA]">
                            <h3 className="text-sm font-semibold text-gray-500 mb-4 uppercase tracking-wide">Status Breakdown</h3>
                            <div className="h-[250px] flex items-center justify-center">
                                <Pie data={statusChartData} options={{ maintainAspectRatio: false }} />
                            </div>
                        </div>
                    </div>

                    {/* Subject Breakdown List */}
                    <div className="bg-white p-4 rounded-xl shadow-sm border border-[#E5E5EA]">
                        <h3 className="text-sm font-semibold text-gray-500 mb-4 uppercase tracking-wide">Top Subjects</h3>
                        <div className="divide-y divide-gray-100">
                            {Object.entries(data.eventsBySubject)
                                .sort(([, a], [, b]) => b - a)
                                .slice(0, 5)
                                .map(([subject, count]) => (
                                    <div key={subject} className="py-2 flex justify-between text-sm">
                                        <span className="font-medium text-gray-700">{subject}</span>
                                        <span className="text-gray-500">{count} events</span>
                                    </div>
                                ))}
                        </div>
                    </div>

                </div>
            </div>
        </div>
    );
};

const StatCard = ({ title, value, highlight = false }: { title: string, value: string | number, highlight?: boolean }) => (
    <div className={`p-4 rounded-xl border ${highlight ? 'bg-blue-50 border-blue-100' : 'bg-white border-[#E5E5EA]'} shadow-sm`}>
        <div className="text-xs text-gray-500 uppercase font-semibold tracking-wider">{title}</div>
        <div className={`text-2xl font-bold mt-1 ${highlight ? 'text-blue-600' : 'text-[#1D1D1F]'}`}>{value}</div>
    </div>
);

export default AnalyticsModal;
