package com.assetmanagement.backend.security;

import java.util.Collections;
import java.util.Set;

public record AuthorizedScope(boolean isAll, Set<Long> employeeIds) {

    public static AuthorizedScope all() {
        return new AuthorizedScope(true, Collections.emptySet());
    }

    public static AuthorizedScope of(Set<Long> employeeIds) {
        return new AuthorizedScope(false, employeeIds != null ? employeeIds : Collections.emptySet());
    }

    public static AuthorizedScope single(Long employeeId) {
        return new AuthorizedScope(false, employeeId != null ? Set.of(employeeId) : Collections.emptySet());
    }

    public static AuthorizedScope empty() {
        return new AuthorizedScope(false, Collections.emptySet());
    }

    public boolean canAccess(Long targetUserId) {
        if (isAll) {
            return true;
        }
        return targetUserId != null && employeeIds != null && employeeIds.contains(targetUserId);
    }

    public boolean isEmpty() {
        return !isAll && (employeeIds == null || employeeIds.isEmpty());
    }
}

