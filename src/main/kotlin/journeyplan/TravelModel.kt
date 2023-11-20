package journeyplan

// Add your code for modelling public transport networks in this file.
class Station(val name: String) {
  override fun toString() = name
}

class Line(val name: String) {
  override fun toString() = name
}

class Segment(val station1: Station, val station2: Station, val line: Line) {
  override fun toString() = "$station1 to $station2 on the $line line."
}
