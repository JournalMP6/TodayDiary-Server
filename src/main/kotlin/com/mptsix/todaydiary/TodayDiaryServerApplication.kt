package com.mptsix.todaydiary

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodayDiaryServerApplication

fun main(args: Array<String>) {
	runApplication<TodayDiaryServerApplication>(*args)
}
