import React, { useState, useEffect } from 'react';
import {
  DifficultyLevel,
  ProficiencyLevel,
  Skill,
  StudentProfile,
  WhatIfSimulationResult,
} from '../types';
import { catalogApi, recommendationApi, studentApi } from '../api/client';
import {
  Sliders,
  Play,
  ArrowUpRight,
  ArrowDownRight,
  Sparkles,
  Check,
  Save,
  RotateCcw,
  AlertTriangle,
  ShieldCheck,
} from 'lucide-react';

interface SimulatorPageProps {
  student: StudentProfile | null;
  onProfileUpdated: (updated: StudentProfile) => void;
}

export const SimulatorPage: React.FC<SimulatorPageProps> = ({ student, onProfileUpdated }) => {
  const [roles, setRoles] = useState<string[]>([]);
  const [availableSkills, setAvailableSkills] = useState<Skill[]>([]);

  const [targetRole, setTargetRole] = useState('');
  const [weeklyHours, setWeeklyHours] = useState(15);
  const [maxBudget, setMaxBudget] = useState(100);
  const [difficulty, setDifficulty] = useState<DifficultyLevel>('INTERMEDIATE');
  const [applyDiversity, setApplyDiversity] = useState(true);
  const [simulatedSkills, setSimulatedSkills] = useState<
    { skillId: number; skillName: string; proficiencyLevel: ProficiencyLevel; yearsExperience: number }[]
  >([]);

  const [simulation, setSimulation] = useState<WhatIfSimulationResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [applying, setApplying] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  useEffect(() => {
    catalogApi.getRoles().then(setRoles).catch(console.error);
    catalogApi.getSkills().then(setAvailableSkills).catch(console.error);
  }, []);

  const resetToProfile = () => {
    if (!student) return;
    setTargetRole(student.targetRole);
    setWeeklyHours(student.weeklyHoursAvailable);
    setMaxBudget(student.maxBudget);
    setDifficulty(student.preferredDifficulty);
    setSimulatedSkills(
      (student.skillsList || []).map((ss) => ({
        skillId: ss.skill.id,
        skillName: ss.skill.name,
        proficiencyLevel: ss.proficiencyLevel,
        yearsExperience: ss.yearsExperience ?? 1.0,
      }))
    );
    setSimulation(null);
    setError(null);
  };

  useEffect(() => {
    resetToProfile();
  }, [student?.id]);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3500);
  };

  const toggleSkill = (skill: Skill) => {
    const exists = simulatedSkills.some((s) => s.skillId === skill.id);
    if (exists) {
      setSimulatedSkills(simulatedSkills.filter((s) => s.skillId !== skill.id));
    } else {
      setSimulatedSkills([
        ...simulatedSkills,
        {
          skillId: skill.id,
          skillName: skill.name,
          proficiencyLevel: 'INTERMEDIATE',
          yearsExperience: 1.0,
        },
      ]);
    }
  };

  const updateSkillLevel = (skillId: number, level: ProficiencyLevel) => {
    setSimulatedSkills(
      simulatedSkills.map((s) => (s.skillId === skillId ? { ...s, proficiencyLevel: level } : s))
    );
  };

  const runSimulation = async () => {
    if (!student) return;
    setLoading(true);
    setError(null);
    try {
      const res = await recommendationApi.simulate(student.id, {
        targetRole,
        weeklyHoursAvailable: weeklyHours,
        maxBudget,
        preferredDifficulty: difficulty,
        simulatedSkills: simulatedSkills.map((s) => ({
          skillId: s.skillId,
          skillName: s.skillName,
          proficiencyLevel: s.proficiencyLevel,
          yearsExperience: s.yearsExperience,
        })),
        limit: 8,
        applyDiversity,
      });
      setSimulation(res);
    } catch (err: any) {
      setError(err.message || 'Simulation failed');
    } finally {
      setLoading(false);
    }
  };

  const applySimulationToProfile = async () => {
    if (!student) return;
    setApplying(true);
    setError(null);
    try {
      const updated = await studentApi.update(student.id, {
        fullName: student.fullName,
        targetRole,
        weeklyHoursAvailable: weeklyHours,
        maxBudget,
        preferredDifficulty: difficulty,
        bio: student.bio || '',
        skills: simulatedSkills,
      });
      onProfileUpdated(updated);
      showToast('Simulation scenario saved permanently to your profile!');
    } catch (err: any) {
      setError(err.message || 'Failed to apply simulation changes to profile');
    } finally {
      setApplying(false);
    }
  };

  if (!student) return null;

  return (
    <div className="space-y-6">
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 bg-emerald-600 text-white px-4 py-2.5 rounded-xl shadow-2xl text-xs font-semibold flex items-center space-x-2">
          <Sparkles className="w-4 h-4" />
          <span>{toast}</span>
        </div>
      )}

      {/* Header */}
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-xs text-brand-teal font-semibold uppercase tracking-wider mb-1">
              <Sliders className="w-4 h-4" />
              <span>Non-Destructive What-If Scenario Sandbox</span>
            </div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight">
              Career & Constraint Simulator
            </h1>
            <p className="text-xs text-slate-400 mt-1 max-w-2xl">
              Experiment with target roles, weekly study pacing, budget ceilings, and hypothetical skills to preview how role readiness and rankings shift—without altering your stored profile until you explicitly click Apply.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={resetToProfile}
              className="px-3.5 py-2 rounded-xl bg-navy-950 hover:bg-navy-800 border border-navy-700 text-xs font-semibold text-slate-300 flex items-center space-x-1.5"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset to Profile</span>
            </button>

            <button
              type="button"
              onClick={runSimulation}
              disabled={loading}
              className="px-4 py-2 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white text-xs font-bold flex items-center space-x-1.5 shadow-md shadow-brand-violet/25 disabled:opacity-50"
            >
              <Play className="w-3.5 h-3.5" />
              <span>{loading ? 'Simulating...' : 'Run What-If Simulation'}</span>
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-300 text-xs flex items-center space-x-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Controls Panel */}
        <div className="bg-navy-900 border border-navy-800 rounded-2xl p-5 space-y-4 text-xs">
          <h2 className="text-sm font-bold text-white border-b border-navy-800 pb-3">
            Hypothetical Profile Parameters
          </h2>

          <div>
            <label className="block text-slate-300 font-semibold mb-1">Simulated Target Role</label>
            <select
              aria-label="Simulated Target Career Role"
              value={targetRole}
              onChange={(e) => setTargetRole(e.target.value)}
              className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
            >
              {roles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          <div>
            <div className="flex justify-between mb-1">
              <label className="text-slate-300 font-semibold">Weekly Study Hours</label>
              <span className="text-brand-teal font-bold">{weeklyHours} hrs/wk</span>
            </div>
            <input
              type="range"
              min={2}
              max={60}
              value={weeklyHours}
              onChange={(e) => setWeeklyHours(Number(e.target.value))}
              className="w-full accent-brand-violet"
            />
          </div>

          <div>
            <div className="flex justify-between mb-1">
              <label className="text-slate-300 font-semibold">Max Budget ($ USD)</label>
              <span className="text-amber-400 font-bold">${maxBudget.toFixed(0)}</span>
            </div>
            <input
              type="range"
              min={0}
              max={600}
              step={10}
              value={Math.min(600, maxBudget)}
              onChange={(e) => setMaxBudget(Number(e.target.value))}
              className="w-full accent-brand-teal"
            />
            <input
              type="number"
              min={0}
              value={maxBudget}
              onChange={(e) => setMaxBudget(Math.max(0, Number(e.target.value)))}
              className="mt-2 w-full bg-navy-950 border border-navy-700 rounded-xl px-3 py-1.5 text-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-slate-300 font-semibold mb-1">Difficulty Preference</label>
              <select
                aria-label="Simulated Preferred Difficulty"
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as DifficultyLevel)}
                className="w-full bg-navy-950 border border-navy-700 rounded-xl px-2.5 py-2 text-white"
              >
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="ADVANCED">Advanced</option>
                <option value="EXPERT">Expert</option>
              </select>
            </div>

            <div className="flex items-end">
              <label className="w-full flex items-center space-x-2 p-2 rounded-xl bg-navy-950 border border-navy-700 cursor-pointer">
                <input
                  type="checkbox"
                  checked={applyDiversity}
                  onChange={(e) => setApplyDiversity(e.target.checked)}
                  className="rounded accent-brand-teal"
                />
                <span className="text-[11px] font-semibold text-slate-200">MMR Diversity</span>
              </label>
            </div>
          </div>

          <div>
            <label className="block text-slate-300 font-semibold mb-1.5">
              Hypothetical Skills ({simulatedSkills.length} active)
            </label>
            <div className="flex flex-wrap gap-1.5 max-h-36 overflow-y-auto p-2 bg-navy-950 border border-navy-800 rounded-xl">
              {availableSkills.map((skill) => {
                const selected = simulatedSkills.some((s) => s.skillId === skill.id);
                return (
                  <button
                    key={skill.id}
                    type="button"
                    onClick={() => toggleSkill(skill)}
                    className={`px-2 py-1 rounded-lg text-[11px] font-semibold flex items-center space-x-1 transition-all ${
                      selected
                        ? 'bg-brand-violet text-white'
                        : 'bg-navy-900 text-slate-400 border border-navy-800 hover:text-white'
                    }`}
                  >
                    {selected && <Check className="w-3 h-3" />}
                    <span>{skill.name}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {simulatedSkills.length > 0 && (
            <div className="space-y-1.5 max-h-40 overflow-y-auto">
              {simulatedSkills.map((sk) => (
                <div
                  key={sk.skillId}
                  className="flex items-center justify-between px-2.5 py-1.5 bg-navy-950 border border-navy-800 rounded-lg"
                >
                  <span className="text-slate-200 font-medium">{sk.skillName}</span>
                  <select
                    aria-label={`Simulated level for ${sk.skillName}`}
                    value={sk.proficiencyLevel}
                    onChange={(e) => updateSkillLevel(sk.skillId, e.target.value as ProficiencyLevel)}
                    className="bg-navy-900 border border-navy-700 text-[11px] text-brand-teal font-semibold rounded px-2 py-0.5"
                  >
                    <option value="BEGINNER">Beginner</option>
                    <option value="INTERMEDIATE">Intermediate</option>
                    <option value="ADVANCED">Advanced</option>
                    <option value="EXPERT">Expert</option>
                  </select>
                </div>
              ))}
            </div>
          )}

          {simulation && (
            <button
              type="button"
              onClick={applySimulationToProfile}
              disabled={applying}
              className="w-full py-2.5 rounded-xl bg-brand-teal/20 hover:bg-brand-teal/30 border border-brand-teal/40 text-brand-tealLight font-bold flex items-center justify-center space-x-1.5"
            >
              <Save className="w-4 h-4" />
              <span>{applying ? 'Applying...' : 'Apply Simulation to My Profile'}</span>
            </button>
          )}
        </div>

        {/* Simulation Outcome Panel */}
        <div className="lg:col-span-2 space-y-5">
          {!simulation ? (
            <div className="bg-navy-900 border border-navy-800 rounded-2xl p-12 text-center">
              <Sliders className="w-10 h-10 text-brand-violetLight mx-auto mb-3 opacity-80" />
              <h3 className="text-base font-bold text-white">Adjust Parameters & Run Simulation</h3>
              <p className="text-xs text-slate-400 mt-1.5 max-w-md mx-auto leading-relaxed">
                Add skills you plan to learn next, explore switching career tracks, or adjust study hours and budget—then compare baseline vs simulated readiness and ranking deltas in real time.
              </p>
              <button
                type="button"
                onClick={runSimulation}
                className="mt-5 px-5 py-2.5 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white text-xs font-bold inline-flex items-center space-x-2"
              >
                <Play className="w-3.5 h-3.5" />
                <span>Simulate Now</span>
              </button>
            </div>
          ) : (
            <>
              {/* Summary Delta Cards */}
              <div className="bg-navy-900 border border-navy-800 rounded-2xl p-5">
                <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                  <div className="flex items-center space-x-2">
                    <ShieldCheck className="w-4 h-4 text-emerald-400" />
                    <span className="text-xs font-semibold text-emerald-300">
                      Preview Mode • Database Persisted: {simulation.persisted ? 'Yes' : 'No (Safe Sandbox)'}
                    </span>
                  </div>
                  <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-navy-950 border border-navy-700 text-slate-300">
                    Evidence Basis: {simulation.relevanceBasis.replace(/_/g, ' ')}
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                  <div className="bg-navy-950/70 border border-navy-800 rounded-xl p-3.5">
                    <div className="text-[10px] text-slate-400 uppercase font-bold">Role Readiness Shift</div>
                    <div className="text-xl font-extrabold text-white mt-1 flex items-center space-x-2">
                      <span>{simulation.baselineRoleMatchPercentage.toFixed(1)}%</span>
                      <span className="text-slate-500">→</span>
                      <span className="text-brand-tealLight">{simulation.simulatedRoleMatchPercentage.toFixed(1)}%</span>
                    </div>
                    <div
                      className={`text-[11px] font-bold mt-1 ${
                        simulation.roleMatchDelta > 0
                          ? 'text-emerald-400'
                          : simulation.roleMatchDelta < 0
                          ? 'text-rose-400'
                          : 'text-slate-400'
                      }`}
                    >
                      {simulation.roleMatchDelta >= 0 ? '+' : ''}
                      {simulation.roleMatchDelta.toFixed(1)}% vs current profile
                    </div>
                  </div>

                  <div className="bg-navy-950/70 border border-navy-800 rounded-xl p-3.5">
                    <div className="text-[10px] text-slate-400 uppercase font-bold">Role Comparison</div>
                    <div className="text-xs text-slate-400 mt-1">Baseline: {simulation.baselineTargetRole}</div>
                    <div className="text-sm font-bold text-white mt-0.5">
                      Simulated: {simulation.simulatedTargetRole}
                    </div>
                  </div>

                  <div className="bg-navy-950/70 border border-navy-800 rounded-xl p-3.5">
                    <div className="text-[10px] text-slate-400 uppercase font-bold">Simulated Constraints</div>
                    <div className="text-xs text-slate-300 mt-1">
                      Hours: <strong className="text-white">{simulation.simulatedWeeklyHours} hrs/wk</strong> (was {simulation.baselineWeeklyHours})
                    </div>
                    <div className="text-xs text-slate-300 mt-0.5">
                      Budget: <strong className="text-white">${simulation.simulatedMaxBudget.toFixed(0)}</strong> (was ${simulation.baselineMaxBudget.toFixed(0)})
                    </div>
                  </div>
                </div>
              </div>

              {/* Ranked Delta Cards */}
              <div className="space-y-3">
                {simulation.items.map((deltaItem) => {
                  const opp = deltaItem.item.opportunity;
                  const scorePct = Math.round(deltaItem.item.rankingScore * 100);
                  const deltaPct = Math.round(deltaItem.scoreDelta * 100);

                  return (
                    <div
                      key={opp.id}
                      className="bg-navy-900 border border-navy-800 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                    >
                      <div className="space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="w-6 h-6 rounded-md bg-navy-950 border border-navy-700 text-xs font-extrabold text-brand-violetLight flex items-center justify-center">
                            #{deltaItem.simulatedRank}
                          </span>
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-navy-800 text-slate-300 border border-navy-700">
                            {opp.opportunityType}
                          </span>
                          <span
                            className={`text-[10px] font-bold px-2 py-0.5 rounded border ${
                              deltaItem.changeStatus === 'NEW_ENTRY'
                                ? 'bg-brand-teal/15 text-brand-tealLight border-brand-teal/30'
                                : deltaItem.changeStatus === 'MOVED_UP'
                                ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30'
                                : deltaItem.changeStatus === 'MOVED_DOWN'
                                ? 'bg-amber-500/15 text-amber-300 border-amber-500/30'
                                : 'bg-navy-800 text-slate-400 border-navy-700'
                            }`}
                          >
                            {deltaItem.changeStatus.replace(/_/g, ' ')}
                          </span>
                        </div>

                        <h4 className="text-sm font-bold text-white">{opp.title}</h4>
                        <p className="text-[11px] text-slate-400">
                          {opp.provider} • {opp.cost === 0 ? 'Free' : `$${opp.cost.toFixed(0)}`} • ~{opp.estimatedHours} hrs •{' '}
                          {deltaItem.item.explanation.careerRelevanceReason}
                        </p>
                      </div>

                      <div className="flex items-center space-x-4 flex-shrink-0">
                        <div className="text-right">
                          <div className="text-base font-extrabold text-white">{scorePct}%</div>
                          <div
                            className={`text-[11px] font-bold flex items-center justify-end ${
                              deltaPct >= 0 ? 'text-emerald-400' : 'text-rose-400'
                            }`}
                          >
                            {deltaPct >= 0 ? (
                              <ArrowUpRight className="w-3.5 h-3.5" />
                            ) : (
                              <ArrowDownRight className="w-3.5 h-3.5" />
                            )}
                            <span>
                              {deltaPct >= 0 ? '+' : ''}
                              {deltaPct} pts
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
