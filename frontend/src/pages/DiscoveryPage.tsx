import React, { useState, useEffect } from 'react';
import { RecommendationItem, RecommendationResult, StudentProfile } from '../types';
import { recommendationApi, feedbackApi, roadmapApi } from '../api/client';
import { ExplainableCard } from '../components/ExplainableCard';
import { ComparisonModal } from '../components/ComparisonModal';
import {
  Sparkles,
  RefreshCw,
  AlertTriangle,
  Layers,
  Clock,
  DollarSign,
  Target,
  GitCompare,
  SlidersHorizontal,
  Info,
  X,
} from 'lucide-react';

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
  const [resultMeta, setResultMeta] = useState<Pick<RecommendationResult, 'coldStart' | 'relevanceBasis' | 'diversityReranked'> | null>(null);
  const [items, setItems] = useState<RecommendationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedType, setSelectedType] = useState<string>('ALL');
  const [diverse, setDiverse] = useState<boolean>(true);
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const [feedbackToast, setFeedbackToast] = useState<string | null>(null);

  // Side-by-side comparison state
  const [selectedCompareIds, setSelectedCompareIds] = useState<number[]>([]);
  const [compareModalOpen, setCompareModalOpen] = useState(false);
  const [compareItems, setCompareItems] = useState<RecommendationItem[]>([]);
  const [compareLoading, setCompareLoading] = useState(false);
  const [compareError, setCompareError] = useState<string | null>(null);

  const fetchRecommendations = async (useDiversity = diverse) => {
    if (!student) return;
    try {
      setLoading(true);
      setError(null);
      const res = await recommendationApi.getRecommendations(student.id, 10, useDiversity);
      setItems(res.items);
      setResultMeta({
        coldStart: res.coldStart,
        relevanceBasis: res.relevanceBasis,
        diversityReranked: res.diversityReranked,
      });

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
    setSelectedCompareIds([]);
    fetchRecommendations(diverse);
  }, [student?.id, diverse]);

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
      showToast(err.message || 'Error saving opportunity');
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
      setSelectedCompareIds((prev) => prev.filter((id) => id !== oppId));
      showToast('Opportunity dismissed. Future rankings updated.');
    } catch (err: any) {
      showToast(err.message || 'Error dismissing opportunity');
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
      showToast(err.message || 'Error submitting rating');
    }
  };

  const handleAddToRoadmap = async (oppId: number) => {
    if (!student) return;
    try {
      await roadmapApi.addItem(student.id, oppId);
      setCompareModalOpen(false);
      showToast('Added opportunity to your learning roadmap!');
    } catch (err: any) {
      showToast(err.message || 'Error updating roadmap');
    }
  };

  const handleToggleCompare = (oppId: number) => {
    setSelectedCompareIds((prev) => {
      if (prev.includes(oppId)) {
        return prev.filter((id) => id !== oppId);
      }
      if (prev.length >= 3) {
        return prev;
      }
      return [...prev, oppId];
    });
  };

  const handleOpenComparison = async () => {
    if (!student || selectedCompareIds.length < 2) return;
    setCompareModalOpen(true);
    setCompareLoading(true);
    setCompareError(null);
    try {
      const compared = await recommendationApi.compare(student.id, selectedCompareIds);
      setCompareItems(compared);
    } catch (err: any) {
      setCompareError(err.message || 'Failed to compute side-by-side comparison');
    } finally {
      setCompareLoading(false);
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
                onClick={() => fetchRecommendations(diverse)}
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

      {/* Honest Cold-Start Guidance Banner */}
      {resultMeta?.coldStart && (
        <div className="bg-amber-500/10 border border-amber-500/30 rounded-2xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-start space-x-3">
            <Info className="w-5 h-5 text-amber-400 flex-shrink-0 mt-0.5" />
            <div className="text-xs text-slate-200">
              <strong className="text-amber-300">Cold-Start Recommendation Mode:</strong> Your profile currently has limited skill or feedback history, so rankings prioritize role alignment, time/budget constraints, and foundational coverage. Rate or bookmark opportunities to personalize scores further.
            </div>
          </div>
          <div className="flex items-center space-x-2 flex-shrink-0">
            <button
              onClick={onNavigateToSkillGap}
              className="px-3 py-1.5 rounded-lg bg-navy-900 hover:bg-navy-800 border border-navy-700 text-xs font-semibold text-white whitespace-nowrap"
            >
              Inspect Skill Gap
            </button>
            <button
              onClick={onNavigateToRoadmap}
              className="px-3 py-1.5 rounded-lg bg-amber-500/20 hover:bg-amber-500/30 border border-amber-500/40 text-xs font-semibold text-amber-200 whitespace-nowrap"
            >
              View Roadmap
            </button>
          </div>
        </div>
      )}

      {/* Filter Tabs, Diversity Toggle & Compare Trigger */}
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

        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={() => setDiverse(!diverse)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold border flex items-center space-x-1.5 transition-all ${
              diverse
                ? 'bg-brand-teal/15 text-brand-tealLight border-brand-teal/40'
                : 'bg-navy-900 text-slate-300 border-navy-800 hover:text-white'
            }`}
            title="Maximal Marginal Relevance balances top scores against diverse opportunity types and providers"
          >
            <SlidersHorizontal className="w-3.5 h-3.5" />
            <span>Diversity Reranking: {diverse ? 'ON (MMR)' : 'OFF'}</span>
          </button>

          {selectedCompareIds.length > 0 && (
            <div className="flex items-center space-x-1.5">
              <button
                type="button"
                onClick={handleOpenComparison}
                disabled={selectedCompareIds.length < 2}
                className="px-3 py-1.5 rounded-lg text-xs font-bold bg-brand-violet hover:bg-brand-violetLight text-white flex items-center space-x-1.5 disabled:opacity-50 transition-all shadow"
              >
                <GitCompare className="w-3.5 h-3.5" />
                <span>Compare ({selectedCompareIds.length}/3)</span>
              </button>
              <button
                type="button"
                onClick={() => setSelectedCompareIds([])}
                title="Clear comparison selection"
                className="p-1.5 rounded-lg bg-navy-900 border border-navy-800 text-slate-400 hover:text-white"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          )}

          <div className="text-xs text-slate-400 font-medium">
            Showing <span className="text-white font-bold">{filteredItems.length}</span> ranked opportunities
          </div>
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
              isSelectedForCompare={selectedCompareIds.includes(item.opportunity.id)}
              onToggleCompare={handleToggleCompare}
              compareDisabled={selectedCompareIds.length >= 3}
            />
          ))}
        </div>
      )}

      <ComparisonModal
        isOpen={compareModalOpen}
        onClose={() => setCompareModalOpen(false)}
        items={compareItems}
        loading={compareLoading}
        error={compareError}
        onAddToRoadmap={handleAddToRoadmap}
      />
    </div>
  );
};
