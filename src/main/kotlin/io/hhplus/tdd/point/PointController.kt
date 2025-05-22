package io.hhplus.tdd.point

import PointUseCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/point")
class PointController(
    private val pointUseCase: PointUseCase,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 유저 포인트 조회 API
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint = pointUseCase.getUserPoint(id)

    /**
     * 유저의 포인트 히스토리 조회 API
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> = pointUseCase.getUserPointHistory(id)

    /**
     * 유저의 포인트를 충전하는 API
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint = pointUseCase.charge(id, amount)

    /**
     * 유저의 포인트를 사용하는 API
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint = pointUseCase.use(id, amount)
}
