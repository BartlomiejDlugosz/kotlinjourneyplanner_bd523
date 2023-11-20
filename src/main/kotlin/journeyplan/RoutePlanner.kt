package journeyplan

import java.net.URL

private val url =
    "https://api.tfl.gov.uk/Line/central/Route/Sequence/all?app_id=REDACTED&app_key=REDACTED"
private val centralStations = URL("https://api.tfl.gov.uk/Line/central/StopPoints?app_id=REDACTED&app_key=REDACTED").readText()
// Add your code for the route planner in this file.
class SubwayMap(val segments: List<Segment>)

fun lookUpStationCode(stationCode: String): String {
    return centralStations.substringAfter(stationCode).substringAfter("commonName\":\"").substringBefore("\"")
}

fun londonUnderground(): SubwayMap {
    val data = URL(url).readText()
    val orderedStations = data.substringAfter("orderedLineRoutes")
    val stations = orderedStations.substringAfter("naptanIds\":[\"").substringBefore("\"]").split("\",\"")
    println(stations)
    val sub = SubwayMap(emptyList())
    return sub
}

fun main() {
    lookUpStationCode("940GZZLUEBY")
}