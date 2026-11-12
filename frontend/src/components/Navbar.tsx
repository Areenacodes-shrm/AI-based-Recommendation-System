import React from 'react';
import {
  Compass,
  Target,
  Map,
  Search,
  Bookmark,
  BarChart3,
  Settings,
  UserPlus,
  Sliders,
  Activity,
  LogIn,
  LogOut,
} from 'lucide-react';
import { DemoProfile, StudentProfile } from '../types';

interface NavbarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  demoProfiles: DemoProfile[];
  activeStudent: StudentProfile | null;
  onSelectDemoProfile: (demoId: number) => void;
  onOpenNewStudentModal: () => void;
  onOpenAuthModal: () => void;
  onLogout: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  setCurrentTab,
  demoProfiles,
  activeStudent,
  onSelectDemoProfile,
  onOpenNewStudentModal,
  onOpenAuthModal,
  onLogout,
}) => {
  const isDemoActive = activeStudent && demoProfiles.some((d) => d.id === activeStudent.id);

  return (
    <header className="border-b border-navy-800 bg-navy-900/95 sticky top-0 z-40 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-3">
          {/* Logo & Brand */}
          <div className="flex items-center space-x-2.5 cursor-pointer flex-shrink-0" onClick={() => setCurrentTab('discovery')}>
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-violet to-brand-teal flex items-center justify-center shadow-lg shadow-brand-violet/20">
              <Compass className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <span className="font-extrabold text-base tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                  PathPilot
                </span>
                <span className="text-[10px] px-1.5 py-0.5 rounded-full font-bold bg-brand-violet/20 text-brand-violet border border-brand-violet/30">
                  AI
                </span>
              </div>
              <p className="text-[10px] text-slate-400 font-medium hidden xl:block">Explainable Recommender</p>
            </div>
          </div>

          {/* Nav Tabs */}
          <nav className="hidden lg:flex items-center space-x-1">
            <button
              onClick={() => setCurrentTab('discovery')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'discovery'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Compass className="w-3.5 h-3.5" />
              <span>For You</span>
            </button>

            <button
              onClick={() => setCurrentTab('skill-gap')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'skill-gap'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Target className="w-3.5 h-3.5" />
              <span>Skill Gap</span>
            </button>

            <button
              onClick={() => setCurrentTab('roadmap')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'roadmap'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Map className="w-3.5 h-3.5" />
              <span>Roadmap</span>
            </button>

            <button
              onClick={() => setCurrentTab('simulator')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'simulator'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Sliders className="w-3.5 h-3.5" />
              <span>Simulator</span>
            </button>

            <button
              onClick={() => setCurrentTab('catalog')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'catalog'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Search className="w-3.5 h-3.5" />
              <span>Catalog</span>
            </button>

            <button
              onClick={() => setCurrentTab('saved')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'saved'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Bookmark className="w-3.5 h-3.5" />
              <span>Saved</span>
            </button>

            <button
              onClick={() => setCurrentTab('evaluation')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'evaluation'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <BarChart3 className="w-3.5 h-3.5" />
              <span>Benchmarks</span>
            </button>

            <button
              onClick={() => setCurrentTab('system-status')}
              className={`px-2.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'system-status'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Activity className="w-3.5 h-3.5" />
              <span>Health</span>
            </button>
          </nav>

          {/* Session, Demo Switcher & Profile Controls */}
          <div className="flex items-center space-x-2 flex-shrink-0">
            <select
              aria-label="Switch Demo Persona"
              value={isDemoActive ? activeStudent.id : ''}
              onChange={(e) => {
                const selectedId = Number(e.target.value);
                if (selectedId) onSelectDemoProfile(selectedId);
              }}
              className="bg-navy-800 border border-navy-700 text-xs text-slate-200 font-medium rounded-lg px-2.5 py-1.5 max-w-[175px] focus:outline-none focus:ring-2 focus:ring-brand-violet"
            >
              <option value="" disabled>
                {activeStudent ? activeStudent.fullName : 'Select Demo Persona...'}
              </option>
              {demoProfiles.map((demo) => (
                <option key={demo.id} value={demo.id}>
                  {demo.fullName} ({demo.targetRole})
                </option>
              ))}
            </select>

            <button
              onClick={onOpenNewStudentModal}
              title="Create New Student Profile"
              className="p-1.5 bg-brand-teal/20 text-brand-teal border border-brand-teal/40 rounded-lg hover:bg-brand-teal/30 transition-colors"
            >
              <UserPlus className="w-4 h-4" />
            </button>

            {activeStudent ? (
              <>
                <button
                  onClick={() => setCurrentTab('profile')}
                  title={`Profile Settings (${activeStudent.fullName})`}
                  className={`p-1.5 rounded-lg border transition-colors ${
                    currentTab === 'profile'
                      ? 'bg-brand-violet text-white border-brand-violet'
                      : 'bg-navy-800 text-slate-300 border-navy-700 hover:text-white hover:bg-navy-700'
                  }`}
                >
                  <Settings className="w-4 h-4" />
                </button>

                <button
                  onClick={onLogout}
                  title="Sign Out"
                  className="px-2.5 py-1.5 rounded-lg border border-navy-700 bg-navy-800 text-slate-300 hover:text-rose-300 hover:border-rose-500/40 text-xs font-semibold flex items-center space-x-1 transition-colors"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">Sign Out</span>
                </button>
              </>
            ) : (
              <button
                onClick={onOpenAuthModal}
                className="px-3 py-1.5 rounded-lg bg-brand-violet hover:bg-brand-violetLight text-white text-xs font-bold flex items-center space-x-1 transition-colors"
              >
                <LogIn className="w-3.5 h-3.5" />
                <span>Sign In</span>
              </button>
            )}
          </div>
        </div>

        {/* Mobile Navigation bar */}
        <div className="flex lg:hidden overflow-x-auto py-2 border-t border-navy-800 space-x-2 text-xs">
          {[
            { id: 'discovery', label: 'For You' },
            { id: 'skill-gap', label: 'Skill Gap' },
            { id: 'roadmap', label: 'Roadmap' },
            { id: 'simulator', label: 'Simulator' },
            { id: 'catalog', label: 'Catalog' },
            { id: 'saved', label: 'Saved' },
            { id: 'evaluation', label: 'Benchmarks' },
            { id: 'system-status', label: 'Health' },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setCurrentTab(tab.id)}
              className={`px-2.5 py-1 rounded whitespace-nowrap ${
                currentTab === tab.id ? 'bg-brand-violet text-white' : 'text-slate-300'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>
    </header>
  );
};
