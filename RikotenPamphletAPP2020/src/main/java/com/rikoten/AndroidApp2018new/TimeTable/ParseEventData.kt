package com.rikoten.AndroidApp2018new.TimeTable

import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.TimeTableBlankPeriod

import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.TimeTablePeriod
import com.rikoten.AndroidApp2018new.SharingMaterial.DataType.TimeTableEventProperty

private fun fillWithSpacer(programs: List<TimeTableEventProperty>): List<TimeTablePeriod> {
    if (programs.isEmpty()) return programs

    val sortedPrograms = programs.sortedBy { it.start_timestamp }
    //最も早い企画ののスタート時間
    val firstProgramstart_timestamp = sortedPrograms.first().start_timestamp
    //最も遅い企画の終了時間
    val lastProgramend_timestamp = sortedPrograms.maxBy { it.end_timestamp }?.end_timestamp ?: return programs
    //ステージナンバーを抽出してlane_location_numsに入れる
    val lane_location_nums = sortedPrograms.map { it.lane_location_num }.distinct()

    val filledPeriod = ArrayList<TimeTablePeriod>()
    lane_location_nums.forEach { roomNumber ->
        //各ステージごとに埋めていく
        //そのステージの企画をsessionInSameRoomに抽出
        val sessionsInSameRoom = sortedPrograms.filter { it.lane_location_num == roomNumber }
        sessionsInSameRoom.forEachIndexed { index, session ->
            //そのステージの最初の企画が全体での最初の企画ではないとき
            if (index == 0 && session.start_timestamp > firstProgramstart_timestamp)
            //空の企画で埋めていく
                filledPeriod.add(
                    TimeTableBlankPeriod(
                        firstProgramstart_timestamp,
                        session.start_timestamp,
                        roomNumber
                    )
                )

            //今見ているsessionを追加する
            filledPeriod.add(session)

            //最後のこの列のsessionであり、すべての企画の中で一番最後ではないとき
            if (index == sessionsInSameRoom.size - 1 && session.end_timestamp < lastProgramend_timestamp) {
                //空の企画を入れる
                filledPeriod.add(
                    TimeTableBlankPeriod(
                        session.end_timestamp,
                        lastProgramend_timestamp,
                        roomNumber
                    )
                )
            }

            //次のセッションがなかったら、forEachの次のループへと行く
            val nextSession = sessionsInSameRoom.getOrNull(index + 1) ?: return@forEachIndexed
            //次のセッションがあって、つながってなかったらTimeTableBlankPeriodで間をつなげる。
            if (session.end_timestamp != nextSession.start_timestamp)
                filledPeriod.add(
                    TimeTableBlankPeriod(
                        session.end_timestamp,
                        nextSession.start_timestamp,
                        roomNumber
                    )
                )
        }
    }
    return filledPeriod.sortedBy { it.start_timestamp }
}