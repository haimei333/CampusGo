package com.campusgo.domain.model;

/**
 * T07 拼单成员
 */
public class GroupMember {

    public enum Role { CREATOR, MEMBER, EMPTY_SLOT }

    public final String id;
    public final String name;
    public final Role role;
    public final String addressSummary;
    public final double paidAmount;
    public final boolean joined;

    public GroupMember(String id, String name, Role role, String addressSummary,
                       double paidAmount, boolean joined) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.addressSummary = addressSummary;
        this.paidAmount = paidAmount;
        this.joined = joined;
    }

    public static GroupMember emptySlot() {
        return new GroupMember("", "", Role.EMPTY_SLOT, "", 0, false);
    }
}
