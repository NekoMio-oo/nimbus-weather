package com.example.nimbus.domain.model

/**
 * A location the user follows. [id] is the geocoder's stable id, so saving the same city twice keeps one
 * entry, and [timeZone] is an IANA zone so times in a forecast can be shown as the place experiences them.
 */
data class Place(
    val id: Long,
    val name: String,
    val region: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
) {
    /** "Region, Country" with whichever parts exist, for the line under a place name. */
    val subtitle: String
        get() = listOfNotNull(
            region?.takeIf { it.isNotBlank() && it != name },
            country.takeIf { it.isNotBlank() },
        ).joinToString(", ")
}
