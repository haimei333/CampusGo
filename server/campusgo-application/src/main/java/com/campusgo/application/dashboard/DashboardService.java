package com.campusgo.application.dashboard;

import com.campusgo.domain.enums.UserRole;
import com.campusgo.domain.model.DashboardStats;

public interface DashboardService {

    DashboardStats getStats(long userId, UserRole role);
}