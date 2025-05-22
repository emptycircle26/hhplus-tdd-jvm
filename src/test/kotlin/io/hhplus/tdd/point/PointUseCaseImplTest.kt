package io.hhplus.tdd.point

import PointUseCase
import PointUseCaseImpl
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointUseCaseImplTest {
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointUseCase: PointUseCase

    @BeforeEach
    fun setup() {
        userPointTable = mockk(relaxed = true)
        pointHistoryTable = mockk(relaxed = true)
        pointUseCase = PointUseCaseImpl(userPointTable, pointHistoryTable)
    }

    @Test
    fun `포인트 조회`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 1000L, 1234L)
        val userPoint = pointUseCase.getUserPoint(1L)
        assertEquals(1L, userPoint.id)
        assertEquals(1000L, userPoint.point)
        assertEquals(1234L, userPoint.updateMillis)
        verify { userPointTable.selectById(1L) }
    }

    @Test
    fun `포인트 충전`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 0L, 1000L)
        every { userPointTable.insertOrUpdate(1L, 1000L) } returns UserPoint(1L, 1000L, 2000L)
        val userPoint = pointUseCase.charge(1L, 1000L)
        assertEquals(1L, userPoint.id)
        assertEquals(1000L, userPoint.point)
        assertTrue(userPoint.updateMillis != 0L)
        verify { userPointTable.insertOrUpdate(1L, 1000L) }
    }

    @Test
    fun `포인트 사용`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 1000L, 1000L)
        every { userPointTable.insertOrUpdate(1L, 500L) } returns UserPoint(1L, 500L, 2000L)
        val userPoint = pointUseCase.use(1L, 500L)
        assertEquals(1L, userPoint.id)
        assertEquals(500L, userPoint.point)
        verify { userPointTable.insertOrUpdate(1L, 500L) }
    }

    @Test
    fun `포인트 충전 및 사용 내역 조회`() {
        every { pointHistoryTable.selectAllByUserId(1L) } returns
            listOf(
                PointHistory(1L, 1L, TransactionType.CHARGE, 1000L, 1234L),
                PointHistory(2L, 1L, TransactionType.USE, 200L, 1235L),
            )
        val histories = pointUseCase.getUserPointHistory(1L)
        assertEquals(2, histories.size)
        assertEquals(TransactionType.CHARGE, histories[0].type)
        assertEquals(TransactionType.USE, histories[1].type)
        verify { pointHistoryTable.selectAllByUserId(1L) }
    }

    @Test
    fun `음수 금액 충전 시 예외`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 0L, 1000L)
        try {
            pointUseCase.charge(1L, -100L)
            assert(false) { "예외가 발생해야 합니다." }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("0보다 커야") == true)
        }
    }

    @Test
    fun `음수 금액 사용 시 예외`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 1000L, 1000L)
        try {
            pointUseCase.use(1L, -100L)
            assert(false) { "예외가 발생해야 합니다." }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("0보다 커야") == true)
        }
    }

    @Test
    fun `잔고 부족 시 사용 예외`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 100L, 1000L)
        try {
            pointUseCase.use(1L, 200L)
            assert(false) { "예외가 발생해야 합니다." }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("잔고가 부족") == true)
        }
    }

    @Test
    fun `충전 시 히스토리 기록`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 0L, 1000L)
        every { userPointTable.insertOrUpdate(1L, 1000L) } returns UserPoint(1L, 1000L, 2000L)
        every { pointHistoryTable.insert(any(), any(), any(), any()) } returns PointHistory(1L, 1L, TransactionType.CHARGE, 1000L, 2000L)
        pointUseCase.charge(1L, 1000L)
        verify { pointHistoryTable.insert(1L, 1000L, TransactionType.CHARGE, any()) }
    }

    @Test
    fun `사용 시 히스토리 기록`() {
        every { userPointTable.selectById(1L) } returns UserPoint(1L, 1000L, 1000L)
        every { userPointTable.insertOrUpdate(1L, 500L) } returns UserPoint(1L, 500L, 2000L)
        every { pointHistoryTable.insert(any(), any(), any(), any()) } returns PointHistory(2L, 1L, TransactionType.USE, 500L, 2000L)
        pointUseCase.use(1L, 500L)
        verify { pointHistoryTable.insert(1L, 500L, TransactionType.USE, any()) }
    }
}
