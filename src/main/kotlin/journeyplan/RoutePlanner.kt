package journeyplan

import java.util.*
import kotlin.collections.HashMap

// Add your code for the route planner in this file.
data class SubwayMap(private val segments: List<Segment>) {
  private val hashmap: HashMap<Station, List<Segment>> = hashMapOf()

  init {
    for (segment in segments) {
      hashmap[segment.from] =
        hashmap.getOrDefault(segment.from, emptyList()) + segment
    }
  }

  fun getStationByName(stationName: String): Station =
    segments.find { it.from.name == stationName }?.from
      ?: Station(stationName)

  fun routesFrom(
    origin: Station,
    destination: Station,
    visitedStations: List<Station> = listOf(origin),
    optimisingFor: (Route) -> Int = Route::duration
  ): List<Route> {
    // Get all the segments we can follow from the origin
    val segmentsWeCanFollow =
      (hashmap[origin] ?: emptyList())
        .filter { it.to !in visitedStations }
    val routesToDestination = mutableListOf<List<Route>>()

    // Terminate if we've reached destination
    if (origin == destination) return listOf(Route(listOf()))

    for (segment in segmentsWeCanFollow) {
      // Add all the routes from the next station to the destination
      // and prepend the current segment
      routesToDestination.add(
        routesFrom(
          segment.to,
          destination,
          visitedStations + origin
        ).map { Route(listOf(segment) + it.segments) }
      )
    }

    // Return flattened list of routes to destination, filtered for
    // suspended lines and closed stations
    // and sorted by the optimising function
    return routesToDestination
      .flatten()
      .filter {
        it.segments.last().to == destination &&
                it.segments.none { it.line.suspended } &&
                it.segments.filterIndexed { index, segment ->
                  // If the current line and the next line
                  // are different
                  // and the station in between is closed
                  // return that item
                  // and then check if empty for valid routes
                  segment.line !=
                          it.segments.getOrNull(index + 1)
                            ?.line && !segment.to.opened
                }.isEmpty()
      }
      .sortedBy { optimisingFor(it) }
  }

  fun findShortest(
    origin: Station,
    destination: Station,
    useDistance: Boolean = false
  ): Route? {
    data class Node(
      var segment: Segment,
      var metric: Double,
      var lastNode: Node? = null
    ) {
      // Reconstruct route from last nodes
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

      // Update the node
      fun update(
        segment: Segment,
        metric: Double,
        lastNode: Node? = null
      ) {
        this.segment = segment
        this.metric = metric
        this.lastNode = lastNode
      }
    }

    // Terminate if we've reached destination
    if (origin == destination) return null

    // Holds all the nodes in an ordered queue
    val nodes =
      PriorityQueue<Node>(
        compareBy<Node> { it.metric }.thenBy { it.segment.line.name }
      )

    // Initialise all the nodes with either the metric from the origin
    // or a large number otherwise
    for (segment in segments) {
      nodes.add(
        Node(
          segment,
          if (segment.from == origin)
            (if (useDistance)
              calculateDistance(segment.to.geo, destination.geo) - calculateDistance(
                segment.to.geo,
                destination.geo
              )
            else 0.0) + segment.minutes.toDouble()
          else
            Double.POSITIVE_INFINITY
        )
      )
    }

    // Keeps track of the visited stations to prevent loops
    val visitedStations: MutableList<Station> = mutableListOf(origin)

    // Get next node from queue
    var currentNode = nodes.peek()
    while (currentNode.segment.to != destination) {
      // Get all the segments from the next node
      // and filter out the visited stations
      val fromNextNode =
        (
                hashmap[currentNode.segment.to]
                  ?: emptyList()
                ).filter { it.from !in visitedStations }

      // Go over segments and update their node if the metric is lower
      for (segment in fromNextNode) {
        var metric =
          currentNode.metric +
                  (if (useDistance)
                    calculateDistance(segment.to.geo, destination.geo) - calculateDistance(
                      currentNode.segment.to.geo,
                      destination.geo
                    )
                  else 0.0) + segment.minutes.toDouble()
        if (segment.line != currentNode.segment.line) metric += 10
        val segmentNode = nodes.find { it.segment == segment }
        if (segmentNode != null && segmentNode.metric > metric) {
          // Remove the node and add it back with the updated metric
          // so priority queue can sort it
          nodes.remove(segmentNode)
          segmentNode.update(segment, metric, currentNode)
          nodes.add(segmentNode)
        }
      }
      // Add the current station to the visited stations
      visitedStations.add(currentNode.segment.from)
      if (nodes.isEmpty()) return null
      // Remove the current node and get the next one
      nodes.remove()
      currentNode = nodes.peek()
    }
    // Return the reconstructed route
    return currentNode.route()
  }
}

fun main() {
  val map = londonUnderground()
  println("STARTING")
  map.getStationByName("Paddington").close()

  val paths =
    map.findShortest(
      map.getStationByName("North Acton"),
      map.getStationByName("Paddington"),
      true
    )
  println(paths)
}
