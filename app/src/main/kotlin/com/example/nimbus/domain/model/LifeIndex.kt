package com.example.nimbus.domain.model

/**
 * One of the service's daily life indices — dressing, UV protection, car washing and so on. [level] is the
 * short verdict the service gives ("舒适", "不需要") and [brief] the even shorter one ("晴好").
 */
data class LifeIndex(
    val type: LifeIndexType,
    val level: String,
    val brief: String,
)

/**
 * The indices Nimbus knows how to name, in the order the screen shows them: the ones a person checks before
 * going out first, the niche ones after. [apiKey] is the service's field name.
 */
enum class LifeIndexType(val apiKey: String) {
    CLOTHING("clothing"),
    COMFORT("comfort"),
    UMBRELLA("umbrella"),
    UV("uv"),
    EXERCISE("exercise"),
    CAR_WASH("car_wash"),
    COLD_RISK("cold_risk"),
    ALLERGY("allergy"),
    DRYING("drying"),
    AIR_CONDITIONER("air_conditioner"),
    TRAVEL("travel"),
    SUNSCREEN("sunscreen"),
    AIR_PURIFIER("air_purifier"),
    POLLEN("pollen"),
    FISHING("fishing"),
    TRAFFIC("traffic"),
    MOOD("mood"),
    BEER("beer"),
}