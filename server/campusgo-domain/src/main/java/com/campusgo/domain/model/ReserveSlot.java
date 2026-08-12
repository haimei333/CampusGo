package com.campusgo.domain.model;

import com.campusgo.domain.enums.ReserveSlotStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ReserveSlot {
    long id;
    long taskId;
    long runnerId;
    ReserveSlotStatus status;
    Instant holdAt;
    Instant confirmDeadline;
}
