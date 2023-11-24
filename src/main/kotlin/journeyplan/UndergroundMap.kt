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

const val tflAppId = "80a6fb65fbfc4bed889483642b39ca85"
const val tflAppKey = "bbe228b0eaa94cc9be87ba869cee5b7d"

// Contains list of all lines on the underground
val lines: List<String> =
  listOf(
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

// Takes a station code and the stations and extracts the station name
fun lookUpStationCode(
  stationCode: String,
  stations: String
): String {
  return stations.substringAfter(stationCode)
    .substringAfter("commonName\":\"")
    .substringBefore("\"")
    .substringBefore(" Underground Station")
}

// Uses the tfl API to get detailed information about the stop points like station name, and geo coordinates
fun getStationNames(line: String): String {
  return URL(
    "https://api.tfl.gov.uk/Line/$line/StopPoints?" +
      "app_id=$tflAppId&" +
      "app_key=$tflAppKey"
  ).readText()
}

// Uses the tfl api to get all the routes on a line, which includes all the stations in order
// Returns a list of all possible routes
// e.g. [[(North acton, geo), (East Acton, geo)...], [(Notting Hill Gate, geo), ...]]
fun getStations(line: String): List<List<Pair<String, geoCoordinate>>> {
  val data =
    URL(
      "https://api.tfl.gov.uk/Line/$line/Route/Sequence/all?" +
        "app_id=$tflAppId&" +
        "app_key=$tflAppKey"
    ).readText()
  val orderedStations = data.substringAfter("orderedLineRoutes")
  val listOfStations =
    orderedStations.split("},").map { x ->
      x.substringAfter("naptanIds\":[\"")
        .substringBefore("\"]")
        .split("\",\"")
    }

  val stations =
    data.substringAfter("stations")
      .substringBefore("orderedLineRoutes")
  val listOfRoutes: MutableList<List<Pair<String, Pair<Double, Double>>>> =
    mutableListOf()

  for (route in listOfStations) {
    val currentRoute: MutableList<Pair<String, Pair<Double, Double>>> =
      mutableListOf()
    for (station in route) {
      val lat =
        stations.substringAfter(station)
          .substringAfter("lat\":")
          .substringBefore(",")
      val lon =
        stations.substringAfter(station)
          .substringAfter("lon\":")
          .substringBefore("}")
      currentRoute.add(Pair(station, Pair(lat.toDouble(), lon.toDouble())))
    }
    listOfRoutes.add(currentRoute)
  }

  return listOfRoutes
}

// Calculates the distance between two geo coordinates
fun calculateDistance(
  geo1: geoCoordinate,
  geo2: geoCoordinate
): Double {
  var (lat1, lon1) = geo1
  var (lat2, lon2) = geo2
  lat1 = Math.toRadians(lat1)
  lat2 = Math.toRadians(lat2)
  lon1 = Math.toRadians(lon1)
  lon2 = Math.toRadians(lon2)

  val dlon = lon2 - lon1
  val dlat = lat2 - lat1

  val a =
    sin(dlat / 2).pow(2.0) +
      cos(lat1) * cos(lat2) *
      sin(dlon / 2).pow(2.0)
  val c = 2 * asin(sqrt(a))

  return c * 6371.0
}

// Calculates rough amount of time taken to travel between two stations
fun calculateTime(
  geo1: geoCoordinate,
  geo2: geoCoordinate
): Int = max((calculateDistance(geo1, geo2) / 0.5498592).roundToInt(), 1)

// Creates the map of the london underground
fun londonUndergroundCustom(): SubwayMap {
  val segments: MutableList<Segment> = mutableListOf()

  // Uses multithreading to speed up the process
  val threads: MutableList<Thread> = mutableListOf()

  for (line in lines) {
    // One thread per line
    val t =
      thread {
        val listOfRoutes = getStations(line)
        val stationNames = getStationNames(line)

        for (route in listOfRoutes) {
          val tempSegment: MutableList<Segment> = mutableListOf()
          for (i in 0..<(route.size - 1)) {
            tempSegment.add(
              Segment(
                // Looks up the station code to get the station name
                Station(
                  lookUpStationCode(route[i].first, stationNames),
                  route[i].second
                ),
                Station(
                  lookUpStationCode(route[i + 1].first, stationNames),
                  route[i + 1].second
                ),
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
