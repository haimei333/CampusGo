package com.campusgo.data.remote.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.campusgo.data.remote.dto.task.GroupMemberDto;
import com.campusgo.data.remote.dto.task.GroupOrderDetailDto;
import com.campusgo.data.remote.dto.wallet.WalletTransactionDto;
import com.campusgo.domain.model.GroupMember;
import com.campusgo.domain.model.GroupOrderDetail;
import com.campusgo.domain.model.WalletTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GroupDtoMapper {

    private GroupDtoMapper() {
    }

    @Nullable
    public static GroupOrderDetail toDetail(@Nullable GroupOrderDetailDto dto) {
        if (dto == null || dto.taskId == null) {
            return null;
        }
        List<GroupMember> members = new ArrayList<>();
        if (dto.members != null) {
            for (GroupMemberDto member : dto.members) {
                GroupMember mapped = toMember(member);
                if (mapped != null) {
                    members.add(mapped);
                }
            }
        }
        return new GroupOrderDetail(
                dto.taskId,
                nullToEmpty(dto.title),
                nullToEmpty(dto.categoryLabel),
                nullToEmpty(dto.pickupAddress),
                nullToEmpty(dto.deliverySummary),
                dto.totalReward,
                dto.sharePerPerson,
                dto.maxMembers,
                dto.joinedCount,
                nullToEmpty(dto.timeLabel),
                dto.viewerJoined,
                dto.viewerIsCreator,
                dto.full,
                members);
    }

    @Nullable
    private static GroupMember toMember(@Nullable GroupMemberDto dto) {
        if (dto == null || dto.role == null) {
            return null;
        }
        GroupMember.Role role = switch (dto.role) {
            case CREATOR -> GroupMember.Role.CREATOR;
            case MEMBER -> GroupMember.Role.MEMBER;
            case EMPTY_SLOT -> GroupMember.Role.EMPTY_SLOT;
        };
        return new GroupMember(
                nullToEmpty(dto.id),
                nullToEmpty(dto.name),
                role,
                nullToEmpty(dto.addressSummary),
                dto.paidAmount,
                dto.joined);
    }

    @NonNull
    public static List<WalletTransaction> toTransactions(@Nullable List<WalletTransactionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        List<WalletTransaction> result = new ArrayList<>(dtos.size());
        for (WalletTransactionDto dto : dtos) {
            if (dto == null || dto.id == null) {
                continue;
            }
            WalletTransaction.Type type = dto.type == WalletTransactionDto.Type.INCOME
                    ? WalletTransaction.Type.INCOME
                    : WalletTransaction.Type.EXPENSE;
            result.add(new WalletTransaction(
                    dto.id,
                    nullToEmpty(dto.title),
                    nullToEmpty(dto.timeLabel),
                    dto.amount,
                    type));
        }
        return result;
    }

    @NonNull
    private static String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
