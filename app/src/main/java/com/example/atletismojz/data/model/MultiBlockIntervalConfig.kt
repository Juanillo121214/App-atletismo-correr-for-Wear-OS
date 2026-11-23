package com.example.atletismojz.data.model

data class IntervalBlock(
    val workSeconds: Int = 30,
    val restSeconds: Int = 30,
    val totalSets: Int = 5
)

data class MultiBlockIntervalConfig(
    val blocks: List<IntervalBlock> = listOf(
        IntervalBlock(30, 30, 5),
        IntervalBlock(45, 45, 4),
        IntervalBlock(60, 30, 3)
    )
)
