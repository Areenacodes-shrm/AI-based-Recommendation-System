import React, { useState, useEffect } from 'react';
import { StudentProfile, SkillGapAnalysis } from '../types';
import { skillGapApi, catalogApi } from '../api/client';
import {
  Target,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  TrendingUp,
  Map,
  ArrowRight,
  ShieldCheck,
  Award
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
  RadarChart
} from 'recharts';

interface SkillGapPageProps {
  student: StudentProfile | null;
  onNavigateToRoadmap: () => void;
}

export const SkillGapPage: React.FC<SkillGapPageProps> = ({
  student,
  onNavigateToRoadmap,
}) => {
  const [targetRole, setTargetRole] = useState<string>(student?.targetRole || 'Java Backend Developer');
  const [availableRoles, setAvailableRoles] = useState<string[]>([]);
  const [analysis, setAnalysis] = useState<SkillGapAnalysis | null>(null);
  const [loading, setLoading] = useState(false);
  const [chartView, setChartView] = useState<'bar' | 'radar'>('bar');

  useEffect(() => {
    catalogApi.getRoles().then((roles) => {
      setAvailableRoles(roles);
      if (student?.targetRole && roles.includes(student.targetRole)) {
        setTargetRole(student.targetRole);
      }
    }).catch(console.error);
  }, [student?.targetRole]);

  useEffect(() => {
    if (!student) return;
    setLoading(true);
    skillGapApi.getSkillGap(student.id, targetRole)
      .then(setAnalysis)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [student?.id, targetRole]);

  const getNumericRank = (lvl: string | null) => {
    switch (lvl) {
      case 'EXPERT': return 4;
      case 'ADVANCED': return 3;
      case 'INTERMEDIATE': return 2;
      case 'BEGINNER': return 1;
      default: return 0;
    }
  };

  // Prepare chart comparison data
  const chartData = [
    ...(analysis?.matchedSkills || []),
    ...(analysis?.weakSkills || []),
    ...(analysis?.missingSkills || []),
  ].map((item) => ({
    skill: item.skillName,
    StudentProficiency: getNumericRank(item.currentProficiency),
    RequiredBenchmark: getNumericRank(item.requiredProficiency),
    isCore: item.isCore,
  }));

  return (
    <div className="space-y-6">
      {/* Role Selector & Overview Banner */}
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-xs text-brand-teal font-semibold uppercase tracking-wider mb-1">
              <Target className="w-4 h-4" />
              <span>Career Benchmark Diagnostic</span>
            </div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight">
              Skill Gap & Readiness Analysis
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Compare your current verified skills against industry standard competencies for your target role.
            </p>
          </div>

          {/* Role Switcher */}
          <div className="flex items-center space-x-3">
            <span className="text-xs text-slate-300 font-medium">Evaluate For Role:</span>
            <select
              aria-label="Target Career Role"
              value={targetRole}
              onChange={(e) => setTargetRole(e.target.value)}
              className="bg-navy-950 border border-navy-700 text-sm text-white font-semibold rounded-xl px-4 py-2 focus:ring-2 focus:ring-brand-violet focus:outline-none"
            >
              {availableRoles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Main Diagnostic Panel */}
      {analysis && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Readiness Score Card */}
          <div className="bg-navy-900 border border-navy-800 rounded-2xl p-6 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  Role Readiness
                </span>
                <span className="text-xs text-brand-violetLight font-mono">
                  {analysis.satisfiedSkillsCount} / {analysis.totalRequiredSkills} Skills Satisfied
                </span>
              </div>

              {/* Circular / Big Metric */}
              <div className="text-center py-6">
                <div className="relative inline-flex items-center justify-center">
                  <div className="w-36 h-36 rounded-full border-8 border-navy-950 flex flex-col items-center justify-center bg-navy-850 shadow-inner">
                    <span className="text-4xl font-extrabold text-white tracking-tight">
                      {analysis.matchPercentage}%
                    </span>
                    <span className="text-[10px] text-brand-teal uppercase tracking-wider font-bold mt-1">
                      Target Match
                    </span>
                  </div>
                </div>
              </div>

              <p className="text-xs text-slate-300 text-center leading-relaxed px-4">
                {analysis.matchPercentage >= 75
                  ? `Outstanding readiness for ${targetRole}. You are well positioned for intermediate & advanced roles.`
                  : analysis.matchPercentage >= 50
                  ? `Moderate alignment for ${targetRole}. Closing 2-3 core skill gaps will unlock high-tier opportunities.`
                  : `Early career progression for ${targetRole}. Follow your personalized roadmap to build foundational mastery.`}
              </p>
            </div>

            <button
              onClick={onNavigateToRoadmap}
              className="mt-6 w-full py-3 bg-brand-violet hover:bg-brand-violetLight text-white rounded-xl text-xs font-bold flex items-center justify-center space-x-2 transition-all shadow-lg shadow-brand-violet/25"
            >
              <Map className="w-4 h-4 text-brand-teal" />
              <span>Generate Step-by-Step Roadmap</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {/* Graphical Comparison Visualization */}
          <div className="lg:col-span-2 bg-navy-900 border border-navy-800 rounded-2xl p-6 flex flex-col justify-between">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-sm font-bold text-white">Proficiency Comparison</h3>
                <p className="text-xs text-slate-400">Scale: 1 = Beginner, 2 = Intermediate, 3 = Advanced, 4 = Expert</p>
              </div>

              <div className="flex items-center space-x-1 bg-navy-950 p-1 rounded-lg border border-navy-800 text-xs">
                <button
                  onClick={() => setChartView('bar')}
                  className={`px-3 py-1 rounded-md font-semibold transition-colors ${
                    chartView === 'bar' ? 'bg-brand-violet text-white' : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Bar Chart
                </button>
                <button
                  onClick={() => setChartView('radar')}
                  className={`px-3 py-1 rounded-md font-semibold transition-colors ${
                    chartView === 'radar' ? 'bg-brand-violet text-white' : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Radar
                </button>
              </div>
            </div>

            {/* Recharts Component */}
            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                {chartView === 'bar' ? (
                  <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 25 }}>
                    <XAxis
                      dataKey="skill"
                      stroke="#64748b"
                      fontSize={11}
                      tickLine={false}
                      angle={-25}
                      textAnchor="end"
                    />
                    <YAxis stroke="#64748b" fontSize={11} domain={[0, 4]} ticks={[0, 1, 2, 3, 4]} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0c1424', borderColor: '#223456', borderRadius: '8px', fontSize: '12px' }}
                      labelStyle={{ color: '#ffffff', fontWeight: 'bold' }}
                    />
                    <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
                    <Bar dataKey="StudentProficiency" name="Your Level" fill="#14b8a6" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="RequiredBenchmark" name="Required Target" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
                  </BarChart>
                ) : (
                  <RadarChart data={chartData}>
                    <PolarGrid stroke="#223456" />
                    <PolarAngleAxis dataKey="skill" stroke="#94a3b8" fontSize={11} />
                    <PolarRadiusAxis angle={30} domain={[0, 4]} stroke="#64748b" />
                    <Radar name="Your Level" dataKey="StudentProficiency" stroke="#14b8a6" fill="#14b8a6" fillOpacity={0.4} />
                    <Radar name="Required Target" dataKey="RequiredBenchmark" stroke="#8b5cf6" fill="#8b5cf6" fillOpacity={0.25} />
                    <Legend wrapperStyle={{ fontSize: '12px' }} />
                    <Tooltip contentStyle={{ backgroundColor: '#0c1424', borderColor: '#223456', borderRadius: '8px', fontSize: '12px' }} />
                  </RadarChart>
                )}
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      {/* Categorized Skills Breakdown */}
      {analysis && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Matched Skills */}
          <div className="bg-navy-900 border border-emerald-500/20 rounded-2xl p-5">
            <div className="flex items-center space-x-2 text-emerald-400 mb-3">
              <CheckCircle2 className="w-4 h-4" />
              <h3 className="text-sm font-bold text-white">Matched Skills ({analysis.matchedSkills.length})</h3>
            </div>
            <p className="text-xs text-slate-400 mb-3">Skills where your current mastery meets or exceeds expectations.</p>

            <div className="space-y-2">
              {analysis.matchedSkills.map((s) => (
                <div key={s.skillName} className="p-2.5 bg-navy-950/70 border border-navy-800 rounded-xl flex items-center justify-between text-xs">
                  <div>
                    <div className="font-bold text-white">{s.skillName}</div>
                    <div className="text-[10px] text-slate-400">{s.category}</div>
                  </div>
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    {s.currentProficiency}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Weak / Level-Up Skills */}
          <div className="bg-navy-900 border border-amber-500/20 rounded-2xl p-5">
            <div className="flex items-center space-x-2 text-amber-400 mb-3">
              <TrendingUp className="w-4 h-4" />
              <h3 className="text-sm font-bold text-white">Needs Growth ({analysis.weakSkills.length})</h3>
            </div>
            <p className="text-xs text-slate-400 mb-3">Skills you possess that need leveling up to reach target requirement.</p>

            <div className="space-y-2">
              {analysis.weakSkills.map((s) => (
                <div key={s.skillName} className="p-2.5 bg-navy-950/70 border border-navy-800 rounded-xl flex items-center justify-between text-xs">
                  <div>
                    <div className="font-bold text-white">{s.skillName}</div>
                    <div className="text-[10px] text-amber-400/80">Target: {s.requiredProficiency}</div>
                  </div>
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">
                    Currently: {s.currentProficiency}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Missing Skills */}
          <div className="bg-navy-900 border border-rose-500/20 rounded-2xl p-5">
            <div className="flex items-center space-x-2 text-rose-400 mb-3">
              <AlertCircle className="w-4 h-4" />
              <h3 className="text-sm font-bold text-white">Missing Critical Skills ({analysis.missingSkills.length})</h3>
            </div>
            <p className="text-xs text-slate-400 mb-3">Essential role competencies you currently have not yet acquired.</p>

            <div className="space-y-2">
              {analysis.missingSkills.map((s) => (
                <div key={s.skillName} className="p-2.5 bg-navy-950/70 border border-navy-800 rounded-xl flex items-center justify-between text-xs">
                  <div>
                    <div className="font-bold text-white">{s.skillName}</div>
                    <div className="text-[10px] text-rose-400">Required: {s.requiredProficiency}</div>
                  </div>
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">
                    Missing
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
