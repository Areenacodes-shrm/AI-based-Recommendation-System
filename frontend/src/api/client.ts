import {
  ApiResponse,
  EvaluationResult,
  LearningPlanItem,
  Opportunity,
  PlanStatus,
  RecommendationResult,
  RecommendationItem,
  Skill,
  SkillGapAnalysis,
  StudentProfile,
  UserFeedback
} from '../types';

const API_BASE = '/api';

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE}${endpoint}`;
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  const body: ApiResponse<T> = await response.json();
  if (!response.ok || !body.success) {
    throw new Error(body.message || `Request failed with status ${response.status}`);
  }

  return body.data;
}

export const studentApi = {
  getById: (id: number) => request<StudentProfile>(`/students/${id}`),
  getByEmail: (email: string) => request<StudentProfile>(`/students/email/${encodeURIComponent(email)}`),
  getAll: () => request<StudentProfile[]>('/students'),
  register: (data: Partial<StudentProfile> & { skills?: any[] }) =>
    request<StudentProfile>('/students/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: Partial<StudentProfile>) =>
    request<StudentProfile>(`/students/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
};

export const recommendationApi = {
  getRecommendations: (studentId: number, limit = 8) =>
    request<RecommendationResult>(`/recommendations/student/${studentId}?limit=${limit}`),
  getLatest: (studentId: number) =>
    request<RecommendationItem[]>(`/recommendations/student/${studentId}/latest`),
};

export const skillGapApi = {
  getSkillGap: (studentId: number, targetRole?: string) => {
    const query = targetRole ? `?targetRole=${encodeURIComponent(targetRole)}` : '';
    return request<SkillGapAnalysis>(`/skill-gap/student/${studentId}${query}`);
  },
};

export const roadmapApi = {
  getRoadmap: (studentId: number) => request<LearningPlanItem[]>(`/roadmap/student/${studentId}`),
  generateRoadmap: (studentId: number) =>
    request<LearningPlanItem[]>(`/roadmap/student/${studentId}/generate`, { method: 'POST' }),
  updateItem: (itemId: number, status?: PlanStatus, targetCompletionWeeks?: number) =>
    request<LearningPlanItem>(`/roadmap/item/${itemId}`, {
      method: 'PATCH',
      body: JSON.stringify({ status, targetCompletionWeeks }),
    }),
  deleteItem: (itemId: number) =>
    request<void>(`/roadmap/item/${itemId}`, { method: 'DELETE' }),
};

export const feedbackApi = {
  submit: (studentId: number, data: { opportunityId: number; actionType: string; ratingValue?: number; notes?: string }) =>
    request<UserFeedback>(`/feedback/student/${studentId}`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getStudentFeedback: (studentId: number) =>
    request<UserFeedback[]>(`/feedback/student/${studentId}`),
};

export const catalogApi = {
  getSkills: () => request<Skill[]>('/catalog/skills'),
  getRoles: () => request<string[]>('/catalog/roles'),
  searchOpportunities: (params: Record<string, any> = {}) => {
    const q = new URLSearchParams(params).toString();
    return request<Opportunity[]>(`/opportunities?${q}`);
  },
};

export const evaluationApi = {
  run: (topK = 5) => request<EvaluationResult>(`/evaluation/run?topK=${topK}`),
};
