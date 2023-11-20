package journeyplan

import java.net.URL

private val url =
    "https://api.tfl.gov.uk/Line/central/Route/Sequence/all?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d"
private val centralStations = URL("https://api.tfl.gov.uk/Line/central/StopPoints?app_id=80a6fb65fbfc4bed889483642b39ca85&app_key=bbe228b0eaa94cc9be87ba869cee5b7d").readText()
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