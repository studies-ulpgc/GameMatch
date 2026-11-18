package com.pamn.gamematch.data.remote.mapper

fun computeFlags(
    rating: Double?,
    ratingCount: Int?,
    firstReleaseDateSeconds: Long?,
    nowMs: Long = System.currentTimeMillis()
): Pair<Boolean, Boolean> {
    val isFeatured = rating != null && rating >= 80.0 && ratingCount != null && ratingCount >= 15

    val isTrending = if (firstReleaseDateSeconds != null && firstReleaseDateSeconds > 0 && ratingCount != null && ratingCount >= 3) {
        val releaseMs = firstReleaseDateSeconds * 1000L
        val oneYearAgoMs = nowMs - (365L * 24 * 3600 * 1000)
        releaseMs in oneYearAgoMs..nowMs
    } else {
        false
    }

    return Pair(isFeatured, isTrending)
}
