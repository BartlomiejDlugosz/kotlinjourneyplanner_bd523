package journeyplan

// Add your code for modelling public transport networks in this file.
data class Station(val name: String) {
  override fun toString() = name
  override fun equals(other: Any?): Boolean = (other is Station) && this.name == other.name
}

data class Line(val name: String) {
  override fun toString() = "$name Line"
  override fun equals(other: Any?): Boolean = (other is Line) && this.name == other.name
}

data class Segment(val station1: Station, val station2: Station, val line: Line, val minutes: Int) {
  override fun toString() = "$station1 - $station2 on the $line line in $minutes minutes"
  override fun equals(other: Any?): Boolean = (other is Segment) && this.station1 == other.station1 && this.station2 == other.station2 && this.line == other.line
}
