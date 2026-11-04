package com.pathpilot.dto;

/**
 * Encapsulates the results of offline recommendation evaluation comparing
 * PathPilot Hybrid AI against a Popularity Baseline recommender.
 */
public class EvaluationResultDto {
    private int topK;
    private int totalTestUsers;
    private double hybridPrecisionAtK;
    private double hybridRecallAtK;
    private double hybridNdcgAtK;
    private double hybridCatalogCoverage;
    private double baselinePrecisionAtK;
    private double baselineRecallAtK;
    private double baselineNdcgAtK;
    private double baselineCatalogCoverage;

    public EvaluationResultDto() {}

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public int getTotalTestUsers() {
        return totalTestUsers;
    }

    public void setTotalTestUsers(int totalTestUsers) {
        this.totalTestUsers = totalTestUsers;
    }

    public double getHybridPrecisionAtK() {
        return hybridPrecisionAtK;
    }

    public void setHybridPrecisionAtK(double hybridPrecisionAtK) {
        this.hybridPrecisionAtK = hybridPrecisionAtK;
    }

    public double getHybridRecallAtK() {
        return hybridRecallAtK;
    }

    public void setHybridRecallAtK(double hybridRecallAtK) {
        this.hybridRecallAtK = hybridRecallAtK;
    }

    public double getHybridNdcgAtK() {
        return hybridNdcgAtK;
    }

    public void setHybridNdcgAtK(double hybridNdcgAtK) {
        this.hybridNdcgAtK = hybridNdcgAtK;
    }

    public double getHybridCatalogCoverage() {
        return hybridCatalogCoverage;
    }

    public void setHybridCatalogCoverage(double hybridCatalogCoverage) {
        this.hybridCatalogCoverage = hybridCatalogCoverage;
    }

    public double getBaselinePrecisionAtK() {
        return baselinePrecisionAtK;
    }

    public void setBaselinePrecisionAtK(double baselinePrecisionAtK) {
        this.baselinePrecisionAtK = baselinePrecisionAtK;
    }

    public double getBaselineRecallAtK() {
        return baselineRecallAtK;
    }

    public void setBaselineRecallAtK(double baselineRecallAtK) {
        this.baselineRecallAtK = baselineRecallAtK;
    }

    public double getBaselineNdcgAtK() {
        return baselineNdcgAtK;
    }

    public void setBaselineNdcgAtK(double baselineNdcgAtK) {
        this.baselineNdcgAtK = baselineNdcgAtK;
    }

    public double getBaselineCatalogCoverage() {
        return baselineCatalogCoverage;
    }

    public void setBaselineCatalogCoverage(double baselineCatalogCoverage) {
        this.baselineCatalogCoverage = baselineCatalogCoverage;
    }
}
