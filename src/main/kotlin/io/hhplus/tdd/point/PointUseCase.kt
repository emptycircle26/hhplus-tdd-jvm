import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.UserPoint

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
