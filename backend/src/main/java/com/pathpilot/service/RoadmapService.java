package com.pathpilot.service;

import com.pathpilot.model.LearningPlanItem;
import com.pathpilot.model.PlanStatus;

import java.util.List;

public interface RoadmapService {
    List<LearningPlanItem> generateRoadmap(Long studentId);
    List<LearningPlanItem> getRoadmap(Long studentId);
    LearningPlanItem updateItemStatus(Long planItemId, PlanStatus status, Integer targetWeeks);
    boolean deleteRoadmapItem(Long planItemId);
}
