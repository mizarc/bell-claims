package dev.mizarc.bellclaims.config

data class MainConfig(
    var claimLimit: Int = 0,
    var claimBlockLimit: Int = 0,
    var initialClaimSize: Int = 0,
    var minimumPartitionSize: Int = 0,
    var distanceBetweenClaims: Int = 0,
    var visualiserHideDelayPeriod: Double = 0.0,
    var visualiserRefreshPeriod: Double = 0.0,
    var autoRefreshVisualisation: Boolean = true,
    var rightClickHarvest: Boolean = true,
    var showClaimEnterPopup: Boolean = true,
    var pluginLanguage: String = "EN",
    var customClaimToolModelId: Int = 732000,
    var customMoveToolModelId: Int = 732001,
    var blacklistedFlags: Set<String> = emptySet(),
    var blacklistedPermissions: Set<String> = emptySet()
)