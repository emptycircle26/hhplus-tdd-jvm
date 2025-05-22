import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class PointUseCaseImpl(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) : PointUseCase {
    companion object {
        private const val MAX_POINT = 1_000_000L
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val locks = ConcurrentHashMap<Long, Any>()

    override fun getUserPoint(id: Long): UserPoint = userPointTable.selectById(id)

    override fun charge(
        id: Long,
        amount: Long,
    ): UserPoint {
        if (amount <= 0) throw IllegalArgumentException("충전 금액은 0보다 커야합니다")
        val lock = locks.computeIfAbsent(id) { Any() }
        synchronized(lock) {
            val current = userPointTable.selectById(id)
            val newPoint = current.point + amount
            if (newPoint > MAX_POINT) throw IllegalArgumentException("최대 잔고는 $MAX_POINT 입니다")
            val updated = userPointTable.insertOrUpdate(id, newPoint)
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updated.updateMillis)
            return updated
        }
    }

    override fun use(
        id: Long,
        amount: Long,
    ): UserPoint {
        if (amount <= 0) throw IllegalArgumentException("사용 금액은 0보다 커야합니다")
        if (amount > MAX_POINT) throw IllegalArgumentException("사용 금액은 최대 $MAX_POINT 원을 초과할 수 없습니다")
        val lock = locks.computeIfAbsent(id) { Any() }
        synchronized(lock) {
            val current = userPointTable.selectById(id)
            if (current.point < amount) throw IllegalArgumentException("잔고가 부족합니다")
            val updated = userPointTable.insertOrUpdate(id, current.point - amount)
            pointHistoryTable.insert(id, amount, TransactionType.USE, updated.updateMillis)
            return updated
        }
    }

    override fun getUserPointHistory(id: Long): List<PointHistory> = pointHistoryTable.selectAllByUserId(id)
}
