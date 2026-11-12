import React, { useState, useEffect } from 'react';
import { StudentProfile, DifficultyLevel, ProficiencyLevel, Skill } from '../types';
import { studentApi, catalogApi } from '../api/client';
import { X, Save, Check, User } from 'lucide-react';

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
  const [fullName, setFullName] = useState('');
  const [targetRole, setTargetRole] = useState('');
  const [weeklyHours, setWeeklyHours] = useState(15);
  const [maxBudget, setMaxBudget] = useState(100);
  const [difficulty, setDifficulty] = useState<DifficultyLevel>('INTERMEDIATE');
  const [bio, setBio] = useState('');
  const [roles, setRoles] = useState<string[]>([]);
  const [availableSkills, setAvailableSkills] = useState<Skill[]>([]);
  const [selectedSkills, setSelectedSkills] = useState<
    { skillId: number; skillName: string; proficiencyLevel: ProficiencyLevel; yearsExperience: number }[]
  >([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    catalogApi.getRoles().then(setRoles).catch(console.error);
    catalogApi.getSkills().then(setAvailableSkills).catch(console.error);
  }, []);

  useEffect(() => {
    if (student && isOpen) {
      setFullName(student.fullName);
      setTargetRole(student.targetRole);
      setWeeklyHours(student.weeklyHoursAvailable);
      setMaxBudget(student.maxBudget);
      setDifficulty(student.preferredDifficulty);
      setBio(student.bio || '');
      setSelectedSkills(
        (student.skillsList || []).map((ss) => ({
          skillId: ss.skill.id,
          skillName: ss.skill.name,
          proficiencyLevel: ss.proficiencyLevel,
          yearsExperience: ss.yearsExperience ?? 1.0,
        }))
      );
      setError(null);
    }
  }, [student, isOpen]);

  if (!isOpen || !student) return null;

  const toggleSkill = (skill: Skill) => {
    const exists = selectedSkills.some((s) => s.skillId === skill.id);
    if (exists) {
      setSelectedSkills(selectedSkills.filter((s) => s.skillId !== skill.id));
    } else {
      setSelectedSkills([
        ...selectedSkills,
        {
          skillId: skill.id,
          skillName: skill.name,
          proficiencyLevel: 'INTERMEDIATE',
          yearsExperience: 1.0,
        },
      ]);
    }
  };

  const updateSkillProficiency = (skillId: number, level: ProficiencyLevel) => {
    setSelectedSkills(
      selectedSkills.map((s) => (s.skillId === skillId ? { ...s, proficiencyLevel: level } : s))
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const updated = await studentApi.update(student.id, {
        fullName,
        targetRole,
        weeklyHoursAvailable: weeklyHours,
        maxBudget,
        preferredDifficulty: difficulty,
        bio,
        skills: selectedSkills,
      });
      onProfileUpdated(updated);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Unable to save profile changes');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-navy-950/80 backdrop-blur-sm animate-fade-in">
      <div className="bg-navy-900 border border-navy-800 rounded-2xl w-full max-w-2xl p-6 shadow-2xl relative max-h-[90vh] overflow-y-auto">
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
            <p className="text-xs text-slate-400">Modify your constraints, career goal, and registered skill competencies</p>
          </div>
        </div>

        {error && (
          <div className="p-3 mb-4 bg-rose-500/10 border border-rose-500/20 text-rose-300 text-xs rounded-xl">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          <div>
            <label className="block text-slate-300 font-semibold mb-1">Full Name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
              minLength={2}
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
              rows={2}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              className="w-full bg-navy-950 border border-navy-700 rounded-xl px-3.5 py-2 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-brand-violet"
            />
          </div>

          <div>
            <label className="block text-slate-300 font-semibold mb-1.5">
              Registered Skills ({selectedSkills.length} selected)
            </label>
            <div className="flex flex-wrap gap-1.5 max-h-32 overflow-y-auto p-2 bg-navy-950 border border-navy-800 rounded-xl">
              {availableSkills.map((skill) => {
                const isSelected = selectedSkills.some((s) => s.skillId === skill.id);
                return (
                  <button
                    key={skill.id}
                    type="button"
                    onClick={() => toggleSkill(skill)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-semibold flex items-center space-x-1 transition-all ${
                      isSelected
                        ? 'bg-brand-violet text-white shadow-sm'
                        : 'bg-navy-900 text-slate-400 hover:text-white border border-navy-700'
                    }`}
                  >
                    {isSelected && <Check className="w-3 h-3" />}
                    <span>{skill.name}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {selectedSkills.length > 0 && (
            <div className="space-y-2 max-h-36 overflow-y-auto">
              {selectedSkills.map((s) => (
                <div
                  key={s.skillId}
                  className="flex items-center justify-between p-2 bg-navy-950 border border-navy-800 rounded-xl"
                >
                  <span className="font-semibold text-white">{s.skillName}</span>
                  <select
                    aria-label={`Proficiency level for ${s.skillName}`}
                    value={s.proficiencyLevel}
                    onChange={(e) => updateSkillProficiency(s.skillId, e.target.value as ProficiencyLevel)}
                    className="bg-navy-900 border border-navy-700 text-[11px] text-brand-teal font-semibold rounded-lg px-2 py-1"
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
              className="px-5 py-2 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white font-bold flex items-center space-x-1.5 shadow-md shadow-brand-violet/25 disabled:opacity-50"
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
