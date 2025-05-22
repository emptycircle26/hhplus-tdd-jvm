package io.hhplus.tdd.point

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointControllerTest {
    private lateinit var controller: PointController

    @BeforeEach
    fun setup() {
        val userPointTable = FakeUserPointTable()
        val pointHistoryTable = FakePointHistoryTable()
        val service = PointService(userPointTable, pointHistoryTable)
        controller = PointController(service)
    }

    @Test
    fun `포인트 조회 - 기본값 0 반환`() {
        // 목적: 유저가 없을 때 0포인트 반환
        val result = controller.point(1L)
        assertEquals(0, result.point)
    }

    @Test
    fun `포인트 충전 - 정상 케이스`() {
        // 목적: 정상적으로 포인트 충전이 되는지 확인
        val charged = controller.charge(1L, 1000L)
        assertEquals(1000L, charged.point)
    }

    @Test
    fun `포인트 사용 - 정상 케이스`() {
        // 목적: 충전 후 포인트 사용이 정상적으로 되는지 확인
        controller.charge(1L, 2000L)
        val used = controller.use(1L, 500L)
        assertEquals(1500L, used.point)
    }

    @Test
    fun `포인트 사용 - 잔고 부족 예외`() {
        // 목적: 잔고가 부족할 때 예외가 발생하는지 확인
        controller.charge(1L, 100L)
        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                controller.use(1L, 200L)
            }
        assertTrue(ex.message!!.contains("잔고가 부족"))
    }

    @Test
    fun `포인트 내역 조회 - 충전, 사용 내역 확인`() {
        // 목적: 충전/사용 내역이 올바르게 기록되는지 확인
        controller.charge(1L, 1000L)
        controller.use(1L, 200L)
        val histories = controller.history(1L)
        assertEquals(2, histories.size)
        assertEquals(PointHistoryType.CHARGE, histories[0].type)
        assertEquals(PointHistoryType.USE, histories[1].type)
    }

    @Test
    fun `포인트 충전 - 음수 금액 예외`() {
        // 목적: 음수 금액 충전 시 예외 발생
        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                controller.charge(1L, -100L)
            }
        assertTrue(ex.message!!.contains("충전 금액은 0보다 커야"))
    }

    @Test
    fun `포인트 사용 - 음수 금액 예외`() {
        // 목적: 음수 금액 사용 시 예외 발생
        val ex =
            assertThrows(IllegalArgumentException::class.java) {
                controller.use(1L, -100L)
            }
        assertTrue(ex.message!!.contains("사용 금액은 0보다 커야"))
    }
}
