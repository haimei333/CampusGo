package com.campusgo.domain.model;

/**
 * 首页推荐任务卡（跑腿员 home-runner.html）
 */
public class RecommendTask {

    public enum CardStyle { EXPRESS, BUY, FILE }

    public final String id;
    public final String title;
    public final String category;
    public final boolean emergency;
    public final String priceLabel;
    public final int distanceMeters;
    public final String expectTime;
    public final int payScore;
    public final int routeScore;
    public final String timeAgo;
    public final String routeSummary;
    public final String sizeLabel;
    public final boolean groupOrder;
    public final CardStyle cardStyle;

    public RecommendTask(String id, String title, String category, boolean emergency,
                         String priceLabel, int distanceMeters, String expectTime,
                         int payScore, int routeScore) {
        this(id, title, category, emergency, priceLabel, distanceMeters, expectTime,
                payScore, routeScore, "", "", "", false, CardStyle.EXPRESS);
    }

    public RecommendTask(String id, String title, String category, boolean emergency,
                         String priceLabel, int distanceMeters, String expectTime,
                         int payScore, int routeScore,
                         String timeAgo, String routeSummary, String sizeLabel,
                         boolean groupOrder, CardStyle cardStyle) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.emergency = emergency;
        this.priceLabel = priceLabel;
        this.distanceMeters = distanceMeters;
        this.expectTime = expectTime;
        this.payScore = payScore;
        this.routeScore = routeScore;
        this.timeAgo = timeAgo;
        this.routeSummary = routeSummary;
        this.sizeLabel = sizeLabel;
        this.groupOrder = groupOrder;
        this.cardStyle = cardStyle;
    }

    public String formatDistance() {
        if (distanceMeters >= 1000) {
            return String.format("%.1fkm", distanceMeters / 1000f);
        }
        return distanceMeters + "m";
    }
}
