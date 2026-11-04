import React, { useState, useEffect } from 'react';
import { RecommendationItem, StudentProfile, OpportunityType } from '../types';
import { recommendationApi, feedbackApi, roadmapApi } from '../api/client';
import { ExplainableCard } from '../components/ExplainableCard';
import { Sparkles, RefreshCw, Filter, AlertTriangle, Layers, Clock, DollarSign, Target } from 'lucide-react';

interface DiscoveryPageProps {
  student: StudentProfile | null;
  onNavigateToRoadmap: () => void;
  onNavigateToSkillGap: () => void;
}

export const DiscoveryPage: React.FC<DiscoveryPageProps> = ({
  student,
  onNavigateToRoadmap,
  onNavigateToSkillGap,
}) => {
  const [items, setItems] = useState<RecommendationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedType, setSelectedType] = useState<string>('ALL');
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const [feedbackToast, setFeedbackToast] = useState<string | null>(null);

  const fetchRecommendations = async () => {
    if (!student) return;
    try {
      setLoading(true);
      setError(null);
      const res = await recommendationApi.getRecommendations(student.id, 10);
      setItems(res.items);

      // Load saved feedback
      const feedbacks = await feedbackApi.getStudentFeedback(student.id);
      const saved = new Set<number>();
      for (const fb of feedbacks) {
        if (fb.actionType === 'SAVED') {
          saved.add(fb.opportunityId);
        }
      }
      setSavedIds(saved);
    } catch (err: any) {
      setError(err.message || 'Failed to load recommendations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecommendations();
  }, [student?.id]);

  const showToast = (msg: string) => {
    setFeedbackToast(msg);
    setTimeout(() => setFeedbackToast(null), 3000);
  };

  const handleSave = async (oppId: number) => {
    if (!student) return;
    try {
      await feedbackApi.submit(student.id, {
        opportunityId: oppId,
        actionType: 'SAVED',
      });
      setSavedIds(new Set([...savedIds, oppId]));
      showToast('Opportunity saved to your library!');
    } catch (err: any) {
      showToast('Error saving opportunity');
    }
  };

  const handleDismiss = async (oppId: number) => {
    if (!student) return;
    try {
      await feedbackApi.submit(student.id, {
        opportunityId: oppId,
        actionType: 'DISMISSED',
      });
      setItems((prev) => prev.filter((i) => i.opportunity.id !== oppId));
      showToast('Opportunity dismissed. Future rankings updated.');
    } catch (err: any) {
      showToast('Error dismissing opportunity');
    }
  };

  const handleRate = async (oppId: number, rating: number) => {
    if (!student) return;
    try {
      await feedbackApi.submit(student.id, {
        opportunityId: oppId,
        actionType: 'RATED',
        ratingValue: rating,
      });
      showToast(`Thank you! Rated ${rating} stars. Ranking weights adjusted.`);
    } catch (err: any) {
      showToast('Error submitting rating');
    }
  };

  const handleAddToRoadmap = async (oppId: number) => {
    if (!student) return;
    try {
      // Regenerate or add
      await roadmapApi.generateRoadmap(student.id);
      showToast('Added to learning roadmap!');
      onNavigateToRoadmap();
    } catch (err: any) {
      showToast('Error updating roadmap');
    }
  };

  const filteredItems = items.filter((item) => {
    if (selectedType === 'ALL') return true;
    return item.opportunity.opportunityType === selectedType;
  });

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {feedbackToast && (
        <div className="fixed bottom-6 right-6 z-50 bg-brand-violet text-white px-4 py-2.5 rounded-xl shadow-2xl text-xs font-semibold flex items-center space-x-2 animate-bounce">
          <Sparkles className="w-4 h-4 text-brand-teal" />
          <span>{feedbackToast}</span>
        </div>
      )}

      {/* Header Profile Constraints Banner */}
      {student && (
        <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6 relative overflow-hidden">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="flex items-center space-x-2 mb-1.5">
                <span className="text-xs font-semibold text-brand-teal uppercase tracking-wider">
                  Target Aspirations
                </span>
                <span className="text-slate-500">•</span>
                <span className="text-xs text-slate-400">Constraint-Aware Personalization</span>
              </div>
              <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center space-x-2">
                <span>Recommendations for {student.fullName}</span>
              </h1>
              <p className="text-xs text-slate-300 mt-1 max-w-2xl">
                Ranked using Java 21 Hybrid Scoring Engine combining TF-IDF content similarity, Jaccard skill-gap matching, Bayesian feedback, and real-time telemetry.
              </p>
            </div>

            {/* Quick Metrics */}
            <div className="flex flex-wrap items-center gap-3">
              <div className="bg-navy-950/80 border border-navy-800 px-3.5 py-2 rounded-xl text-xs flex items-center space-x-2">
                <Target className="w-4 h-4 text-brand-violetLight" />
                <div>
                  <div className="text-[10px] text-slate-400">Target Role</div>
                  <div className="font-bold text-white">{student.targetRole}</div>
                </div>
              </div>

              <div className="bg-navy-950/80 border border-navy-800 px-3.5 py-2 rounded-xl text-xs flex items-center space-x-2">
                <Clock className="w-4 h-4 text-brand-teal" />
                <div>
                  <div className="text-[10px] text-slate-400">Weekly Hours</div>
                  <div className="font-bold text-white">{student.weeklyHoursAvailable} hrs/wk</div>
                </div>
              </div>

              <div className="bg-navy-950/80 border border-navy-800 px-3.5 py-2 rounded-xl text-xs flex items-center space-x-2">
                <DollarSign className="w-4 h-4 text-amber-400" />
                <div>
                  <div className="text-[10px] text-slate-400">Max Budget</div>
                  <div className="font-bold text-white">${student.maxBudget.toFixed(2)}</div>
                </div>
              </div>

              <button
                onClick={fetchRecommendations}
                disabled={loading}
                className="px-4 py-2.5 bg-brand-violet hover:bg-brand-violetLight text-white rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all shadow-md shadow-brand-violet/25 disabled:opacity-50"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
                <span>Refresh AI Rankings</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
        <div className="flex items-center space-x-1.5 overflow-x-auto pb-1">
          {['ALL', 'COURSE', 'PROJECT', 'INTERNSHIP', 'CERTIFICATION'].map((type) => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                selectedType === type
                  ? 'bg-brand-violet text-white shadow'
                  : 'bg-navy-900 text-slate-300 border border-navy-800 hover:text-white hover:bg-navy-800'
              }`}
            >
              {type === 'ALL' ? 'All Opportunities' : type.charAt(0) + type.slice(1).toLowerCase() + 's'}
            </button>
          ))}
        </div>

        <div className="text-xs text-slate-400 font-medium">
          Showing <span className="text-white font-bold">{filteredItems.length}</span> ranked opportunities
        </div>
      </div>

      {/* Content State: Loading, Error, Empty, or Cards Grid */}
      {loading ? (
        <div className="text-center py-20 bg-navy-900 border border-navy-800 rounded-2xl">
          <RefreshCw className="w-8 h-8 text-brand-violet animate-spin mx-auto mb-3" />
          <h3 className="text-base font-bold text-white">Running Parallel Scoring Pipeline...</h3>
          <p className="text-xs text-slate-400 mt-1">
            Evaluating candidate opportunities via multi-threaded ExecutorService & CompletableFuture tasks.
          </p>
        </div>
      ) : error ? (
        <div className="p-6 bg-rose-500/10 border border-rose-500/20 rounded-2xl text-rose-300 text-xs flex items-center space-x-3">
          <AlertTriangle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      ) : filteredItems.length === 0 ? (
        <div className="text-center py-16 bg-navy-900 border border-navy-800 rounded-2xl">
          <Layers className="w-10 h-10 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-white">No opportunities match current filter</h3>
          <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
            Try adjusting your category filter, or widen your budget and difficulty settings in your profile.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredItems.map((item) => (
            <ExplainableCard
              key={item.opportunity.id}
              item={item}
              onSave={handleSave}
              onDismiss={handleDismiss}
              onAddToRoadmap={handleAddToRoadmap}
              onRate={handleRate}
              isSaved={savedIds.has(item.opportunity.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
};
