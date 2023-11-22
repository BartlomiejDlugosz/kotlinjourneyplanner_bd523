package journeyplan

import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

val lines: List<String> = listOf(
//  "central",
    "circle",
    "district",
//  "northern",
//  "jubilee",
//  "bakerloo",
  "piccadilly",
//  "metropolitan",
  "victoria",
//  "hammersmith-city",
  "elizabeth"
)

// Add your code for the route planner in this file.
class SubwayMap(val segments: List<Segment>) {
    val hashmap: HashMap<Station, List<Segment>> = hashMapOf()
    init {

        for (segment in segments) {
            hashmap[segment.station1] = hashmap[segment.station1]?.plus(segment) ?: listOf(segment)
        }
//        println(hashmap)
    }
    fun routesFrom(origin: Station, destination: Station): List<Route> {
        val segmentsWeCanFollow = hashmap[origin] ?: emptyList()
        val routesToDestination = mutableListOf<Route>()

        if (origin == destination) {
            routesToDestination.add(Route(listOf()))
            return routesToDestination
        }

        val threads = mutableListOf<Thread>()
        for (segment in segmentsWeCanFollow) {
            val t = thread {
//                println("WORKING ON THREAD $segment")
                val currentRoutes =
                    routesFromHelper(segment.station2, destination, listOf(origin))
                routesToDestination.addAll(currentRoutes.map { Route(listOf(segment) + it.segments) })
//                println("FINISHED THREAD ${currentRoutes.map { Route(listOf(segment) + it.segments) }.size} ${segment}")
            }
            threads.add(t)
        }

        threads.forEach { it.join() }
        return routesToDestination.distinct().filter { it.segments.last().station2 == destination }
    }

    fun routesFromHelper(
        origin: Station,
        destination: Station,
        visitedStations: List<Station> = listOf(origin)
    ): List<Route> {
        val segmentsWeCanFollow = (hashmap[origin] ?: emptyList()).filter { it.station2 !in visitedStations }
        val routesToDestination = mutableListOf<Route>()

        if (origin == destination) {
            routesToDestination.add(Route(listOf()))
            return routesToDestination
        }

        for (segment in segmentsWeCanFollow) {
            val currentRoutes = routesFromHelper(segment.station2, destination, (visitedStations + origin).distinct())
            routesToDestination.addAll(currentRoutes.map { Route(listOf(segment) + it.segments) })
        }

        return routesToDestination.distinct().filter { it.segments.last().station2 == destination }
    }
}

data class Route(val segments: List<Segment>) {
    override fun toString(): String = segments.toString()

    override fun equals(other: Any?): Boolean = (other is Route) && this.segments == other.segments
}

fun main() {
    println(measureTimeMillis {
        val paths = londonUndergroundCustom().routesFrom(Station("South Kensington"), Station("Victoria"))
        println(paths.size)
    })

    println(measureTimeMillis {
        val paths = londonUndergroundCustom().routesFromHelper(Station("South Kensington"), Station("Victoria"))
        println(paths.size)
    })

//    39974
//    7149
//    39974
//    13861

//  val paths = londonUnderground().routesFrom(Station("Chesham"), Station("Charing Cross"))
//  val paths = londonUnderground().segments.filter { it.station1 == Station("Charing Cross") || it.station2 == Station("Charing Cross")}

//    paths.forEach { route ->
//        println(route)
//    }

//  val stations = londonUnderground()

//  val route = listOf(Route(listOf(Segment(Station("North Acton"), Station("East Acton"), Line("Central"), 2), Segment(Station("East Acton"), Station("White City"), Line("Central"), 2))))
//  println(route.map{ Route(listOf(Segment(Station("West Acton"), Station("North Acton"), Line("Central"), 2)) + it.segments) })
}