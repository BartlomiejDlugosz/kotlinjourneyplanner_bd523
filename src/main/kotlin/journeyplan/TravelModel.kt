package journeyplan

// Add your code for modelling public transport networks in this file.
data class Station(val name: String, val geo: Pair<Double, Double> = Pair(0.0, 0.0), var opened: Boolean = true) {
  fun close() {
    opened = false
  }

  fun open() {
    opened = true
  }

  override fun toString() = name

  override fun equals(other: Any?): Boolean = (other is Station) && this.name == other.name

  override fun hashCode(): Int {
    return name.hashCode()
  }
}

data class Line(val name: String, var suspended: Boolean = false) {
  fun suspend() {
    suspended = true
  }

  fun resume() {
    suspended = false
  }

  override fun toString() = "$name Line"

  override fun equals(other: Any?): Boolean = (other is Line) && this.name == other.name

  override fun hashCode(): Int {
    return name.hashCode()
  }
}

data class Segment(val from: Station, val to: Station, val line: Line, val minutes: Int) {
  override fun toString() = "$from to $to by $line"

  override fun equals(other: Any?): Boolean =
    (other is Segment) && this.from == other.from && this.to == other.to && this.line == other.line

  override fun hashCode(): Int {
    var result = from.hashCode()
    result = 31 * result + to.hashCode()
    result = 31 * result + line.hashCode()
    result = 31 * result + minutes
    return result
  }
}

data class Route(val segments: List<Segment>) {
  override fun toString(): String {
    var str =
      "${segments.first().from} to ${segments.last().to} - ${this.duration()} minutes, ${this.numChanges()} changes\n"
    var currentLine: Line = segments.first().line
    str += " - ${segments.first().from}"
    for (segment in segments) {
      if (segment.line != currentLine) str += " to ${segment.from} by $currentLine\n - ${segment.from}"
      currentLine = segment.line
    }
    str += " to ${segments.last().to} by $currentLine"
    return str
  }

  fun duration(): Int = segments.sumOf { it.minutes }

  companion object {
    fun duration(route: Route): Int = route.duration()
  }

  fun numChanges(): Int {
    var count = 0
    for (i in 0..<segments.size - 1) {
      if (segments[i].line != segments[i + 1].line) count++
    }
    return count
  }

  override fun equals(other: Any?): Boolean = (other is Route) && this.segments == other.segments

  override fun hashCode(): Int {
    return segments.hashCode()
  }
}
