package io.hhplus.tdd.point.domain.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.domain.point.PointUseCaseImpl
import io.hhplus.tdd.domain.point.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class PointUseCaseConcurrencyTest {
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var useCase: PointUseCaseImpl

    @BeforeEach
    fun setup() {
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()
        useCase = PointUseCaseImpl(userPointTable, pointHistoryTable)
    }

    @Test
    fun `동시 충전, 사용에도 일관성 보장`() {
        val userId = 2L
        val threadCount = 20
        val chargePerThread = 100L
        val usePerThread = 50L
        val executor = Executors.newFixedThreadPool(threadCount * 2)
        val chargeLatch = CountDownLatch(threadCount)
        val useLatch = CountDownLatch(threadCount)

        // 20개 충전, 20개 사용을 동시에 실행
        repeat(threadCount) {
            executor.submit {
                try {
                    useCase.charge(userId, chargePerThread)
                } finally {
                    chargeLatch.countDown()
                }
            }
        }
        chargeLatch.await()

        repeat(threadCount) {
            executor.submit {
                try {
                    useCase.use(userId, usePerThread)
                } finally {
                    useLatch.countDown()
                }
            }
        }
        useLatch.await()
        executor.shutdown()

        // 최종 잔고: (100 * 20) - (50 * 20) = 1000
        val finalPoint = useCase.getUserPoint(userId).point
        assertEquals(1000L, finalPoint)
        // 히스토리: 20번 충전, 20번 사용
        val histories = useCase.getUserPointHistory(userId)
        assertEquals(40, histories.size)
        assertEquals(20, histories.count { it.type == TransactionType.CHARGE })
        assertEquals(20, histories.count { it.type == TransactionType.USE })
    }
}
