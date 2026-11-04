import React, { useState, useEffect } from 'react';
import { StudentProfile } from './types';
import { studentApi } from './api/client';
import { Navbar } from './components/Navbar';
import { DiscoveryPage } from './pages/DiscoveryPage';
import { SkillGapPage } from './pages/SkillGapPage';
import { RoadmapPage } from './pages/RoadmapPage';
import { CatalogPage } from './pages/CatalogPage';
import { SavedPage } from './pages/SavedPage';
import { EvaluationPage } from './pages/EvaluationPage';
import { ProfileModal } from './components/ProfileModal';
import { OnboardingModal } from './components/OnboardingModal';
import { Compass, Database, Cpu, Sparkles } from 'lucide-react';

export const App: React.FC = () => {
  const [students, setStudents] = useState<StudentProfile[]>([]);
  const [activeStudent, setActiveStudent] = useState<StudentProfile | null>(null);
  const [currentTab, setCurrentTab] = useState<string>('discovery');
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
  const [isOnboardingModalOpen, setIsOnboardingModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchStudents = async () => {
    try {
      const data = await studentApi.getAll();
      setStudents(data);
      if (data.length > 0 && !activeStudent) {
        setActiveStudent(data[0]);
      }
    } catch (err) {
      console.error('Failed to load students:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const handleStudentCreated = (newStudent: StudentProfile) => {
    setStudents((prev) => [...prev, newStudent]);
    setActiveStudent(newStudent);
    setCurrentTab('discovery');
  };

  const handleProfileUpdated = (updated: StudentProfile) => {
    setStudents((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
    setActiveStudent(updated);
  };

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
          students={students}
          activeStudent={activeStudent}
          onSelectStudent={setActiveStudent}
          onOpenNewStudentModal={() => setIsOnboardingModalOpen(true)}
        />

        {/* Main Content View */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-32 space-y-3">
              <div className="w-10 h-10 border-4 border-brand-violet border-t-transparent rounded-full animate-spin" />
              <p className="text-xs text-slate-400 font-medium">Connecting to PathPilot Java REST Backend...</p>
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

              {currentTab === 'catalog' && <CatalogPage studentId={activeStudent?.id || null} />}

              {currentTab === 'saved' && <SavedPage studentId={activeStudent?.id || null} />}

              {currentTab === 'evaluation' && <EvaluationPage />}
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
