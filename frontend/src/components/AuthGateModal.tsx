import React, { useState, useEffect } from 'react';
import { AuthSession, DemoProfile } from '../types';
import { authApi } from '../api/client';
import { X, LogIn, Sparkles, UserCheck, UserPlus } from 'lucide-react';

interface AuthGateModalProps {
  isOpen: boolean;
  onClose: () => void;
  demoProfiles: DemoProfile[];
  onAuthenticated: (session: AuthSession) => void;
  onOpenOnboarding: () => void;
}

export const AuthGateModal: React.FC<AuthGateModalProps> = ({
  isOpen,
  onClose,
  demoProfiles,
  onAuthenticated,
  onOpenOnboarding,
}) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setError(null);
      setPassword('');
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handlePasswordLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const session = await authApi.login(email, password);
      onAuthenticated(session);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDemoLogin = async (studentId: number) => {
    setSubmitting(true);
    setError(null);
    try {
      const session = await authApi.demoLogin(studentId);
      onAuthenticated(session);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Demo login failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-navy-950/85 backdrop-blur-sm">
      <div className="bg-navy-900 border border-navy-800 rounded-2xl w-full max-w-2xl p-6 shadow-2xl relative max-h-[90vh] overflow-y-auto">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-slate-400 hover:text-white"
          aria-label="Close sign-in modal"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-2.5 mb-5">
          <div className="w-9 h-9 rounded-xl bg-brand-violet/20 border border-brand-violet/30 flex items-center justify-center text-brand-violetLight">
            <LogIn className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Sign In to PathPilot AI</h2>
            <p className="text-xs text-slate-400">
              Start an authenticated HttpSession with your registered account or switch into a sandboxed demo persona
            </p>
          </div>
        </div>

        {error && (
          <div className="p-3 mb-4 bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs rounded-xl">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Standard Credential Login */}
          <form onSubmit={handlePasswordLogin} className="space-y-3.5 text-xs bg-navy-950/60 p-4 rounded-xl border border-navy-800">
            <h3 className="text-sm font-bold text-white flex items-center space-x-1.5">
              <LogIn className="w-3.5 h-3.5 text-brand-violetLight" />
              <span>Account Credentials</span>
            </h3>

            <div>
              <label className="block text-slate-300 font-semibold mb-1">Email Address</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="student@university.edu"
                className="w-full bg-navy-900 border border-navy-700 rounded-lg px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-violet"
              />
            </div>

            <div>
              <label className="block text-slate-300 font-semibold mb-1">Password</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                className="w-full bg-navy-900 border border-navy-700 rounded-lg px-3 py-2 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-violet"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-2.5 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white font-bold transition-all disabled:opacity-50"
            >
              {submitting ? 'Signing In...' : 'Sign In'}
            </button>

            <div className="pt-2 border-t border-navy-800 flex items-center justify-between">
              <span className="text-[11px] text-slate-400">New student?</span>
              <button
                type="button"
                onClick={() => {
                  onClose();
                  onOpenOnboarding();
                }}
                className="text-brand-teal hover:text-brand-tealLight font-bold flex items-center space-x-1"
              >
                <UserPlus className="w-3.5 h-3.5" />
                <span>Create Account</span>
              </button>
            </div>
          </form>

          {/* 1-Click Demo Personas */}
          <div className="space-y-3 text-xs bg-navy-950/60 p-4 rounded-xl border border-navy-800">
            <h3 className="text-sm font-bold text-white flex items-center space-x-1.5">
              <Sparkles className="w-3.5 h-3.5 text-brand-teal" />
              <span>Quick Demo Personas</span>
            </h3>
            <p className="text-[11px] text-slate-400 leading-relaxed">
              Launch a pre-seeded student profile immediately without creating an account. Only built-in demo personas support 1-click switching.
            </p>

            <div className="space-y-2 pt-1">
              {demoProfiles.map((demo) => (
                <button
                  key={demo.id}
                  type="button"
                  disabled={submitting}
                  onClick={() => handleDemoLogin(demo.id)}
                  className="w-full text-left p-3 rounded-xl bg-navy-900 hover:bg-navy-800 border border-navy-700 hover:border-brand-teal/50 transition-all flex items-center justify-between"
                >
                  <div>
                    <div className="font-bold text-white">{demo.fullName}</div>
                    <div className="text-[11px] text-brand-teal font-medium">{demo.targetRole}</div>
                  </div>
                  <UserCheck className="w-4 h-4 text-slate-400" />
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
