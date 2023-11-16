package io.github.scottweaver.zio.testcontainers.neo4j

import com.dimafeng.testcontainers.Neo4jContainer
import neotypes.{AsyncDriver, GraphDatabase}
import org.testcontainers.utility.DockerImageName
import zio.{Task, ULayer, ZEnvironment, ZIO, ZLayer}
import neotypes.zio.implicits._

object ZNeo4jContainer {

  final case class Settings(
    imageVersion: String
  )

  object Settings {
    val community5: ULayer[Settings] = ZLayer.succeed(
      Settings(
        imageVersion = "community"
      )
    )

    val default: ULayer[Settings] = community5
  }

  val live: ZLayer[Settings, Throwable, AsyncDriver[Task]] = {
    def makeScopedContainer(settings: Settings) = ZIO.acquireRelease(
      ZIO.attempt {
        val containerDef = Neo4jContainer.Def(
          dockerImageName = DockerImageName.parse(s"neo4j:${settings.imageVersion}")
        )

        containerDef.start()
      }.orDie
    )(container =>
      ZIO.attempt(container.stop())
        .ignoreLogged
    )

    ZLayer.scopedEnvironment {
      for {
        settings <- ZIO.service[Settings]
        container <- makeScopedContainer(settings)
        driver <- GraphDatabase.asyncDriver(container.boltUrl)
      } yield ZEnvironment(driver)
    }
  }

}
