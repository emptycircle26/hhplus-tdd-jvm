import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PointUseCaseImpl(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) : PointUseCase {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun getUserPoint(id: Long): UserPoint {
        TODO("Not yet implemented")
    }

    override fun charge(
        id: Long,
        amount: Long,
    ): UserPoint {
        TODO("Not yet implemented")
    }

    override fun use(
        id: Long,
        amount: Long,
    ): UserPoint {
        TODO("Not yet implemented")
    }

    override fun getUserPointHistory(id: Long): List<PointHistory> {
        TODO("Not yet implemented")
    }
}
