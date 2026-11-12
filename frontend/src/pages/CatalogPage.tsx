import React, { useState, useEffect } from 'react';
import { Opportunity } from '../types';
import { catalogApi, feedbackApi, roadmapApi } from '../api/client';
import { Search, ExternalLink, Bookmark, PlusCircle, Sparkles } from 'lucide-react';

interface CatalogPageProps {
  studentId: number | null;
}

export const CatalogPage: React.FC<CatalogPageProps> = ({ studentId }) => {
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [query, setQuery] = useState('');
  const [type, setType] = useState<string>('ALL');
  const [difficulty, setDifficulty] = useState<string>('ALL');
  const [loading, setLoading] = useState(false);
  const [savedIds, setSavedIds] = useState<Set<number>>(new Set());
  const [toast, setToast] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const searchCatalog = async () => {
    setLoading(true);
    try {
      const params: Record<string, any> = { limit: 30 };
      if (query.trim()) params.query = query.trim();
      if (type !== 'ALL') params.type = type;
      if (difficulty !== 'ALL') params.difficulty = difficulty;

      const results = await catalogApi.searchOpportunities(params);
      setOpportunities(results);

      if (studentId) {
        const feedbacks = await feedbackApi.getStudentFeedback(studentId);
        const s = new Set<number>();
        for (const fb of feedbacks) {
          if (fb.actionType === 'SAVED') s.add(fb.opportunityId);
        }
        setSavedIds(s);
      } else {
        setSavedIds(new Set());
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    searchCatalog();
  }, [type, difficulty, studentId]);

  const handleSave = async (oppId: number) => {
    if (!studentId) return;
    try {
      await feedbackApi.submit(studentId, {
        opportunityId: oppId,
        actionType: 'SAVED',
      });
      setSavedIds(new Set([...savedIds, oppId]));
      showToast('Saved opportunity to your bookmarks!');
    } catch (err: any) {
      showToast(err.message || 'Unable to save opportunity');
    }
  };

  const handleAddToRoadmap = async (oppId: number) => {
    if (!studentId) return;
    try {
      await roadmapApi.addItem(studentId, oppId);
      showToast('Added opportunity to your learning roadmap!');
    } catch (err: any) {
      showToast(err.message || 'Unable to add opportunity to roadmap');
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

      {/* Search Header */}
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <h1 className="text-2xl font-extrabold text-white tracking-tight">
          Opportunity Catalog Explorer
        </h1>
        <p className="text-xs text-slate-400 mt-1 mb-5">
          Browse verified university courses, open-source projects, engineering internships, and professional credentials.
        </p>

        {/* Search Bar & Filters */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <div className="md:col-span-2 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
            <input
              type="text"
              placeholder="Search by title, keyword, tech stack, or provider..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && searchCatalog()}
              className="w-full bg-navy-950 border border-navy-700 rounded-xl pl-10 pr-4 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-violet"
            />
          </div>

          <div>
            <select
              aria-label="Filter by Type"
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3 py-2.5 text-xs text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
            >
              <option value="ALL">All Types</option>
              <option value="COURSE">Courses</option>
              <option value="PROJECT">Projects</option>
              <option value="INTERNSHIP">Internships</option>
              <option value="CERTIFICATION">Certifications</option>
            </select>
          </div>

          <div>
            <select
              aria-label="Filter by Difficulty"
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3 py-2.5 text-xs text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
            >
              <option value="ALL">All Difficulties</option>
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
              <option value="EXPERT">Expert</option>
            </select>
          </div>
        </div>
      </div>

      {/* Catalog Grid */}
      {loading ? (
        <div className="text-center py-20 bg-navy-900 border border-navy-800 rounded-2xl">
          <div className="animate-spin w-8 h-8 border-4 border-brand-violet border-t-transparent rounded-full mx-auto mb-3" />
          <p className="text-xs text-slate-400">Querying JDBC catalog repository...</p>
        </div>
      ) : opportunities.length === 0 ? (
        <div className="text-center py-16 bg-navy-900 border border-navy-800 rounded-2xl">
          <Search className="w-8 h-8 text-slate-600 mx-auto mb-2" />
          <p className="text-xs text-slate-400">No opportunities match query.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {opportunities.map((opp) => (
            <div
              key={opp.id}
              className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-2.5">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-navy-800 text-brand-violetLight border border-navy-700">
                    {opp.opportunityType}
                  </span>
                  <div className="flex items-center space-x-1.5">
                    <span className="text-xs font-bold text-brand-teal">
                      {opp.cost === 0 ? 'Free' : `$${opp.cost.toFixed(2)}`}
                    </span>
                    {studentId && (
                      <button
                        onClick={() => handleSave(opp.id)}
                        className={`p-1 rounded ${
                          savedIds.has(opp.id) ? 'text-brand-violet' : 'text-slate-500 hover:text-white'
                        }`}
                      >
                        <Bookmark className="w-3.5 h-3.5 fill-current" />
                      </button>
                    )}
                  </div>
                </div>

                <h3 className="text-sm font-bold text-white mb-1 hover:text-brand-violetLight transition-colors">
                  {opp.title}
                </h3>
                <p className="text-[11px] text-slate-400 mb-2">{opp.provider} • {opp.difficultyLevel} tier</p>
                <p className="text-xs text-slate-300 line-clamp-3 mb-3 leading-relaxed">
                  {opp.description}
                </p>

                {/* Skills gained */}
                {opp.skillsGained && opp.skillsGained.length > 0 && (
                  <div className="flex flex-wrap gap-1 mb-4">
                    {opp.skillsGained.map((s) => (
                      <span key={s.id} className="text-[10px] px-1.5 py-0.5 rounded bg-navy-950 text-slate-400 border border-navy-800">
                        {s.name}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <div className="pt-3 border-t border-navy-800 flex items-center justify-between gap-2">
                <span className="text-xs text-slate-400 font-medium">~{opp.estimatedHours} hrs total</span>
                <div className="flex items-center space-x-2">
                  {studentId && (
                    <button
                      onClick={() => handleAddToRoadmap(opp.id)}
                      className="px-2.5 py-1 rounded-lg bg-navy-800 hover:bg-navy-700 text-slate-200 border border-navy-700 text-[11px] font-semibold flex items-center space-x-1 transition-colors"
                    >
                      <PlusCircle className="w-3.5 h-3.5 text-brand-teal" />
                      <span>Roadmap</span>
                    </button>
                  )}
                  {opp.url && (
                    <a
                      href={opp.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-1.5 bg-brand-violet hover:bg-brand-violetLight text-white rounded text-xs flex items-center"
                      title="Open Opportunity Resource"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
