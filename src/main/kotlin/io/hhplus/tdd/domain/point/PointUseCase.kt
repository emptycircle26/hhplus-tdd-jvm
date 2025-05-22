package io.hhplus.tdd.domain.point

interface PointUseCase {
    fun getUserPoint(id: Long): UserPoint

    fun charge(
        id: Long,
        amount: Long,
    ): UserPoint

    fun use(
        id: Long,
        amount: Long,
    ): UserPoint

    fun getUserPointHistory(id: Long): List<PointHistory>
}
