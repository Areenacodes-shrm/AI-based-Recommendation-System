import React, { useState } from 'react';
import {
  RecommendationItem,
  OpportunityType,
  PlanStatus,
} from '../types';
import {
  Bookmark,
  XCircle,
  PlusCircle,
  ExternalLink,
  ChevronDown,
  ChevronUp,
  Star,
  CheckCircle,
  Clock,
  Award,
  BookOpen,
  Briefcase,
  Code2,
  GitCompare,
  ShieldAlert,
} from 'lucide-react';

interface ExplainableCardProps {
  item: RecommendationItem;
  onSave: (opportunityId: number) => void;
  onDismiss: (opportunityId: number) => void;
  onAddToRoadmap?: (opportunityId: number) => void;
  onRate: (opportunityId: number, rating: number) => void;
  isSaved?: boolean;
  isSelectedForCompare?: boolean;
  onToggleCompare?: (opportunityId: number) => void;
  compareDisabled?: boolean;
}

export const ExplainableCard: React.FC<ExplainableCardProps> = ({
  item,
  onSave,
  onDismiss,
  onAddToRoadmap,
  onRate,
  isSaved = false,
  isSelectedForCompare = false,
  onToggleCompare,
  compareDisabled = false,
}) => {
  const [showBreakdown, setShowBreakdown] = useState(false);
  const [userRating, setUserRating] = useState<number | null>(null);

  const { opportunity, rankingScore, contentScore, skillScore, contextualScore, feedbackScore, explanation } = item;

  const getConfidenceStyle = () => {
    switch (explanation.relevanceBasis) {
      case 'FEEDBACK_SUPPORTED':
        return 'bg-emerald-500/10 text-emerald-300 border-emerald-500/25';
      case 'PROFILE_BASED':
        return 'bg-blue-500/10 text-blue-300 border-blue-500/25';
      case 'LIMITED_DATA_COLD_START':
        return 'bg-amber-500/10 text-amber-300 border-amber-500/25';
      default:
        return 'bg-navy-800 text-slate-300 border-navy-700';
    }
  };

  const getTypeBadge = (type: OpportunityType) => {
    switch (type) {
      case 'COURSE':
        return {
          icon: <BookOpen className="w-3.5 h-3.5" />,
          label: opportunity.platform ? `Course • ${opportunity.platform}` : 'Course',
          color: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
        };
      case 'PROJECT':
        return {
          icon: <Code2 className="w-3.5 h-3.5" />,
          label: opportunity.projectTier ? `Project • ${opportunity.projectTier}` : 'Project',
          color: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
        };
      case 'INTERNSHIP':
        return {
          icon: <Briefcase className="w-3.5 h-3.5" />,
          label: opportunity.locationType ? `Internship • ${opportunity.locationType}` : 'Internship',
          color: 'bg-brand-violet/10 text-brand-violetLight border-brand-violet/20',
        };
      case 'CERTIFICATION':
        return {
          icon: <Award className="w-3.5 h-3.5" />,
          label: opportunity.issuingOrganization ? `Cert • ${opportunity.issuingOrganization}` : 'Certification',
          color: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
        };
    }
  };

  const badge = getTypeBadge(opportunity.opportunityType);
  const matchPercent = Math.round(rankingScore * 100);

  return (
    <div className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card relative flex flex-col justify-between overflow-hidden">
      {/* Top Banner: Type Badge, Match Score, Save & Dismiss */}
      <div>
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className={`inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${badge.color}`}>
            {badge.icon}
            <span>{badge.label}</span>
          </span>

          <div className="flex items-center space-x-1.5">
            {onToggleCompare && (
              <button
                type="button"
                onClick={() => onToggleCompare(opportunity.id)}
                disabled={compareDisabled && !isSelectedForCompare}
                title={
                  isSelectedForCompare
                    ? 'Remove from side-by-side comparison'
                    : compareDisabled
                    ? 'Select up to 3 opportunities to compare'
                    : 'Compare side-by-side'
                }
                className={`px-2 py-1 rounded-lg border text-[11px] font-semibold flex items-center space-x-1 transition-colors disabled:opacity-40 disabled:cursor-not-allowed ${
                  isSelectedForCompare
                    ? 'bg-brand-teal/20 text-brand-tealLight border-brand-teal/40'
                    : 'border-navy-700 text-slate-400 hover:text-white hover:bg-navy-800'
                }`}
              >
                <GitCompare className="w-3.5 h-3.5" />
                <span>{isSelectedForCompare ? 'Selected' : 'Compare'}</span>
              </button>
            )}

            <span
              className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${
                matchPercent >= 80
                  ? 'bg-brand-teal/15 text-brand-tealLight border-brand-teal/30'
                  : matchPercent >= 60
                  ? 'bg-brand-violet/15 text-brand-violetLight border-brand-violet/30'
                  : 'bg-slate-700/30 text-slate-300 border-slate-600'
              }`}
            >
              {matchPercent}% Score
            </span>

            <button
              onClick={() => onSave(opportunity.id)}
              title={isSaved ? 'Saved' : 'Save Opportunity'}
              className={`p-1.5 rounded-lg border transition-colors ${
                isSaved
                  ? 'bg-brand-violet text-white border-brand-violet'
                  : 'border-navy-700 text-slate-400 hover:text-white hover:bg-navy-800'
              }`}
            >
              <Bookmark className="w-4 h-4 fill-current" />
            </button>

            <button
              onClick={() => onDismiss(opportunity.id)}
              title="Dismiss Opportunity"
              className="p-1.5 rounded-lg border border-navy-700 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 hover:border-rose-500/30 transition-colors"
            >
              <XCircle className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Evidence / Cold-Start Basis Indicator */}
        {explanation.confidenceLabel && (
          <div className="mb-2.5 flex items-center justify-between gap-2">
            <span
              title={explanation.confidenceNote || explanation.confidenceLabel}
              className={`inline-flex items-center space-x-1 px-2 py-0.5 rounded-md text-[10px] font-semibold border ${getConfidenceStyle()}`}
            >
              <ShieldAlert className="w-3 h-3" />
              <span>{explanation.confidenceLabel}</span>
            </span>
          </div>
        )}

        {/* Opportunity Title & Provider */}
        <h3 className="text-base font-bold text-white mb-1 hover:text-brand-violetLight transition-colors">
          {opportunity.title}
        </h3>
        <p className="text-xs text-slate-400 font-medium mb-3">
          Provided by <span className="text-slate-300">{opportunity.provider}</span> •{' '}
          <span className="text-brand-tealLight font-semibold">
            {opportunity.cost === 0 ? 'Free' : `$${opportunity.cost.toFixed(2)}`}
          </span>{' '}
          • ~{opportunity.estimatedHours} hrs total
        </p>

        {/* Opportunity Description */}
        <p className="text-xs text-slate-300 line-clamp-2 mb-4 leading-relaxed">
          {opportunity.description}
        </p>

        {/* Skills Gained Tags */}
        <div className="mb-4">
          <p className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold mb-1.5">
            Key Competencies Taught
          </p>
          <div className="flex flex-wrap gap-1.5">
            {opportunity.skillsGained && opportunity.skillsGained.length > 0 ? (
              opportunity.skillsGained.map((s) => (
                <span
                  key={s.id}
                  className="px-2 py-0.5 rounded text-[11px] font-medium bg-navy-800 text-slate-200 border border-navy-700 flex items-center space-x-1"
                >
                  <CheckCircle className="w-3 h-3 text-brand-teal" />
                  <span>{s.name}</span>
                </span>
              ))
            ) : (
              <span className="text-xs text-slate-500 italic">Core software engineering concepts</span>
            )}
          </div>
        </div>

        {/* Explainability Callouts */}
        <div className="bg-navy-950/70 border border-navy-800/80 rounded-lg p-3 mb-4 space-y-2 text-xs">
          {explanation.careerRelevanceReason && (
            <div className="flex items-start space-x-2 text-slate-300">
              <Award className="w-3.5 h-3.5 text-brand-violetLight flex-shrink-0 mt-0.5" />
              <span>
                <strong className="text-white">Why Recommended:</strong> {explanation.careerRelevanceReason}
              </span>
            </div>
          )}

          {explanation.timeAndBudgetFitReason && (
            <div className="flex items-start space-x-2 text-slate-300">
              <Clock className="w-3.5 h-3.5 text-brand-teal flex-shrink-0 mt-0.5" />
              <span>
                <strong className="text-white">Pacing & Budget:</strong> {explanation.timeAndBudgetFitReason}
              </span>
            </div>
          )}

          {explanation.prerequisitesReason && (
            <div className="flex items-start space-x-2 text-slate-300">
              <CheckCircle className="w-3.5 h-3.5 text-emerald-400 flex-shrink-0 mt-0.5" />
              <span>
                <strong className="text-white">Prerequisites:</strong> {explanation.prerequisitesReason}
              </span>
            </div>
          )}

          {explanation.confidenceNote && (
            <div className="pt-1 border-t border-navy-800/70 text-[11px] text-slate-400">
              <strong className="text-slate-300">Data Basis:</strong> {explanation.confidenceNote}
            </div>
          )}
        </div>

        {/* Score Breakdown Accordion */}
        <div className="mb-4">
          <button
            onClick={() => setShowBreakdown(!showBreakdown)}
            className="flex items-center justify-between w-full text-xs text-slate-400 hover:text-slate-200 py-1"
          >
            <span className="font-semibold">Algorithmic Scoring Breakdown</span>
            {showBreakdown ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>

          {showBreakdown && (
            <div className="mt-2.5 grid grid-cols-2 gap-2 text-[11px] bg-navy-800/50 p-2.5 rounded-lg border border-navy-700/60">
              <div>
                <span className="text-slate-400">Content Semantic Match:</span>
                <span className="ml-1 text-white font-mono font-semibold">
                  {(contentScore * 100).toFixed(0)}%
                </span>
              </div>
              <div>
                <span className="text-slate-400">Skill Gap Match:</span>
                <span className="ml-1 text-brand-teal font-mono font-semibold">
                  {(skillScore * 100).toFixed(0)}%
                </span>
              </div>
              <div>
                <span className="text-slate-400">Context & Difficulty:</span>
                <span className="ml-1 text-brand-violetLight font-mono font-semibold">
                  {(contextualScore * 100).toFixed(0)}%
                </span>
              </div>
              <div>
                <span className="text-slate-400">Feedback & Bayesian:</span>
                <span className="ml-1 text-amber-400 font-mono font-semibold">
                  {(feedbackScore * 100).toFixed(0)}%
                </span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Action Bar: Rating, Add to Roadmap, External Link */}
      <div className="pt-3 border-t border-navy-800 flex items-center justify-between gap-2">
        {/* Star Rating Feedback */}
        <div className="flex items-center space-x-1" title="Rate this recommendation">
          {[1, 2, 3, 4, 5].map((star) => (
            <button
              key={star}
              onClick={() => {
                setUserRating(star);
                onRate(opportunity.id, star);
              }}
              className="p-0.5 hover:scale-110 transition-transform"
            >
              <Star
                className={`w-3.5 h-3.5 ${
                  (userRating || Math.round(opportunity.rating)) >= star
                    ? 'text-amber-400 fill-amber-400'
                    : 'text-slate-600'
                }`}
              />
            </button>
          ))}
        </div>

        <div className="flex items-center space-x-2">
          {onAddToRoadmap && (
            <button
              onClick={() => onAddToRoadmap(opportunity.id)}
              className="px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-navy-800 hover:bg-navy-700 text-slate-200 border border-navy-700 flex items-center space-x-1 transition-colors"
            >
              <PlusCircle className="w-3.5 h-3.5 text-brand-teal" />
              <span>Add to Roadmap</span>
            </button>
          )}

          {opportunity.url && (
            <a
              href={opportunity.url}
              target="_blank"
              rel="noopener noreferrer"
              className="p-1.5 rounded-lg text-xs font-semibold bg-brand-violet hover:bg-brand-violetLight text-white flex items-center transition-colors"
              title="Open Opportunity Website"
            >
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
          )}
        </div>
      </div>
    </div>
  );
};
