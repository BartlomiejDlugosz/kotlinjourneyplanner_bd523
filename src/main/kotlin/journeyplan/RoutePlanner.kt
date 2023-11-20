package journeyplan

import java.lang.Math.toRadians
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.*

val lines: List<String> = listOf(
  "central",
  "circle",
  "district",
  "northern",
  "jubilee",
  "bakerloo",
  "piccadilly",
  "metropolitan",
  "victoria",
  "hammersmith-city",
  "elizabeth"
)

// Add your code for the route planner in this file.
class SubwayMap(val segments: List<Segment>) {
  fun routesFrom(origin: Station, destination: Station): List<Route>{
    TODO()
  }
}
class Route(val segments: List<Segment>)

fun lookUpStationCode(stationCode: String, stations: String): String {
  //  println(stations.substringAfter(stationCode))
//  val stationInfo = URL("https://api.tfl.gov.uk/StopPoint/$stationCode?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d").readText()
//  val lat = stationInfo.substringAfterLast("lat\":").substringBefore(",")
//  val lon = stationInfo.substringAfterLast("lon\":").substringBefore("}")
//  println(stations.substringAfter(stationCode).substringAfter("lat\":").substringBefore(","))
  return stations.substringAfter(stationCode).substringAfter("commonName\":\"").substringBefore("\"")
    .substringBefore(" Underground Station")
}

fun getStationNames(line: String): String {
  return URL("https://api.tfl.gov.uk/Line/$line/StopPoints?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d").readText()
}

fun getStations(line: String): List<List<Pair<String, Pair<Double, Double>>>> {
  val data =
    URL("https://api.tfl.gov.uk/Line/$line/Route/Sequence/all?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d").readText()
  val orderedStations = data.substringAfter("orderedLineRoutes")
  val listOfStations = orderedStations.split("},").map { x ->
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

fun calculateTime(geo1: Pair<Double, Double>, geo2: Pair<Double, Double>): Int {
  var (lat1, lon1) = geo1
  var (lat2, lon2) = geo2
  lat1 = toRadians(lat1)
  lat2 = toRadians(lat2)
  lon1 = toRadians(lon1)
  lon2 = toRadians(lon2)

  val dlon = lon2 - lon1
  val dlat = lat2 - lat1

  val a = sin(dlat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2.0)
  val c = 2 * asin(sqrt(a))

  return max(((c * 6371.0) / 0.5498592).roundToInt(), 1)
}

fun londonUnderground(): SubwayMap {
  val segments: MutableList<Segment> = mutableListOf()

  val threads: MutableList<Thread> = mutableListOf()

  for (line in lines) {
    val t = thread {
      val routes = getStations(line)
      val centralStations = getStationNames(line)


      for (route in routes) {
        val tempSegment: MutableList<Segment> = mutableListOf()
        for (i in 0..<(route.size - 1)) {
          tempSegment.add(
            Segment(
              Station(lookUpStationCode(route[i].first, centralStations)),
              Station(lookUpStationCode(route[i + 1].first, centralStations)),
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

fun main() {
//  val centralStations = getStationNames("central")
//    lookUpStationCode("940GZZLUEBY", centralStations)

  londonUnderground()
}