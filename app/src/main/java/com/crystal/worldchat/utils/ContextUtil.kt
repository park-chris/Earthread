package com.crystal.worldchat.utils

import android.content.Context
import com.crystal.worldchat.datas.User
import org.json.JSONArray

class ContextUtil(context: Context) {

    companion object {
        private const val prefName = "worldchatPref"
        private const val userId = "userId"
        private const val userName = "userName"
        private const val topicList = "topicList"

        fun setUserInfo(context: Context, user: User) {

            val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

            pref.edit().putString(userId, user.uid).apply()
            pref.edit().putString(userName, user.name).apply()

        }

        fun getUserId(context: Context): String? {

            val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

            return pref.getString(userId, "")
        }

        fun getUserName(context: Context): String? {

            val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

            return pref.getString(userName, "")
        }

        fun setFilterTopic(context: Context, topics: ArrayList<String>) {

            val jsonArr = JSONArray()
            for (i in topics) {
                jsonArr.put(i)
            }

            val result = jsonArr.toString()

            val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE) ?: return

                pref.edit().putString(topicList, result).apply()
        }

        fun getFilterTopic(context: Context): ArrayList<String> {
            val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

            val getString = pref.getString(topicList, "")

            val resultArr: ArrayList<String> = arrayListOf()


            if (getString == "") {
                return resultArr
            }

            val arrJson = JSONArray(getString)


            for (i in 0 until arrJson.length()) {
                resultArr.add(arrJson.optString(i))
            }

            return resultArr
        }



    }
}