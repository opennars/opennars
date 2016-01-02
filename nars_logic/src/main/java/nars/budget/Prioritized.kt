package nars.budget

/**
 * Essentially the methods involved with the priority component of a Budget
 */
interface Prioritized {

    var priority: Float
    val lastForgetTime: Long

    fun setLastForgetTime(currentTime: Long): Long

}
