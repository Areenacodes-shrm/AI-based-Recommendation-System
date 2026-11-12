package com.pathpilot.service;

import com.pathpilot.dto.WeeklyProgressSummaryDto;
import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.model.PlanStatus;

import java.util.List;

public interface RoadmapService {
    List<LearningPlanItem> generateRoadmap(Long studentId);
    List<LearningPlanItem> getRoadmap(Long studentId);
    LearningPlanItem getItemById(Long planItemId);
    LearningPlanItem addOpportunityToRoadmap(Long studentId, Long opportunityId, String note);
    LearningPlanItem updateItemStatus(Long planItemId, PlanStatus status, Integer targetWeeks);
    boolean deleteRoadmapItem(Long planItemId);
    String exportRoadmapCsv(Long studentId);
    WeeklyProgressSummaryDto getWeeklyProgressSummary(Long studentId);
}
