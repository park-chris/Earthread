package com.crystal.worldchat.utils

import java.text.SimpleDateFormat
import java.util.*

class UIUtil {

    companion object {


        //        Board에 현재 시간 저장할때 게시글 하나 생성할때 넣기
        fun timeStampToString(): String {
            val pattern = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            return pattern.format(Date())
        }

        //        DB에서 꺼내서 뷰에 보여줄때 / 나중에 오늘 날짜와 비교해서 같은 날짜면 시, 분 출력 /다른 날짜면 날짜 출력만들기
        fun stringTimeToStringDate(date: String): String {

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatDate: Date = format.parse(date)!!
            val pattern = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

            return pattern.format(formatDate)
        }

        fun stringTimeToStringDateTime(date: String): String {

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatDate: Date = format.parse(date)!!
            val pattern = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())

            return pattern.format(formatDate)
        }

        fun compareTimeStamp(date1: String, date2: String): String {

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val formatDate1: Date = format.parse(date1)!!
            val formatDate2: Date = format.parse(date2)!!
            val pattern = SimpleDateFormat("HH:mm", Locale.getDefault())

            return if (formatDate1 == formatDate2) {
                ""
            } else {
                pattern.format(formatDate1)
            }
        }

        fun compareYearDay(date1: String, date2: String): Boolean {

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatDate1: Date = format.parse(date1)!!
            val formatDate2: Date = format.parse(date2)!!


            return formatDate1 != formatDate2
        }

        fun formattedTime(date: String): String {

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val formatDate1: Date = format.parse(date)!!
            val pattern = SimpleDateFormat("HH:mm", Locale.getDefault())

            return pattern.format(formatDate1)
        }


        fun formattedYearDay(date: String): String {

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatDate: Date = format.parse(date)!!
            val pattern = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

            return pattern.format(formatDate)
        }


    }


}