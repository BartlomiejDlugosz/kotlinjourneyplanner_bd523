package journeyplan

import java.net.URL
import kotlin.concurrent.thread

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
class SubwayMap(val segments: List<Segment>)

fun lookUpStationCode(stationCode: String, stations: String): String {
  return stations.substringAfter(stationCode).substringAfter("commonName\":\"").substringBefore("\"")
    .substringBefore(" Underground Station")
}

fun getStationNames(line: String): String {
  return URL("https://api.tfl.gov.uk/Line/$line/StopPoints?app_id=REDACTED&app_key=REDACTED").readText()
}

fun getStations(line: String): List<List<String>> {
  val data =
    URL("https://api.tfl.gov.uk/Line/$line/Route/Sequence/all?app_id=REDACTED&app_key=REDACTED").readText()
  val orderedStations = data.substringAfter("orderedLineRoutes")
  return orderedStations.split("},").map { x ->
    x.substringAfter("naptanIds\":[\"").substringBefore("\"]").split("\",\"")
  }
}

fun londonUnderground(): SubwayMap {
  val segments: MutableList<Segment> = mutableListOf()

  val threads: MutableList<Thread> = mutableListOf()

  for (line in lines) {
    val t = thread {
      val routes = getStations(line)
      val centralStations = getStationNames(line)
      val tempSegment: MutableList<Segment> = mutableListOf()

      for (route in routes) {
        for (i in 0..<(route.size - 1)) {
          tempSegment.add(
            Segment(
              Station(lookUpStationCode(route[i], centralStations)),
              Station(lookUpStationCode(route[i + 1], centralStations)),
              Line(line.capitalize())
            )
          )

          segments.addAll(tempSegment.distinct())
        }
      }
    }
    threads.add(t)
  }
  threads.forEach { it.join() }
  println(segments.distinct())
  return SubwayMap(segments.distinct())
}

fun main() {
//    lookUpStationCode("940GZZLUEBY")
  londonUnderground()
}