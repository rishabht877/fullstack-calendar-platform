import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { AuthService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';

const Login: React.FC = () => {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const { login } = useAuth();
    const [error, setError] = React.useState('');

    const onSubmit = async (data: any) => {
        try {
            const response = await AuthService.login(data.username, data.password);
            login(response, response.token);
            navigate('/');
        } catch (err) {
            setError('Invalid username or password');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <div className="px-8 py-6 mt-4 text-left bg-white shadow-lg rounded-xl md:w-1/3">
                <h3 className="text-2xl font-bold text-center text-primary">Login to Calendar</h3>
                {error && <div className="text-red-500 text-sm mt-2 text-center">{error}</div>}
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div className="mt-4">
                        <label className="block" htmlFor="username">Username</label>
                        <input
                            type="text"
                            placeholder="Username"
                            className="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-primary"
                            {...register('username', { required: true })}
                        />
                        {errors.username && <span className="text-xs text-red-600">Username is required</span>}
                    </div>
                    <div className="mt-4">
                        <label className="block" htmlFor="password">Password</label>
                        <input
                            type="password"
                            placeholder="Password"
                            className="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-primary"
                            {...register('password', { required: true })}
                        />
                        {errors.password && <span className="text-xs text-red-600">Password is required</span>}
                    </div>
                    <div className="flex items-baseline justify-between">
                        <button className="px-6 py-2 mt-4 text-white bg-primary rounded-lg hover:bg-blue-600">
                            Login
                        </button>
                        <Link to="/register" className="text-sm text-blue-600 hover:underline">Register</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Login;
