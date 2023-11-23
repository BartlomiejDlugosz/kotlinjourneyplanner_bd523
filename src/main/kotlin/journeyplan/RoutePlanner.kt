package journeyplan

import java.util.*
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

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

// Add your code for the route planner in this file.
data class SubwayMap(private val segments: List<Segment>) {
  private val hashmap: HashMap<Station, List<Segment>> = hashMapOf()

  init {
    for (segment in segments) {
      hashmap[segment.from] = hashmap.getOrDefault(segment.from, emptyList()) + segment
    }
  }

  fun getStationByName(stationName: String): Station =
    segments.find { it.from.name == stationName }?.from ?: Station(stationName)

  fun routesFrom(
    origin: Station,
    destination: Station,
    visitedStations: List<Station> = listOf(origin),
    optimisingFor: (Route) -> Int = Route::duration
  ): List<Route> {
    val segmentsWeCanFollow = (hashmap[origin] ?: emptyList()).filter { it.to !in visitedStations }
    val routesToDestination = mutableListOf<List<Route>>()

    if (origin == destination) return listOf(Route(listOf()))

    for (segment in segmentsWeCanFollow) {
      routesToDestination.add(
        routesFrom(
          segment.to,
          destination,
          visitedStations + origin
        ).map { Route(listOf(segment) + it.segments) }
      )
    }

    return routesToDestination
      .flatten()
      .filter {
        it.segments.last().to == destination &&
          it.segments.none { it.line.suspended } &&
          it.segments.filterIndexed { index, segment -> segment.line != it.segments.getOrNull(index + 1)?.line && !segment.to.opened }
            .isEmpty()

      }
      .sortedBy { optimisingFor(it) }
  }

  fun findShortest(
    origin: Station,
    destination: Station
  ): Route? {
    data class Node(var segment: Segment, var metric: Double, var lastNode: Node? = null) {
      fun route(): Route {
        var current: Node? = this
        val path = mutableListOf<Segment>()

        while (current!!.lastNode != null) {
          path.add(0, current.segment)
          current = current.lastNode
        }
        path.add(0, current.segment)

        return Route(path)
      }
    }

    if (origin == destination) return null

    val nodes = PriorityQueue<Node>(compareBy<Node> { it.metric }.thenBy { it.segment.line.name })
    val visitedStations: MutableList<Station> = mutableListOf()

    val fromOrigin = (hashmap[origin] ?: emptyList())

    for (segment in fromOrigin) {
      val metric = calculateDistance(segment.to.geo, destination.geo) * 10 + segment.minutes.toDouble()
      nodes.add(Node(segment, metric))
    }

    var currentNode = nodes.peek()
    while (currentNode.segment.to != destination) {
      val fromNextNode = (hashmap[currentNode.segment.to] ?: emptyList()).filter { it.from !in visitedStations }

      for (segment in fromNextNode) {
        val metric =
          currentNode.metric + calculateDistance(segment.to.geo, destination.geo) + segment.minutes.toDouble()
        val exists = nodes.find { it.segment == segment }
        if (exists != null) {
          if (exists.metric > metric) nodes.remove(exists)
        } else {
          val newNode = Node(segment, metric, currentNode)
          nodes.add(newNode)
        }
      }
      visitedStations.add(currentNode.segment.from)
      nodes.remove()
      if (nodes.isEmpty()) return null
      currentNode = nodes.peek()
    }

    return currentNode.route()
  }
}

fun main() {
  val map = londonUndergroundCustom()

  println("STARTING")
  println(
    measureTimeMillis {
      val paths =
        map.findShortest(
          map.getStationByName("Whitechapel"),
          map.getStationByName("North Acton")
        )
      println(paths)
    }
  )
}
