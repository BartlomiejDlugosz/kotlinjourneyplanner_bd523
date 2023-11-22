package journeyplan

import java.lang.Math.toRadians
import java.net.URL
import java.util.Collections.newSetFromMap
import java.util.concurrent.ConcurrentHashMap
import javax.print.attribute.standard.Destination
import kotlin.concurrent.thread
import kotlin.math.*

val lines: List<String> = listOf(
  "central",
  "circle",
//  "district",
//  "northern",
//  "jubilee",
//  "bakerloo",
  "piccadilly",
//  "metropolitan",
  "victoria",
//  "hammersmith-city",
//  "elizabeth"
)

// Add your code for the route planner in this file.
class SubwayMap(val segments: List<Segment>) {

  fun routesFrom(origin: Station, destination: Station): List<Route> {
    val routes = mutableListOf<Route>()
    if (origin == destination) {
      routes.add(Route(listOf()))
      return routes
    }
    val directSegments = segments.filter{ it.station1 == origin }
    if (directSegments.isEmpty()) return emptyList()
    val threads = mutableListOf<Thread>()
    for (segment in directSegments) {
      val t = thread {
        val currentRoutes = routesFromHelper(segment.station2, destination, listOf(origin))
        if (currentRoutes.isNotEmpty()) routes.addAll(currentRoutes.map { Route(listOf(segment) + it.segments) })
      }
      threads.add(t)
    }
    threads.forEach{it.join()}
    return routes
  }
  fun routesFromHelper(origin: Station, destination: Station, visitedStations: List<Station> = emptyList()): List<Route> {
//    if (origin == destination) return emptyList()

    var routes = mutableListOf<Route>()
    if (origin == destination) {
      // Create a route with a single segment if the origin is the destination
      routes.add(Route(listOf()))
      return routes
    }
    val directSegments = segments.filter{ it.station1 == origin && it.station2 !in visitedStations }
    if (directSegments.isEmpty()) return emptyList()

    for (segment in directSegments) {
//      val currentRoute = mutableListOf<Segment>(segment)
//      currentRoute.addAll(findRoute(segment.station2, destination, listOf(origin)))
//      if (currentRoute.last().station2 == destination) routes.add(Route(currentRoute))
      val currentRoutes = routesFromHelper(segment.station2, destination, visitedStations.plus(origin))//.map{ Route(listOf(segment) + it.segments) }
      if (currentRoutes.isNotEmpty()) routes.addAll(currentRoutes.map{ Route(listOf(segment) + it.segments) })
      routes = routes.distinct().toMutableList()
      routes = routes.filter{ it.segments.last().station2 == destination }.toMutableList()
//      routes.addAll(currentRoutes)
    }

    return routes
  }
}
class Route(val segments: List<Segment>) {
  override fun toString(): String = segments.toString()
}

fun main() {

  val paths = londonUndergroundCustom().routesFrom(Station("South Kensington"), Station("Victoria"))
//  val paths = londonUnderground().routesFrom(Station("Chesham"), Station("Charing Cross"))
//  val paths = londonUnderground().segments.filter { it.station1 == Station("Charing Cross") || it.station2 == Station("Charing Cross")}
  for (path in paths) {
    println(path)
  }

//  val stations = londonUnderground()

//  val route = listOf(Route(listOf(Segment(Station("North Acton"), Station("East Acton"), Line("Central"), 2), Segment(Station("East Acton"), Station("White City"), Line("Central"), 2))))
//  println(route.map{ Route(listOf(Segment(Station("West Acton"), Station("North Acton"), Line("Central"), 2)) + it.segments) })
}