import React, { useState, useEffect } from 'react';
import { StudentProfile, LearningPlanItem, PlanStatus, WeeklyProgressSummary } from '../types';
import { roadmapApi } from '../api/client';
import {
  Map,
  CheckCircle2,
  Clock,
  Calendar,
  Sparkles,
  ExternalLink,
  RefreshCw,
  Trash2,
  Award,
  BookOpen,
  Code2,
  Briefcase,
  Download,
  Printer,
  TrendingUp,
} from 'lucide-react';

interface RoadmapPageProps {
  student: StudentProfile | null;
}

export const RoadmapPage: React.FC<RoadmapPageProps> = ({ student }) => {
  const [items, setItems] = useState<LearningPlanItem[]>([]);
  const [summary, setSummary] = useState<WeeklyProgressSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const [regenerating, setRegenerating] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const fetchRoadmap = async () => {
    if (!student) return;
    try {
      setLoading(true);
      const [data, summaryData] = await Promise.all([
        roadmapApi.getRoadmap(student.id),
        roadmapApi.getSummary(student.id),
      ]);
      setItems(data);
      setSummary(summaryData);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const refreshSummaryOnly = async () => {
    if (!student) return;
    try {
      const summaryData = await roadmapApi.getSummary(student.id);
      setSummary(summaryData);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchRoadmap();
  }, [student?.id]);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleStatusChange = async (itemId: number, newStatus: PlanStatus) => {
    try {
      const updated = await roadmapApi.updateItem(itemId, newStatus);
      setItems((prev) => prev.map((it) => (it.id === itemId ? updated : it)));
      await refreshSummaryOnly();
      showToast(`Milestone updated to ${newStatus.replace('_', ' ')}!`);
    } catch (err: any) {
      showToast(err.message || 'Error updating milestone status');
    }
  };

  const handleDeleteItem = async (itemId: number) => {
    try {
      await roadmapApi.deleteItem(itemId);
      setItems((prev) => prev.filter((it) => it.id !== itemId));
      await refreshSummaryOnly();
      showToast('Milestone removed from roadmap');
    } catch (err: any) {
      showToast(err.message || 'Error deleting milestone');
    }
  };

  const handleRegenerate = async () => {
    if (!student) return;
    try {
      setRegenerating(true);
      const data = await roadmapApi.generateRoadmap(student.id);
      setItems(data);
      await refreshSummaryOnly();
      showToast('Roadmap regenerated based on latest skill gaps!');
    } catch (err: any) {
      showToast(err.message || 'Failed to regenerate roadmap');
    } finally {
      setRegenerating(false);
    }
  };

  const handleExportCsv = async () => {
    if (!student) return;
    try {
      setExporting(true);
      await roadmapApi.exportCsv(student.id);
      showToast('Exported personalized learning roadmap CSV!');
    } catch (err: any) {
      showToast(err.message || 'Failed to export roadmap CSV');
    } finally {
      setExporting(false);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const completedCount = summary ? summary.completedItems : items.filter((it) => it.status === 'COMPLETED').length;
  const totalCount = summary ? summary.totalItems : items.length;
  const progressPercent = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0;
  const remainingWeeks = summary
    ? summary.remainingWeeks
    : items.reduce((acc, it) => acc + (it.status !== 'SKIPPED' && it.status !== 'COMPLETED' ? it.targetCompletionWeeks : 0), 0);

  const getTypeIcon = (type?: string) => {
    switch (type) {
      case 'COURSE': return <BookOpen className="w-4 h-4 text-blue-400" />;
      case 'PROJECT': return <Code2 className="w-4 h-4 text-emerald-400" />;
      case 'INTERNSHIP': return <Briefcase className="w-4 h-4 text-brand-violetLight" />;
      case 'CERTIFICATION': return <Award className="w-4 h-4 text-amber-400" />;
      default: return <Sparkles className="w-4 h-4 text-brand-teal" />;
    }
  };

  const getStatusBadge = (status: PlanStatus) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'IN_PROGRESS':
        return 'bg-brand-violet/15 text-brand-violetLight border-brand-violet/30';
      case 'PENDING':
        return 'bg-slate-800 text-slate-400 border-navy-700';
      case 'SKIPPED':
        return 'bg-navy-950 text-slate-500 border-navy-800 line-through';
    }
  };

  return (
    <div className="space-y-6">
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 bg-brand-violet text-white px-4 py-2.5 rounded-xl shadow-2xl text-xs font-semibold flex items-center space-x-2 animate-bounce">
          <Sparkles className="w-4 h-4 text-brand-teal" />
          <span>{toast}</span>
        </div>
      )}

      {/* Header Banner */}
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-xs text-brand-teal font-semibold uppercase tracking-wider mb-1">
              <Map className="w-4 h-4" />
              <span>Personalized Learning Trajectory</span>
            </div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight">
              Learning Roadmap & Action Plan
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Sequenced milestone path structured to close skill gaps for <span className="text-white font-bold">{student?.targetRole}</span> while respecting your weekly availability.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2 no-print">
            <button
              onClick={handleExportCsv}
              disabled={exporting || items.length === 0}
              className="px-3.5 py-2.5 bg-navy-950 hover:bg-navy-800 border border-navy-700 text-slate-200 rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all disabled:opacity-40"
            >
              <Download className="w-3.5 h-3.5 text-brand-teal" />
              <span>{exporting ? 'Exporting...' : 'Export CSV'}</span>
            </button>

            <button
              onClick={handlePrint}
              disabled={items.length === 0}
              className="px-3.5 py-2.5 bg-navy-950 hover:bg-navy-800 border border-navy-700 text-slate-200 rounded-xl text-xs font-semibold flex items-center space-x-1.5 transition-all disabled:opacity-40"
            >
              <Printer className="w-3.5 h-3.5 text-brand-violetLight" />
              <span>Print / PDF</span>
            </button>

            <button
              onClick={handleRegenerate}
              disabled={regenerating}
              className="px-4 py-2.5 bg-brand-violet hover:bg-brand-violetLight text-white rounded-xl text-xs font-bold flex items-center space-x-2 transition-all shadow-md shadow-brand-violet/25 disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${regenerating ? 'animate-spin' : ''}`} />
              <span>Regenerate Trajectory</span>
            </button>
          </div>
        </div>

        {/* Progress Bar & Weekly Progress Summary */}
        <div className="mt-6 pt-5 border-t border-navy-800/80 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-navy-950/70 border border-navy-800 rounded-xl px-4 py-3">
            <div className="flex items-center justify-between text-xs mb-1.5">
              <span className="text-slate-400 font-medium">Trajectory Completion</span>
              <span className="text-white font-bold">{progressPercent}%</span>
            </div>
            <div className="w-full bg-navy-900 h-2.5 rounded-full overflow-hidden border border-navy-800">
              <div
                className="bg-gradient-to-r from-brand-violet to-brand-teal h-full rounded-full transition-all duration-500"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <div className="text-[10px] text-slate-400 mt-1.5">
              {completedCount} of {totalCount} milestones completed
            </div>
          </div>

          <div className="bg-navy-950/70 border border-navy-800 rounded-xl px-4 py-3 text-xs flex items-center space-x-3">
            <Clock className="w-4 h-4 text-brand-teal flex-shrink-0" />
            <div>
              <div className="text-[10px] text-slate-400">Study Hours Pacing</div>
              <div className="font-bold text-white">
                {summary ? `${summary.completedHours} done / ${summary.remainingHours} hrs left` : `~${remainingWeeks} weeks`}
              </div>
              <div className="text-[10px] text-slate-400">
                ~{remainingWeeks} wks at {student?.weeklyHoursAvailable} hrs/wk
              </div>
            </div>
          </div>

          <div className="bg-navy-950/70 border border-navy-800 rounded-xl px-4 py-3 text-xs flex items-center space-x-3">
            <TrendingUp className="w-4 h-4 text-brand-violetLight flex-shrink-0" />
            <div>
              <div className="text-[10px] text-slate-400">Projected Role Readiness</div>
              <div className="font-bold text-white">
                {summary
                  ? `${summary.currentRoleMatchPercentage.toFixed(0)}% → ${summary.projectedRoleMatchPercentage.toFixed(0)}%`
                  : 'Calculating...'}
              </div>
              <div className="text-[10px] text-emerald-400 font-semibold">
                {summary && summary.estimatedSkillCoverageGain > 0
                  ? `+${summary.estimatedSkillCoverageGain.toFixed(1)}% projected gain`
                  : 'Complete active steps to raise score'}
              </div>
            </div>
          </div>

          <div className="bg-navy-950/70 border border-navy-800 rounded-xl px-4 py-3 text-xs flex items-center space-x-3">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 flex-shrink-0" />
            <div>
              <div className="text-[10px] text-slate-400">Active & Queue Status</div>
              <div className="font-bold text-white">
                {summary
                  ? `${summary.inProgressItems} Active • ${summary.pendingItems} Pending`
                  : `${items.length} Total Steps`}
              </div>
              <div className="text-[10px] text-slate-400">
                {summary ? `${summary.skippedItems} skipped` : 'Interactive status tracking'}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Sequenced Milestones */}
      {loading ? (
        <div className="text-center py-20 bg-navy-900 border border-navy-800 rounded-2xl">
          <RefreshCw className="w-8 h-8 text-brand-violet animate-spin mx-auto mb-3" />
          <p className="text-xs text-slate-300">Loading structured milestones...</p>
        </div>
      ) : items.length === 0 ? (
        <div className="text-center py-16 bg-navy-900 border border-navy-800 rounded-2xl">
          <Map className="w-10 h-10 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-white">No Roadmap Generated Yet</h3>
          <p className="text-xs text-slate-400 mt-1 mb-4">
            Click regenerate to assemble an achievable, sequenced plan tailored to your skill gaps.
          </p>
          <button
            onClick={handleRegenerate}
            className="px-4 py-2 bg-brand-violet text-white text-xs font-bold rounded-xl"
          >
            Generate Roadmap
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {items.map((item, index) => {
            const opp = item.opportunity;
            return (
              <div
                key={item.id}
                className="bg-navy-900 border border-navy-800 rounded-2xl p-5 glow-card relative flex flex-col md:flex-row md:items-center justify-between gap-5"
              >
                {/* Step Index Circle & Title */}
                <div className="flex items-start space-x-4">
                  <div className="w-9 h-9 rounded-xl bg-navy-950 border border-navy-800 flex items-center justify-center font-extrabold text-sm text-brand-violetLight flex-shrink-0 mt-0.5 shadow-inner">
                    {item.sequenceOrder}
                  </div>

                  <div>
                    <div className="flex flex-wrap items-center gap-2 mb-1">
                      <span className="flex items-center space-x-1 text-xs text-slate-400 font-semibold">
                        {getTypeIcon(opp?.opportunityType)}
                        <span>{opp?.opportunityType}</span>
                      </span>
                      <span className="text-slate-600">•</span>
                      <span className="text-xs text-slate-400">{opp?.provider}</span>
                      <span className="text-slate-600">•</span>
                      <span className="text-xs text-brand-teal font-semibold">
                        {opp?.cost === 0 ? 'Free' : `$${opp?.cost.toFixed(2)}`}
                      </span>
                    </div>

                    <h3 className="text-base font-bold text-white hover:text-brand-violetLight transition-colors">
                      {opp?.title || 'Learning Opportunity Milestone'}
                    </h3>

                    <p className="text-xs text-slate-300 mt-1 leading-relaxed max-w-2xl">
                      {opp?.description || item.prerequisiteNote}
                    </p>

                    {/* Skills Gained in this milestone */}
                    {opp?.skillsGained && opp.skillsGained.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mt-2.5">
                        {opp.skillsGained.map((s) => (
                          <span
                            key={s.id}
                            className="px-2 py-0.5 rounded text-[10px] font-semibold bg-navy-950 text-slate-300 border border-navy-800"
                          >
                            + {s.name}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {/* Status Selector & Milestone Meta */}
                <div className="flex flex-wrap md:flex-col items-end gap-3 flex-shrink-0 pt-3 md:pt-0 border-t md:border-t-0 border-navy-800">
                  <div className="flex items-center space-x-2">
                    <span className="text-[11px] text-slate-400 flex items-center space-x-1">
                      <Calendar className="w-3.5 h-3.5 text-brand-teal" />
                      <span>Target: {item.targetCompletionWeeks} Wks</span>
                    </span>

                    <span className={`px-2.5 py-1 rounded-lg text-xs font-bold border uppercase tracking-wider ${getStatusBadge(item.status)}`}>
                      {item.status.replace('_', ' ')}
                    </span>
                  </div>

                  {/* Status Toggle Controls */}
                  <div className="flex items-center space-x-1.5 no-print">
                    <button
                      onClick={() => handleStatusChange(item.id, 'IN_PROGRESS')}
                      title="Mark In Progress"
                      className={`px-2 py-1 rounded text-[11px] font-semibold border transition-colors ${
                        item.status === 'IN_PROGRESS'
                          ? 'bg-brand-violet text-white border-brand-violet'
                          : 'bg-navy-950 text-slate-400 border-navy-800 hover:text-white'
                      }`}
                    >
                      In Progress
                    </button>

                    <button
                      onClick={() => handleStatusChange(item.id, 'COMPLETED')}
                      title="Mark Completed"
                      className={`px-2 py-1 rounded text-[11px] font-semibold border transition-colors ${
                        item.status === 'COMPLETED'
                          ? 'bg-emerald-500 text-white border-emerald-500'
                          : 'bg-navy-950 text-slate-400 border-navy-800 hover:text-white'
                      }`}
                    >
                      Done
                    </button>

                    <button
                      onClick={() => handleStatusChange(item.id, 'SKIPPED')}
                      title="Skip Milestone"
                      className={`px-2 py-1 rounded text-[11px] font-semibold border transition-colors ${
                        item.status === 'SKIPPED'
                          ? 'bg-slate-700 text-white border-slate-600'
                          : 'bg-navy-950 text-slate-500 border-navy-800 hover:text-white'
                      }`}
                    >
                      Skip
                    </button>

                    {opp?.url && (
                      <a
                        href={opp.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-1.5 bg-navy-950 border border-navy-800 hover:border-brand-violet rounded text-slate-400 hover:text-white"
                        title="Open Resource"
                      >
                        <ExternalLink className="w-3.5 h-3.5" />
                      </a>
                    )}

                    <button
                      onClick={() => handleDeleteItem(item.id)}
                      title="Remove from Roadmap"
                      className="p-1.5 bg-navy-950 border border-navy-800 hover:border-rose-500/50 rounded text-slate-500 hover:text-rose-400 transition-colors"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
