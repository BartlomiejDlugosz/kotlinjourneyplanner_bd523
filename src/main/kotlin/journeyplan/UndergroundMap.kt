package journeyplan

import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

fun lookUpStationCode(
  stationCode: String,
  stations: String
): String {
  return stations.substringAfter(stationCode).substringAfter("commonName\":\"").substringBefore("\"")
    .substringBefore(" Underground Station")
}

fun getStationNames(line: String): String {
  return URL(
    "https://api.tfl.gov.uk/Line/$line/StopPoints?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d"
  ).readText()
}

fun getStations(line: String): List<List<Pair<String, Pair<Double, Double>>>> {
  val data =
    URL(
      "https://api.tfl.gov.uk/Line/$line/Route/Sequence/all?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d"
    ).readText()
  val orderedStations = data.substringAfter("orderedLineRoutes")
  val listOfStations =
    orderedStations.split("},").map { x ->
      x.substringAfter("naptanIds\":[\"").substringBefore("\"]").split("\",\"")
    }

  val stations = data.substringAfter("stations").substringBefore("orderedLineRoutes")
  val newList: MutableList<MutableList<Pair<String, Pair<Double, Double>>>> = mutableListOf()

  for (route in listOfStations) {
    val tempList: MutableList<Pair<String, Pair<Double, Double>>> = mutableListOf()
    for (station in route) {
      val lat = stations.substringAfter(station).substringAfter("lat\":").substringBefore(",")
      val lon = stations.substringAfter(station).substringAfter("lon\":").substringBefore("}")
      tempList.add(Pair(station, Pair(lat.toDouble(), lon.toDouble())))
    }
    newList.add(tempList)
  }

  return newList
}

fun calculateDistance(
  geo1: Pair<Double, Double>,
  geo2: Pair<Double, Double>
): Double {
  var (lat1, lon1) = geo1
  var (lat2, lon2) = geo2
  lat1 = Math.toRadians(lat1)
  lat2 = Math.toRadians(lat2)
  lon1 = Math.toRadians(lon1)
  lon2 = Math.toRadians(lon2)

  val dlon = lon2 - lon1
  val dlat = lat2 - lat1

  val a = sin(dlat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2.0)
  val c = 2 * asin(sqrt(a))

  return c * 6371.0
}

fun calculateTime(
  geo1: Pair<Double, Double>,
  geo2: Pair<Double, Double>
): Int = max((calculateDistance(geo1, geo2) / 0.5498592).roundToInt(), 1)

fun londonUndergroundCustom(): SubwayMap {
  val segments: MutableList<Segment> = mutableListOf()

  val threads: MutableList<Thread> = mutableListOf()

  for (line in lines) {
    val t =
      thread {
        val routes = getStations(line)
        val centralStations = getStationNames(line)

        for (route in routes) {
          val tempSegment: MutableList<Segment> = mutableListOf()
          for (i in 0..<(route.size - 1)) {
            tempSegment.add(
              Segment(
                Station(lookUpStationCode(route[i].first, centralStations), route[i].second),
                Station(lookUpStationCode(route[i + 1].first, centralStations), route[i + 1].second),
                Line(line.capitalize()),
                calculateTime(route[i].second, route[i + 1].second)
              )
            )
          }
          segments.addAll(tempSegment.distinct())
        }
      }
    threads.add(t)
  }
  threads.forEach { it.join() }
  return SubwayMap(segments.distinct())
}
