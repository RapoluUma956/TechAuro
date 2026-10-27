package com.assetmanagement.backend.dto.dashboard;

import java.util.List;

public record AttentionAssetsResponse(
        List<AttentionAssetItem> items,
        PaginationDto pagination
) {}
