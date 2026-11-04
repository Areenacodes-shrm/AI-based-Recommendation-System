import React, { useState, useEffect } from 'react';
import { StudentProfile, DifficultyLevel } from '../types';
import { studentApi, catalogApi } from '../api/client';
import { X, Save, Clock, DollarSign, Target, Award, User } from 'lucide-react';

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  student: StudentProfile | null;
  onProfileUpdated: (updated: StudentProfile) => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({
  isOpen,
  onClose,
  student,
  onProfileUpdated,
}) => {
  if (!isOpen || !student) return null;

  const [fullName, setFullName] = useState(student.fullName);
  const [targetRole, setTargetRole] = useState(student.targetRole);
  const [weeklyHours, setWeeklyHours] = useState(student.weeklyHoursAvailable);
  const [maxBudget, setMaxBudget] = useState(student.maxBudget);
  const [difficulty, setDifficulty] = useState<DifficultyLevel>(student.preferredDifficulty);
  const [bio, setBio] = useState(student.bio || '');
  const [roles, setRoles] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    catalogApi.getRoles().then(setRoles).catch(console.error);
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const updated = await studentApi.update(student.id, {
        fullName,
        targetRole,
        weeklyHoursAvailable: weeklyHours,
        maxBudget,
        preferredDifficulty: difficulty,
        bio,
      });
      onProfileUpdated(updated);
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-navy-950/80 backdrop-blur-sm animate-fade-in">
      <div className="bg-navy-900 border border-navy-800 rounded-2xl w-full max-w-xl p-6 shadow-2xl relative">
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-slate-400 hover:text-white"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-2.5 mb-5">
          <div className="w-8 h-8 rounded-lg bg-brand-violet/20 border border-brand-violet/30 flex items-center justify-center text-brand-violetLight">
            <User className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Student Profile & Preferences</h2>
            <p className="text-xs text-slate-400">Modify your constraints and career trajectory settings</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          <div>
            <label className="block text-slate-300 font-semibold mb-1">Full Name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
              className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-slate-300 font-semibold mb-1">Target Career Role</label>
              <select
                aria-label="Target Career Role"
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
              >
                {roles.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-slate-300 font-semibold mb-1">Preferred Difficulty</label>
              <select
                aria-label="Preferred Difficulty Level"
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as DifficultyLevel)}
                className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
              >
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="ADVANCED">Advanced</option>
                <option value="EXPERT">Expert</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-slate-300 font-semibold mb-1">Available Hours / Week</label>
              <input
                type="number"
                min="1"
                max="80"
                value={weeklyHours}
                onChange={(e) => setWeeklyHours(Number(e.target.value))}
                required
                className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
              />
            </div>

            <div>
              <label className="block text-slate-300 font-semibold mb-1">Max Budget ($ USD)</label>
              <input
                type="number"
                min="0"
                step="5"
                value={maxBudget}
                onChange={(e) => setMaxBudget(Number(e.target.value))}
                required
                className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white focus:outline-none focus:ring-2 focus:ring-brand-violet"
              />
            </div>
          </div>

          <div>
            <label className="block text-slate-300 font-semibold mb-1">Bio & Learning Focus</label>
            <textarea
              rows={3}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-violet"
            />
          </div>

          {/* Current Skills list */}
          {student.skillsList && student.skillsList.length > 0 && (
            <div>
              <label className="block text-slate-400 font-semibold mb-1.5 uppercase text-[10px] tracking-wider">
                Current Registered Skills
              </label>
              <div className="flex flex-wrap gap-1.5">
                {student.skillsList.map((ss) => (
                  <span
                    key={ss.skill.id}
                    className="px-2 py-1 rounded bg-navy-950 border border-navy-800 text-slate-300 text-[11px] flex items-center space-x-1"
                  >
                    <span>{ss.skill.name}</span>
                    <span className="text-brand-teal font-semibold">({ss.proficiencyLevel})</span>
                  </span>
                ))}
              </div>
            </div>
          )}

          <div className="flex justify-end space-x-2 pt-4 border-t border-navy-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-navy-800 hover:bg-navy-700 text-slate-300 font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-5 py-2 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white font-bold flex items-center space-x-1.5 shadow-md shadow-brand-violet/25"
            >
              <Save className="w-4 h-4" />
              <span>{saving ? 'Saving...' : 'Save Preferences'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
