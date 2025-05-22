package io.hhplus.tdd.point

import PointUseCase
import PointUseCaseImpl
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointControllerTest {
    private lateinit var controller: PointController
    private lateinit var pointUseCase: PointUseCase

    @BeforeEach
    fun setup() {
        pointUseCase = PointUseCaseImpl(UserPointTable(), PointHistoryTable())
        controller = PointController(pointUseCase)
    }

    @Test
    fun `포인트 조회 - 기본값 0 반환`() {
        val result = controller.point(1L)
        assertEquals(0, result.point)
    }

    @Test
    fun `포인트 충전 - 정상 케이스`() {
        val result = controller.point(1L)
        assertEquals(0, result.point)

        val charged = controller.charge(1L, 1000L)
        assertEquals(1L, charged.id)
        assertEquals(1000L, charged.point)
        assertTrue(charged.updateMillis != 0L)
    }

    @Test
    fun `포인트 사용 - 정상 케이스`() {
        val charged = controller.charge(1L, 1000L)
        assertEquals(1000L, charged.point)

        val used = controller.use(1L, 500L)
        assertEquals(1L, charged.id)
        assertEquals(500L, used.point)
    }

    @Test
    fun `포인트 사용 - 잔고 부족 예외`() {
        val charged = controller.charge(1L, 1000L)
        assertEquals(1000L, charged.point)

        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                controller.use(1L, 2000L)
            }
        assertTrue(ex.message!!.contains("잔고가 부족합니다"))
    }

    @Test
    fun `포인트 내역 조회 - 충전, 사용 내역 확인`() {
        // 목적: 충전/사용 내역이 올바르게 기록되는지 확인
        controller.charge(1L, 1000L)
        controller.point(1L)
        controller.use(1L, 200L)
        val histories = controller.history(1L)
        assertEquals(2, histories.size)
        assertEquals(TransactionType.CHARGE, histories[0].type)
        assertEquals(TransactionType.USE, histories[1].type)
    }

    @Test
    fun `포인트 충전 - 음수 금액 예외`() {
        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                controller.charge(1L, -100L)
            }
        assertTrue(ex.message!!.contains("충전 금액은 0보다 커야합니다"))
    }

    @Test
    fun `포인트 사용 - 음수 금액 예외`() {
        // 목적: 음수 금액 사용 시 예외 발생
        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                controller.use(1L, -100L)
            }
        assertTrue(ex.message!!.contains("사용 금액은 0보다 커야합니다"))
    }
}
