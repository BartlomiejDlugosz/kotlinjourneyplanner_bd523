package journeyplan

import org.junit.Test
import kotlin.test.assertEquals

class TravelModelTest {
  @Test
  fun `printing stations shows their names`() {
    assertEquals("South Kensington", Station("South Kensington").toString())
    assertEquals("Knightsbridge", Station("Knightsbridge").toString())
  }

  @Test
  fun `printing lines shows their names`() {
    assertEquals("District Line", Line("District").toString())
    assertEquals("Circle Line", Line("Circle").toString())
  }

  @Test
  fun `printing segments shows the stations and line`() {
    val segment = Segment(Station("South Kensington"), Station("Knightsbridge"), Line("Piccadilly"), 3)
    assertEquals("South Kensington to Knightsbridge by Piccadilly Line", segment.toString())
  }

  @Test
  fun `can calculate number of changes`() {
    val route = Route(
      listOf(
        Segment(Station("South Kensington"), Station("Knightsbridge"), Line("Piccadilly"), 3),
        Segment(Station("Knightsbridge"), Station("Hyde Park Corner"), Line("Piccadilly"), 4),
        Segment(Station("Hyde Park Corner"), Station("Green Park"), Line("Piccadilly"), 2),
        Segment(Station("Green Park"), Station("Oxford Circus"), Line("Victoria"), 1),
      )
    )

    assertEquals(1, route.numChanges())
  }

  @Test
  fun `can calculate total duration`() {
    val route = Route(
      listOf(
        Segment(Station("South Kensington"), Station("Knightsbridge"), Line("Piccadilly"), 3),
        Segment(Station("Knightsbridge"), Station("Hyde Park Corner"), Line("Piccadilly"), 4),
        Segment(Station("Hyde Park Corner"), Station("Green Park"), Line("Piccadilly"), 2),
        Segment(Station("Green Park"), Station("Oxford Circus"), Line("Victoria"), 1),
      )
    )

    assertEquals(10, route.duration())
  }

  @Test
  fun `can calculate total duration with no segments`() {
    val route = Route(listOf())

    assertEquals(0, route.duration())
  }
}
