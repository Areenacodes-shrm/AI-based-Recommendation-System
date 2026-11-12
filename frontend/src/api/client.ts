import {
  ApiResponse,
  AuthSession,
  DemoProfile,
  EvaluationResult,
  LearningPlanItem,
  Opportunity,
  PlanStatus,
  RecommendationResult,
  RecommendationItem,
  Skill,
  SkillGapAnalysis,
  StudentProfile,
  SystemHealthReport,
  UserFeedback,
  WeeklyProgressSummary,
  WhatIfSimulationRequest,
  WhatIfSimulationResult
} from '../types';

const API_BASE = '/api';

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = `${API_BASE}${endpoint}`;
  const response = await fetch(url, {
    credentials: 'include',
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

export const authApi = {
  me: () => request<AuthSession>('/auth/me'),
  demoProfiles: () => request<DemoProfile[]>('/auth/demo-profiles'),
  login: (email: string, password: string) =>
    request<AuthSession>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  demoLogin: (studentId: number) =>
    request<AuthSession>('/auth/demo-login', {
      method: 'POST',
      body: JSON.stringify({ studentId }),
    }),
  logout: () => request<void>('/auth/logout', { method: 'POST' }),
};

export const studentApi = {
  getById: (id: number) => request<StudentProfile>(`/students/${id}`),
  getByEmail: (email: string) => request<StudentProfile>(`/students/email/${encodeURIComponent(email)}`),
  getAll: () => request<StudentProfile[]>('/students'),
  register: (data: Partial<StudentProfile> & { password: string; skills?: any[] }) =>
    request<StudentProfile>('/students/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: Partial<StudentProfile> & { skills?: any[] }) =>
    request<StudentProfile>(`/students/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
};

export const recommendationApi = {
  getRecommendations: (studentId: number, limit = 8, diverse = true) =>
    request<RecommendationResult>(`/recommendations/student/${studentId}?limit=${limit}&diverse=${diverse}`),
  getLatest: (studentId: number) =>
    request<RecommendationItem[]>(`/recommendations/student/${studentId}/latest`),
  compare: (studentId: number, opportunityIds: number[]) =>
    request<RecommendationItem[]>(`/recommendations/student/${studentId}/compare`, {
      method: 'POST',
      body: JSON.stringify({ opportunityIds }),
    }),
  simulate: (studentId: number, payload: WhatIfSimulationRequest) =>
    request<WhatIfSimulationResult>(`/recommendations/student/${studentId}/simulate`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
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
  addItem: (studentId: number, opportunityId: number, note?: string) =>
    request<LearningPlanItem>(`/roadmap/student/${studentId}/add`, {
      method: 'POST',
      body: JSON.stringify({ opportunityId, note }),
    }),
  getSummary: (studentId: number) =>
    request<WeeklyProgressSummary>(`/roadmap/student/${studentId}/summary`),
  updateItem: (itemId: number, status?: PlanStatus, targetCompletionWeeks?: number) =>
    request<LearningPlanItem>(`/roadmap/item/${itemId}`, {
      method: 'PATCH',
      body: JSON.stringify({ status, targetCompletionWeeks }),
    }),
  deleteItem: (itemId: number) =>
    request<void>(`/roadmap/item/${itemId}`, { method: 'DELETE' }),
  exportCsv: async (studentId: number): Promise<void> => {
    const response = await fetch(`${API_BASE}/roadmap/student/${studentId}/export?format=csv`, {
      method: 'GET',
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error(`Export failed with status ${response.status}`);
    }
    const blob = await response.blob();
    const disposition = response.headers.get('Content-Disposition') || '';
    const match = /filename="([^"]+)"/.exec(disposition);
    const filename = match ? match[1] : `pathpilot-roadmap-student-${studentId}.csv`;

    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(downloadUrl);
  },
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

export const systemApi = {
  getHealth: async (
    detail: 'summary' | 'engine' | 'session' = 'engine',
    trackSession = true
  ): Promise<SystemHealthReport> => {
    const response = await fetch(
      `/servlet/system-health?detail=${encodeURIComponent(detail)}&trackSession=${trackSession}`,
      {
        method: 'GET',
        credentials: 'include',
        headers: { Accept: 'application/json' },
      }
    );
    const data = await response.json();
    if (!response.ok && response.status !== 503) {
      throw new Error(data.error || `Servlet check failed (${response.status})`);
    }
    return data as SystemHealthReport;
  },
};
