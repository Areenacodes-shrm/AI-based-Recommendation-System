import React from 'react';
import { Compass, Target, Map, Search, Bookmark, BarChart3, Settings, UserPlus, CheckCircle2 } from 'lucide-react';
import { StudentProfile } from '../types';

interface NavbarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  students: StudentProfile[];
  activeStudent: StudentProfile | null;
  onSelectStudent: (student: StudentProfile) => void;
  onOpenNewStudentModal: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  setCurrentTab,
  students,
  activeStudent,
  onSelectStudent,
  onOpenNewStudentModal,
}) => {
  return (
    <header className="border-b border-navy-800 bg-navy-900/95 sticky top-0 z-40 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Brand */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => setCurrentTab('discovery')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-brand-violet to-brand-teal flex items-center justify-center shadow-lg shadow-brand-violet/20">
              <Compass className="w-6 h-6 text-white animate-pulse" />
            </div>
            <div>
              <div className="flex items-center space-x-1.5">
                <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
                  PathPilot
                </span>
                <span className="text-xs px-2 py-0.5 rounded-full font-bold bg-brand-violet/20 text-brand-violet border border-brand-violet/30">
                  AI
                </span>
              </div>
              <p className="text-[10px] text-slate-400 font-medium hidden sm:block">Explainable Opportunity Recommender</p>
            </div>
          </div>

          {/* Nav Tabs */}
          <nav className="hidden md:flex items-center space-x-1">
            <button
              onClick={() => setCurrentTab('discovery')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'discovery'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Compass className="w-4 h-4" />
              <span>Recommendations</span>
            </button>

            <button
              onClick={() => setCurrentTab('skill-gap')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'skill-gap'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Target className="w-4 h-4" />
              <span>Skill Gap</span>
            </button>

            <button
              onClick={() => setCurrentTab('roadmap')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'roadmap'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Map className="w-4 h-4" />
              <span>Roadmap</span>
            </button>

            <button
              onClick={() => setCurrentTab('catalog')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'catalog'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Search className="w-4 h-4" />
              <span>Catalog</span>
            </button>

            <button
              onClick={() => setCurrentTab('saved')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'saved'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Bookmark className="w-4 h-4" />
              <span>Saved</span>
            </button>

            <button
              onClick={() => setCurrentTab('evaluation')}
              className={`px-3 py-1.5 rounded-lg text-sm font-semibold flex items-center space-x-1.5 transition-all ${
                currentTab === 'evaluation'
                  ? 'bg-brand-violet text-white shadow-md shadow-brand-violet/25'
                  : 'text-slate-300 hover:text-white hover:bg-navy-800'
              }`}
            >
              <BarChart3 className="w-4 h-4" />
              <span>Benchmarks</span>
            </button>
          </nav>

          {/* Active Student Selector & Profile */}
          <div className="flex items-center space-x-3">
            <div className="relative">
              <select
                aria-label="Active Student Profile"
                value={activeStudent?.id || ''}
                onChange={(e) => {
                  const s = students.find((st) => st.id === Number(e.target.value));
                  if (s) onSelectStudent(s);
                }}
                className="bg-navy-800 border border-navy-700 text-xs text-slate-200 font-medium rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-brand-violet"
              >
                {students.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.fullName} ({s.targetRole})
                  </option>
                ))}
              </select>
            </div>

            <button
              onClick={onOpenNewStudentModal}
              title="Add New Student Profile"
              className="p-1.5 bg-brand-teal/20 text-brand-teal border border-brand-teal/40 rounded-lg hover:bg-brand-teal/30 transition-colors"
            >
              <UserPlus className="w-4 h-4" />
            </button>

            <button
              onClick={() => setCurrentTab('profile')}
              title="Profile Settings"
              className={`p-1.5 rounded-lg border transition-colors ${
                currentTab === 'profile'
                  ? 'bg-brand-violet text-white border-brand-violet'
                  : 'bg-navy-800 text-slate-300 border-navy-700 hover:text-white hover:bg-navy-700'
              }`}
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Mobile Navigation bar */}
        <div className="flex md:hidden overflow-x-auto py-2 border-t border-navy-800 space-x-2 text-xs">
          <button
            onClick={() => setCurrentTab('discovery')}
            className={`px-2.5 py-1 rounded whitespace-nowrap ${currentTab === 'discovery' ? 'bg-brand-violet text-white' : 'text-slate-300'}`}
          >
            Recommendations
          </button>
          <button
            onClick={() => setCurrentTab('skill-gap')}
            className={`px-2.5 py-1 rounded whitespace-nowrap ${currentTab === 'skill-gap' ? 'bg-brand-violet text-white' : 'text-slate-300'}`}
          >
            Skill Gap
          </button>
          <button
            onClick={() => setCurrentTab('roadmap')}
            className={`px-2.5 py-1 rounded whitespace-nowrap ${currentTab === 'roadmap' ? 'bg-brand-violet text-white' : 'text-slate-300'}`}
          >
            Roadmap
          </button>
          <button
            onClick={() => setCurrentTab('catalog')}
            className={`px-2.5 py-1 rounded whitespace-nowrap ${currentTab === 'catalog' ? 'bg-brand-violet text-white' : 'text-slate-300'}`}
          >
            Catalog
          </button>
          <button
            onClick={() => setCurrentTab('evaluation')}
            className={`px-2.5 py-1 rounded whitespace-nowrap ${currentTab === 'evaluation' ? 'bg-brand-violet text-white' : 'text-slate-300'}`}
          >
            Benchmarks
          </button>
        </div>
      </div>
    </header>
  );
};
