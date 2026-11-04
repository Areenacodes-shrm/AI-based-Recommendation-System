export type OpportunityType = 'COURSE' | 'PROJECT' | 'INTERNSHIP' | 'CERTIFICATION';
export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type PlanStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED';
export type FeedbackAction = 'SAVED' | 'DISMISSED' | 'RATED' | 'COMPLETED';

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
  generatedAt: string;
  items: RecommendationItem[];
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
