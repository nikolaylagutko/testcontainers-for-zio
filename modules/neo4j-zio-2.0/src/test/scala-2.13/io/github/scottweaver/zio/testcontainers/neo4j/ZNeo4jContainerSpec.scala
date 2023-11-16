package io.github.scottweaver.zio.testcontainers.neo4j

import neotypes.AsyncDriver
import zio.{Task, ZIO}
import zio.test._

object ZNeo4jContainerSpec extends ZIOSpecDefault {

  def spec = suite("ZNeo4jContainer")(
    test("should start Neo4j database") {
      for {
        driver <- ZIO.service[AsyncDriver[Task]]
      } yield assertTrue(driver != null)
    }
  ).provideShared(
    ZNeo4jContainer.Settings.default,
    ZNeo4jContainer.live
  )

}
