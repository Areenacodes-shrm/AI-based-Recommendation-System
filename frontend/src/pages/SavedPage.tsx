import React, { useState, useEffect } from 'react';
import { Opportunity } from '../types';
import { feedbackApi, catalogApi } from '../api/client';
import { Bookmark, ExternalLink, Trash2 } from 'lucide-react';

interface SavedPageProps {
  studentId: number | null;
}

export const SavedPage: React.FC<SavedPageProps> = ({ studentId }) => {
  const [savedOpportunities, setSavedOpportunities] = useState<Opportunity[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchSaved = async () => {
    if (!studentId) return;
    setLoading(true);
    try {
      const feedbacks = await feedbackApi.getStudentFeedback(studentId);
      const savedOppIds = feedbacks
        .filter((fb) => fb.actionType === 'SAVED')
        .map((fb) => fb.opportunityId);

      const all = await catalogApi.searchOpportunities({ limit: 50 });
      const filtered = all.filter((o) => savedOppIds.includes(o.id));
      setSavedOpportunities(filtered);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSaved();
  }, [studentId]);

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center space-x-2">
          <Bookmark className="w-5 h-5 text-brand-violetLight fill-current" />
          <span>Saved Opportunities</span>
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          Your bookmarked courses, capstone projects, internships, and certifications.
        </p>
      </div>

      {loading ? (
        <div className="text-center py-20 bg-navy-900 border border-navy-800 rounded-2xl">
          <p className="text-xs text-slate-400">Loading saved items...</p>
        </div>
      ) : savedOpportunities.length === 0 ? (
        <div className="text-center py-16 bg-navy-900 border border-navy-800 rounded-2xl">
          <Bookmark className="w-10 h-10 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-white">No Saved Opportunities</h3>
          <p className="text-xs text-slate-400 mt-1">
            Bookmark opportunities from the Recommendations or Catalog pages to curate your personal backlog.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {savedOpportunities.map((opp) => (
            <div key={opp.id} className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-navy-800 text-brand-teal border border-navy-700">
                    {opp.opportunityType}
                  </span>
                  <span className="text-xs font-bold text-white">
                    {opp.cost === 0 ? 'Free' : `$${opp.cost.toFixed(2)}`}
                  </span>
                </div>

                <h3 className="text-sm font-bold text-white mb-1">{opp.title}</h3>
                <p className="text-[11px] text-slate-400 mb-2">{opp.provider} • {opp.difficultyLevel}</p>
                <p className="text-xs text-slate-300 line-clamp-3 mb-4 leading-relaxed">{opp.description}</p>
              </div>

              <div className="pt-3 border-t border-navy-800 flex items-center justify-between">
                <span className="text-xs text-slate-400">~{opp.estimatedHours} hrs</span>
                {opp.url && (
                  <a
                    href={opp.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="p-1.5 bg-brand-violet hover:bg-brand-violetLight text-white rounded text-xs flex items-center"
                  >
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
