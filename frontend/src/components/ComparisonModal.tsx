import React from 'react';
import { RecommendationItem } from '../types';
import { X, Scale, CheckCircle2, AlertCircle, PlusCircle, ExternalLink, Clock, DollarSign, Award } from 'lucide-react';

interface ComparisonModalProps {
  isOpen: boolean;
  onClose: () => void;
  items: RecommendationItem[];
  loading: boolean;
  error: string | null;
  onAddToRoadmap: (opportunityId: number) => void;
}

export const ComparisonModal: React.FC<ComparisonModalProps> = ({
  isOpen,
  onClose,
  items,
  loading,
  error,
  onAddToRoadmap,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-navy-950/85 backdrop-blur-sm">
      <div className="bg-navy-900 border border-navy-800 rounded-2xl w-full max-w-5xl p-6 shadow-2xl relative max-h-[90vh] overflow-y-auto">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-slate-400 hover:text-white"
          aria-label="Close comparison modal"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-2.5 mb-5">
          <div className="w-9 h-9 rounded-xl bg-brand-teal/20 border border-brand-teal/30 flex items-center justify-center text-brand-teal">
            <Scale className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Side-by-Side Opportunity Comparison</h2>
            <p className="text-xs text-slate-400">
              Trade-off analysis across skill gains, missing target competencies, time commitment, budget, and heuristic match breakdown
            </p>
          </div>
        </div>

        {loading ? (
          <div className="py-16 text-center text-xs text-slate-400">
            Computing parallel side-by-side comparison scores...
          </div>
        ) : error ? (
          <div className="p-4 bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs rounded-xl">
            {error}
          </div>
        ) : items.length === 0 ? (
          <div className="py-12 text-center text-xs text-slate-400">
            Select 2 or 3 opportunities to compare.
          </div>
        ) : (
          <div className={`grid grid-cols-1 ${items.length === 2 ? 'md:grid-cols-2' : 'md:grid-cols-3'} gap-4`}>
            {items.map((rec) => {
              const opp = rec.opportunity;
              const matchPct = Math.round(rec.rankingScore * 100);
              return (
                <div
                  key={opp.id}
                  className={`bg-navy-950/80 border rounded-xl p-4 flex flex-col justify-between space-y-4 ${
                    rec.rankOrder === 1 ? 'border-brand-teal/60 shadow-lg shadow-brand-teal/10' : 'border-navy-800'
                  }`}
                >
                  <div className="space-y-3">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-navy-800 text-brand-violetLight border border-navy-700">
                        #{rec.rankOrder} • {opp.opportunityType}
                      </span>
                      <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-brand-teal/15 text-brand-tealLight border border-brand-teal/30">
                        {matchPct}% Match
                      </span>
                    </div>

                    <div>
                      <h3 className="text-sm font-bold text-white leading-snug">{opp.title}</h3>
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        {opp.provider} • {opp.difficultyLevel}
                      </p>
                    </div>

                    <div className="grid grid-cols-2 gap-2 text-xs bg-navy-900 p-2.5 rounded-lg border border-navy-800">
                      <div className="flex items-center space-x-1.5 text-slate-300">
                        <DollarSign className="w-3.5 h-3.5 text-amber-400" />
                        <span className="font-semibold">
                          {opp.cost === 0 ? 'Free' : `$${opp.cost.toFixed(2)}`}
                        </span>
                      </div>
                      <div className="flex items-center space-x-1.5 text-slate-300">
                        <Clock className="w-3.5 h-3.5 text-brand-teal" />
                        <span className="font-semibold">~{opp.estimatedHours} hrs</span>
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] uppercase tracking-wider text-slate-400 font-bold mb-1">
                        Skills Gained ({opp.skillsGained.length})
                      </div>
                      <div className="flex flex-wrap gap-1">
                        {opp.skillsGained.map((s) => (
                          <span key={s.id} className="px-2 py-0.5 rounded text-[10px] bg-navy-900 border border-navy-800 text-slate-200 flex items-center space-x-1">
                            <CheckCircle2 className="w-2.5 h-2.5 text-brand-teal" />
                            <span>{s.name}</span>
                          </span>
                        ))}
                      </div>
                    </div>

                    <div>
                      <div className="text-[10px] uppercase tracking-wider text-slate-400 font-bold mb-1">
                        Remaining Missing Role Skills
                      </div>
                      {rec.explanation?.missingSkills && rec.explanation.missingSkills.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {rec.explanation.missingSkills.map((ms) => (
                            <span key={ms} className="px-2 py-0.5 rounded text-[10px] bg-amber-500/10 border border-amber-500/20 text-amber-300 flex items-center space-x-1">
                              <AlertCircle className="w-2.5 h-2.5" />
                              <span>{ms}</span>
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-[11px] text-emerald-400">Covers key remaining role gaps cleanly</span>
                      )}
                    </div>

                    <div className="text-[11px] space-y-1 bg-navy-900 p-2.5 rounded-lg border border-navy-800">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Content Alignment:</span>
                        <span className="font-mono text-white font-semibold">{Math.round(rec.contentScore * 100)}%</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Skill-Gap Fit:</span>
                        <span className="font-mono text-brand-teal font-semibold">{Math.round(rec.skillScore * 100)}%</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Constraint & Difficulty:</span>
                        <span className="font-mono text-brand-violetLight font-semibold">{Math.round(rec.contextualScore * 100)}%</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Feedback & Telemetry:</span>
                        <span className="font-mono text-amber-400 font-semibold">{Math.round(rec.feedbackScore * 100)}%</span>
                      </div>
                    </div>

                    {rec.explanation?.confidenceLabel && (
                      <div className="text-[10px] text-slate-400 flex items-center space-x-1">
                        <Award className="w-3 h-3 text-brand-violetLight flex-shrink-0" />
                        <span>{rec.explanation.confidenceLabel}</span>
                      </div>
                    )}
                  </div>

                  <div className="pt-3 border-t border-navy-800 flex items-center justify-between gap-2">
                    <button
                      onClick={() => onAddToRoadmap(opp.id)}
                      className="flex-1 py-2 px-3 rounded-lg bg-brand-teal/15 hover:bg-brand-teal/25 text-brand-tealLight border border-brand-teal/30 text-xs font-bold flex items-center justify-center space-x-1"
                    >
                      <PlusCircle className="w-3.5 h-3.5" />
                      <span>Add to Roadmap</span>
                    </button>
                    {opp.url && (
                      <a
                        href={opp.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-2 rounded-lg bg-navy-800 hover:bg-navy-700 text-slate-300 hover:text-white border border-navy-700"
                        title="Open Opportunity Resource"
                      >
                        <ExternalLink className="w-3.5 h-3.5" />
                      </a>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
