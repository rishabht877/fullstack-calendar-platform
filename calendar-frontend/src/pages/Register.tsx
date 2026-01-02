import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { AuthService } from '../services/auth.service';

const Register: React.FC = () => {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const [error, setError] = React.useState('');
    const [isSubmitting, setIsSubmitting] = React.useState(false);

    const onSubmit = async (data: any) => {
        setError('');
        setIsSubmitting(true);
        try {
            console.log("Attempting registration for:", data.username);
            await AuthService.register(data.username, data.email, data.password);
            alert("Registration successful! Please login.");
            navigate('/login');
        } catch (err: any) {
            console.error("Registration error:", err);
            setError(err.response?.data?.message || 'Registration failed. Please check your connection.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-blue-50">
            <div className="px-8 py-6 mt-4 text-left bg-white shadow-lg rounded-xl md:w-1/3">
                <h3 className="text-2xl font-bold text-center text-primary">Create Account</h3>
                {error && <div className="text-red-500 text-sm mt-2 text-center p-2 bg-red-50 rounded">{error}</div>}
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div className="mt-4">
                        <label className="block">Username</label>
                        <input
                            type="text"
                            className={`w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-primary ${errors.username ? 'border-red-500' : ''}`}
                            {...register('username', { required: "Username is required", minLength: { value: 3, message: "Min 3 characters" } })}
                        />
                        {errors.username && <span className="text-red-500 text-xs text-wrap">{(errors.username as any).message}</span>}
                    </div>
                    <div className="mt-4">
                        <label className="block">Email</label>
                        <input
                            type="email"
                            className={`w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-primary ${errors.email ? 'border-red-500' : ''}`}
                            {...register('email', { required: "Email is required" })}
                        />
                        {errors.email && <span className="text-red-500 text-xs text-wrap">{(errors.email as any).message}</span>}
                    </div>
                    <div className="mt-4">
                        <label className="block">Password</label>
                        <input
                            type="password"
                            className={`w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-primary ${errors.password ? 'border-red-500' : ''}`}
                            {...register('password', { required: "Password is required", minLength: { value: 6, message: "Min 6 characters" } })}
                        />
                        {errors.password && <span className="text-red-500 text-xs text-wrap">{(errors.password as any).message}</span>}
                    </div>
                    <div className="flex items-baseline justify-between">
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className={`px-6 py-2 mt-4 text-white rounded-lg transition-colors ${isSubmitting ? 'bg-gray-400 cursor-not-allowed' : 'bg-primary hover:bg-blue-600'}`}
                        >
                            {isSubmitting ? 'Registering...' : 'Register'}
                        </button>
                        <Link to="/login" className="text-sm text-blue-600 hover:underline">Already have an account?</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;
