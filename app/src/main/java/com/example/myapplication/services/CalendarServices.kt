package com.example.myapplication.services

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalendarServices {

    companion object {
        fun milisToDate(milis: Long): String {
            var date: LocalDate = LocalDate.from(Instant.ofEpochMilli(milis).atZone(ZoneId.of("GMT+3")))
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }

        fun milisToDate(milis: String): String {
            var date: LocalDate = LocalDate.from(Instant.ofEpochMilli(milis.toLong()).atZone(ZoneId.of("GMT+3")))
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }
}