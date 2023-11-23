package journeyplan

import org.junit.Test
import kotlin.test.assertEquals

class RoutePlannerTest {
  val northernLine = Line("Northern")
  val victoriaLine = Line("Victoria")
  val centralLine = Line("Central")

  val highgate = Station("Highgate")
  val archway = Station("Archway")
  val tufnellPark = Station("Tufnell Park")
  val kentishTown = Station("Kentish Town")
  val camden = Station("Camden Town")
  val euston = Station("Euston")
  val warrenStreet = Station("Warren Street")
  val oxfordCircus = Station("Oxford Circus")
  val bondStreet = Station("Bond Street")

  val tufnellParkToHighgate =
    Route(
      listOf(
        Segment(tufnellPark, archway, northernLine, 3),
        Segment(archway, highgate, northernLine, 3)
      )
    )

  val highgateToOxfordCircus =
    Route(
      listOf(
        Segment(highgate, archway, northernLine, 3),
        Segment(archway, kentishTown, northernLine, 3),
        Segment(kentishTown, camden, northernLine, 3),
        Segment(camden, euston, northernLine, 3),
        Segment(euston, warrenStreet, victoriaLine, 3),
        Segment(warrenStreet, oxfordCircus, victoriaLine, 3)
      )
    )

  val camdenToBondStreet =
    Route(
      listOf(
        Segment(camden, euston, northernLine, 3),
        Segment(euston, warrenStreet, victoriaLine, 3),
        Segment(warrenStreet, oxfordCircus, victoriaLine, 3),
        Segment(oxfordCircus, bondStreet, centralLine, 2)
      )
    )

  @Test
  fun `can calculate number of changes`() {
    assertEquals(0, tufnellParkToHighgate.numChanges())
    assertEquals(1, highgateToOxfordCircus.numChanges())
    assertEquals(2, camdenToBondStreet.numChanges())
  }

  @Test
  fun `can calculate total duration`() {
    assertEquals(6, tufnellParkToHighgate.duration())
    assertEquals(18, highgateToOxfordCircus.duration())
    assertEquals(11, camdenToBondStreet.duration())
  }

  @Test
  fun `toString omits calling points`() {
    assertEquals(
      """
      Tufnell Park to Highgate - 6 minutes, 0 changes
       - Tufnell Park to Highgate by Northern Line
      """.trimIndent(),
      tufnellParkToHighgate.toString()
    )
  }

  @Test
  fun `toString shows changes`() {
    assertEquals(
      """
      Highgate to Oxford Circus - 18 minutes, 1 changes
       - Highgate to Euston by Northern Line
       - Euston to Oxford Circus by Victoria Line
      """.trimIndent(),
      highgateToOxfordCircus.toString()
    )
  }

  @Test
  fun `toString shows multiple changes`() {
    assertEquals(
      """
      Camden Town to Bond Street - 11 minutes, 2 changes
       - Camden Town to Euston by Northern Line
       - Euston to Oxford Circus by Victoria Line
       - Oxford Circus to Bond Street by Central Line
      """.trimIndent(),
      camdenToBondStreet.toString()
    )
  }

  @Test
  fun `can find multiple routes between stations`() {
    val map =
      SubwayMap(
        listOf(
          Segment(tufnellPark, archway, northernLine, 3),
          Segment(archway, highgate, northernLine, 3),
          Segment(highgate, archway, northernLine, 3),
          Segment(archway, kentishTown, northernLine, 3),
          Segment(kentishTown, camden, northernLine, 3),
          Segment(camden, euston, northernLine, 3),
          Segment(euston, warrenStreet, victoriaLine, 3),
          Segment(warrenStreet, oxfordCircus, victoriaLine, 3),
          Segment(oxfordCircus, bondStreet, centralLine, 2),
        )
      )

    val routes = map.routesFrom(highgate, oxfordCircus)

    assertEquals(1, routes.size)

    assertEquals(highgateToOxfordCircus, routes[0])
  }

  @Test
  fun `can handle no possible routes`() {
    val map =
      SubwayMap(
        listOf(
          Segment(tufnellPark, archway, northernLine, 3),
          Segment(archway, highgate, northernLine, 3),
          Segment(highgate, archway, northernLine, 3),
          Segment(archway, kentishTown, northernLine, 3),
          Segment(kentishTown, camden, northernLine, 3),
          Segment(camden, euston, northernLine, 3),
          Segment(euston, warrenStreet, victoriaLine, 3),
          Segment(warrenStreet, oxfordCircus, victoriaLine, 3),
          Segment(oxfordCircus, bondStreet, centralLine, 2),
        )
      )

    val routes = map.routesFrom(bondStreet, highgate)

    assertEquals(0, routes.size)
  }
}
