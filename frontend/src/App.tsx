import React, { useState, useEffect } from 'react';
import { AuthSession, DemoProfile, StudentProfile } from './types';
import { authApi } from './api/client';
import { Navbar } from './components/Navbar';
import { DiscoveryPage } from './pages/DiscoveryPage';
import { SkillGapPage } from './pages/SkillGapPage';
import { RoadmapPage } from './pages/RoadmapPage';
import { CatalogPage } from './pages/CatalogPage';
import { SavedPage } from './pages/SavedPage';
import { EvaluationPage } from './pages/EvaluationPage';
import { SimulatorPage } from './pages/SimulatorPage';
import { SystemStatusPage } from './pages/SystemStatusPage';
import { ProfileModal } from './components/ProfileModal';
import { OnboardingModal } from './components/OnboardingModal';
import { AuthGateModal } from './components/AuthGateModal';
import { Compass, Database, Cpu, Sparkles, Lock, UserCheck, LogIn, UserPlus } from 'lucide-react';

const PRIVATE_TABS = new Set(['discovery', 'skill-gap', 'roadmap', 'simulator', 'saved']);

export const App: React.FC = () => {
  const [demoProfiles, setDemoProfiles] = useState<DemoProfile[]>([]);
  const [activeStudent, setActiveStudent] = useState<StudentProfile | null>(null);
  const [currentTab, setCurrentTab] = useState<string>('discovery');
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [isOnboardingModalOpen, setIsOnboardingModalOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState<string | null>(null);

  const bootstrapSession = async () => {
    try {
      const [session, demos] = await Promise.all([authApi.me(), authApi.demoProfiles()]);
      setDemoProfiles(demos);
      setActiveStudent(session.authenticated && session.student ? session.student : null);
    } catch (err: any) {
      console.error('Failed to bootstrap session:', err);
      setAuthError(err.message || 'Unable to connect to PathPilot backend.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    bootstrapSession();
  }, []);

  const handleAuthenticated = (session: AuthSession) => {
    setAuthError(null);
    setActiveStudent(session.authenticated ? session.student : null);
  };

  const handleDemoLogin = async (demoId: number) => {
    try {
      setAuthError(null);
      const session = await authApi.demoLogin(demoId);
      setActiveStudent(session.student);
    } catch (err: any) {
      setAuthError(err.message || 'Demo sign-in failed');
    }
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error(err);
    } finally {
      setActiveStudent(null);
    }
  };

  const handleStudentCreated = (newStudent: StudentProfile) => {
    setActiveStudent(newStudent);
    setCurrentTab('discovery');
  };

  const handleProfileUpdated = (updated: StudentProfile) => {
    setActiveStudent(updated);
  };

  const requiresAuth = !activeStudent && PRIVATE_TABS.has(currentTab);

  return (
    <div className="min-h-screen bg-navy-950 text-slate-100 flex flex-col justify-between">
      <div>
        {/* Navigation Bar */}
        <Navbar
          currentTab={currentTab}
          setCurrentTab={(tab) => {
            if (tab === 'profile') {
              setIsProfileModalOpen(true);
            } else {
              setCurrentTab(tab);
            }
          }}
          demoProfiles={demoProfiles}
          activeStudent={activeStudent}
          onSelectDemoProfile={handleDemoLogin}
          onOpenNewStudentModal={() => setIsOnboardingModalOpen(true)}
          onOpenAuthModal={() => setIsAuthModalOpen(true)}
          onLogout={handleLogout}
        />

        {/* Main Content View */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-32 space-y-3">
              <div className="w-10 h-10 border-4 border-brand-violet border-t-transparent rounded-full animate-spin" />
              <p className="text-xs text-slate-400 font-medium">Connecting to PathPilot Java REST Backend...</p>
            </div>
          ) : requiresAuth ? (
            <div className="max-w-3xl mx-auto bg-navy-900 border border-navy-800 rounded-2xl p-8 text-center space-y-6 shadow-2xl">
              <div className="w-12 h-12 rounded-2xl bg-brand-violet/20 border border-brand-violet/30 flex items-center justify-center text-brand-violetLight mx-auto">
                <Lock className="w-6 h-6" />
              </div>

              <div className="space-y-2">
                <h1 className="text-2xl font-extrabold text-white tracking-tight">
                  Start Your Personalized PathPilot Session
                </h1>
                <p className="text-xs text-slate-300 max-w-xl mx-auto leading-relaxed">
                  Student recommendations, skill-gap diagnostics, career simulations, and learning roadmaps are protected by <code className="text-brand-tealLight">HttpSession</code> ownership controls. Choose a sandboxed demo persona below, sign in with your password, or create a new student account.
                </p>
              </div>

              {authError && (
                <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-300 text-xs">
                  {authError}
                </div>
              )}

              {demoProfiles.length > 0 && (
                <div className="space-y-2.5 text-left">
                  <div className="text-[11px] uppercase tracking-wider text-slate-400 font-bold text-center">
                    1-Click Demo Personas
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {demoProfiles.map((demo) => (
                      <button
                        key={demo.id}
                        type="button"
                        onClick={() => handleDemoLogin(demo.id)}
                        className="p-4 rounded-xl bg-navy-950 hover:bg-navy-800 border border-navy-800 hover:border-brand-teal/50 transition-all flex items-center justify-between text-left"
                      >
                        <div>
                          <div className="text-sm font-bold text-white">{demo.fullName}</div>
                          <div className="text-xs text-brand-teal font-medium">{demo.targetRole}</div>
                        </div>
                        <UserCheck className="w-4 h-4 text-slate-400" />
                      </button>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsAuthModalOpen(true)}
                  className="px-5 py-2.5 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white text-xs font-bold flex items-center space-x-2"
                >
                  <LogIn className="w-4 h-4" />
                  <span>Sign In with Email & Password</span>
                </button>

                <button
                  type="button"
                  onClick={() => setIsOnboardingModalOpen(true)}
                  className="px-5 py-2.5 rounded-xl bg-brand-teal hover:bg-brand-tealLight text-navy-950 text-xs font-bold flex items-center space-x-2"
                >
                  <UserPlus className="w-4 h-4" />
                  <span>Create New Student Profile</span>
                </button>
              </div>

              <div className="pt-4 border-t border-navy-800 text-[11px] text-slate-400">
                Or explore public views immediately:{' '}
                <button onClick={() => setCurrentTab('catalog')} className="text-brand-teal hover:underline font-semibold">
                  Catalog Explorer
                </button>{' '}
                •{' '}
                <button onClick={() => setCurrentTab('evaluation')} className="text-brand-teal hover:underline font-semibold">
                  Offline Benchmarks
                </button>{' '}
                •{' '}
                <button onClick={() => setCurrentTab('system-status')} className="text-brand-teal hover:underline font-semibold">
                  Servlet System Health
                </button>
              </div>
            </div>
          ) : (
            <>
              {currentTab === 'discovery' && (
                <DiscoveryPage
                  student={activeStudent}
                  onNavigateToRoadmap={() => setCurrentTab('roadmap')}
                  onNavigateToSkillGap={() => setCurrentTab('skill-gap')}
                />
              )}

              {currentTab === 'skill-gap' && (
                <SkillGapPage
                  student={activeStudent}
                  onNavigateToRoadmap={() => setCurrentTab('roadmap')}
                />
              )}

              {currentTab === 'roadmap' && <RoadmapPage student={activeStudent} />}

              {currentTab === 'simulator' && (
                <SimulatorPage student={activeStudent} onProfileUpdated={handleProfileUpdated} />
              )}

              {currentTab === 'catalog' && <CatalogPage studentId={activeStudent?.id || null} />}

              {currentTab === 'saved' && <SavedPage studentId={activeStudent?.id || null} />}

              {currentTab === 'evaluation' && <EvaluationPage />}

              {currentTab === 'system-status' && <SystemStatusPage />}
            </>
          )}
        </main>
      </div>

      {/* Modals */}
      <ProfileModal
        isOpen={isProfileModalOpen}
        onClose={() => setIsProfileModalOpen(false)}
        student={activeStudent}
        onProfileUpdated={handleProfileUpdated}
      />

      <OnboardingModal
        isOpen={isOnboardingModalOpen}
        onClose={() => setIsOnboardingModalOpen(false)}
        onStudentCreated={handleStudentCreated}
      />

      <AuthGateModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        demoProfiles={demoProfiles}
        onAuthenticated={handleAuthenticated}
        onOpenOnboarding={() => setIsOnboardingModalOpen(true)}
      />

      {/* Footer */}
      <footer className="border-t border-navy-800 bg-navy-900/80 py-6 mt-12 text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-2">
            <Compass className="w-4 h-4 text-brand-violetLight" />
            <span className="font-bold text-slate-300">PathPilot AI</span>
            <span>•</span>
            <span>Explainable Student Opportunity Recommendation System</span>
          </div>

          <div className="flex flex-wrap items-center gap-3 text-[11px]">
            <span className="flex items-center space-x-1 text-slate-400">
              <Cpu className="w-3.5 h-3.5 text-brand-teal" />
              <span>Java 21 • Spring Boot 3</span>
            </span>
            <span>•</span>
            <span className="flex items-center space-x-1 text-slate-400">
              <Database className="w-3.5 h-3.5 text-blue-400" />
              <span>Explicit JDBC & Relational SQL</span>
            </span>
            <span>•</span>
            <span className="flex items-center space-x-1 text-slate-400">
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              <span>React 18 & TypeScript</span>
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
};
