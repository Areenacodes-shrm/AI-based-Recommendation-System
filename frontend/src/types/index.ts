export type OpportunityType = 'COURSE' | 'PROJECT' | 'INTERNSHIP' | 'CERTIFICATION';
export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type PlanStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED';
export type FeedbackAction = 'SAVED' | 'DISMISSED' | 'RATED' | 'COMPLETED';
export type RelevanceBasis = 'LIMITED_DATA_COLD_START' | 'PROFILE_BASED' | 'FEEDBACK_SUPPORTED';

export interface Skill {
  id: number;
  name: string;
  category: string;
  domain: string;
  description?: string;
}

export interface StudentSkill {
  studentId?: number;
  skill: Skill;
  proficiencyLevel: ProficiencyLevel;
  yearsExperience: number;
  verified: boolean;
}

export interface StudentProfile {
  id: number;
  fullName: string;
  email: string;
  demoAccount?: boolean;
  targetRole: string;
  weeklyHoursAvailable: number;
  maxBudget: number;
  preferredDifficulty: DifficultyLevel;
  bio?: string;
  skillsMap?: Record<string, StudentSkill>;
  skillsList?: StudentSkill[];
  createdAt?: string;
  updatedAt?: string;
}

export interface DemoProfile {
  id: number;
  fullName: string;
  targetRole: string;
}

export interface AuthSession {
  authenticated: boolean;
  student: StudentProfile | null;
}

export interface Opportunity {
  id: number;
  title: string;
  description: string;
  opportunityType: OpportunityType;
  difficultyLevel: DifficultyLevel;
  estimatedHours: number;
  cost: number;
  provider: string;
  url?: string;
  rating: number;
  ratingCount: number;
  skillsGained: Skill[];
  prerequisiteSkills: Skill[];
  createdAt?: string;
  // Subtype fields
  platform?: string;
  syllabusSummary?: string;
  certificateIncluded?: boolean;
  durationWeeks?: number;
  repositoryTemplateUrl?: string;
  techStack?: string[];
  techStackCsv?: string;
  projectTier?: string;
  deliverableDescription?: string;
  companyName?: string;
  monthlyStipend?: number;
  locationType?: string;
  durationMonths?: number;
  applicationDeadline?: string;
  issuingOrganization?: string;
  examCode?: string;
  validityYears?: number;
  examFormat?: string;
}

export interface ExplanationData {
  matchedSkills: string[];
  missingSkills: string[];
  careerRelevanceReason?: string;
  timeAndBudgetFitReason?: string;
  prerequisitesReason?: string;
  difficultyAssessment?: string;
  relevanceBasis?: RelevanceBasis;
  confidenceLabel?: string;
  confidenceNote?: string;
}

export interface RecommendationItem {
  opportunity: Opportunity;
  rankingScore: number;
  contentScore: number;
  skillScore: number;
  contextualScore: number;
  feedbackScore: number;
  rankOrder: number;
  explanation: ExplanationData;
}

export interface RecommendationResult {
  recommendationId?: number;
  studentId: number;
  strategyUsed: string;
  relevanceBasis?: RelevanceBasis;
  coldStart?: boolean;
  diversityReranked?: boolean;
  generatedAt: string;
  items: RecommendationItem[];
}

export interface WhatIfSkillInput {
  skillId?: number;
  skillName?: string;
  proficiencyLevel: ProficiencyLevel;
  yearsExperience: number;
}

export interface WhatIfSimulationRequest {
  targetRole?: string;
  weeklyHoursAvailable?: number;
  maxBudget?: number;
  preferredDifficulty?: DifficultyLevel;
  bio?: string;
  simulatedSkills?: WhatIfSkillInput[];
  limit?: number;
  applyDiversity?: boolean;
}

export interface SimulationItemDelta {
  item: RecommendationItem;
  baselineRank?: number | null;
  simulatedRank: number;
  rankDelta?: number | null;
  baselineScore: number;
  scoreDelta: number;
  changeStatus: 'NEW_ENTRY' | 'MOVED_UP' | 'MOVED_DOWN' | 'UNCHANGED';
}

export interface WhatIfSimulationResult {
  studentId: number;
  baselineTargetRole: string;
  simulatedTargetRole: string;
  baselineWeeklyHours: number;
  simulatedWeeklyHours: number;
  baselineMaxBudget: number;
  simulatedMaxBudget: number;
  simulatedDifficulty: DifficultyLevel;
  baselineRoleMatchPercentage: number;
  simulatedRoleMatchPercentage: number;
  roleMatchDelta: number;
  relevanceBasis: RelevanceBasis;
  persisted: boolean;
  items: SimulationItemDelta[];
}

export interface WeeklyProgressSummary {
  studentId: number;
  targetRole: string;
  totalItems: number;
  completedItems: number;
  inProgressItems: number;
  pendingItems: number;
  skippedItems: number;
  completedHours: number;
  remainingHours: number;
  remainingWeeks: number;
  currentRoleMatchPercentage: number;
  projectedRoleMatchPercentage: number;
  estimatedSkillCoverageGain: number;
}

export interface SystemHealthReport {
  service: string;
  status: 'UP' | 'DEGRADED' | 'ERROR';
  timestamp: string;
  uptimeSeconds: number;
  detailMode: string;
  servletRequestCount: number;
  database: {
    connected: boolean;
    productName: string;
    latencyMs: number;
    message: string;
  };
  recommendationEngine: {
    available: boolean;
    status: string;
    activeThreads?: number;
    corePoolSize?: number;
    queuedTasks?: number;
    catalogOpportunities?: number;
    trackedTelemetryItems?: number;
  };
  session: {
    active: boolean;
    authenticated: boolean;
    activeStudentName?: string;
    statusCheckCount: number;
    lastStatusCheck?: string;
    trackingMode: string;
  };
}

export interface SkillGapItem {
  skillName: string;
  category: string;
  currentProficiency: ProficiencyLevel | null;
  requiredProficiency: ProficiencyLevel;
  importanceWeight: number;
  isCore: boolean;
}

export interface SkillGapAnalysis {
  targetRole: string;
  matchPercentage: number;
  totalRequiredSkills: number;
  satisfiedSkillsCount: number;
  matchedSkills: SkillGapItem[];
  weakSkills: SkillGapItem[];
  missingSkills: SkillGapItem[];
}

export interface LearningPlanItem {
  id: number;
  studentId: number;
  opportunityId: number;
  sequenceOrder: number;
  status: PlanStatus;
  targetCompletionWeeks: number;
  prerequisiteNote?: string;
  opportunity?: Opportunity;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserFeedback {
  id: number;
  studentId: number;
  opportunityId: number;
  actionType: FeedbackAction;
  ratingValue?: number;
  notes?: string;
  createdAt?: string;
}

export interface EvaluationResult {
  topK: number;
  totalTestUsers: number;
  hybridPrecisionAtK: number;
  hybridRecallAtK: number;
  hybridNdcgAtK: number;
  hybridCatalogCoverage: number;
  baselinePrecisionAtK: number;
  baselineRecallAtK: number;
  baselineNdcgAtK: number;
  baselineCatalogCoverage: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: number;
}
