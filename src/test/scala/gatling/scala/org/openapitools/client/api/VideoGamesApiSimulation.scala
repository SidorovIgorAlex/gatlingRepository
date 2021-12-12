package gatling.scala.org.openapitools.client.api

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.io.File
import scala.concurrent.duration.DurationInt

class VideoGamesApiSimulation extends Simulation {

    def getCurrentDirectory = new File("").getAbsolutePath
    def userDataDirectory = getCurrentDirectory + "/src/test/resources/data"

// Setup http protocol configuration
    val httpConf = http
        .baseUrl("http://localhost:8080/app")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
        .acceptHeader("application/json")

    // Set up CSV feeders
    val deleteVideoGamePATHFeeder = csv(userDataDirectory + File.separator + "deleteVideoGame-pathParams.csv").random
    val editVideoGamePATHFeeder = csv(userDataDirectory + File.separator + "editVideoGame-pathParams.csv").random
    val getVideoGamePATHFeeder = csv(userDataDirectory + File.separator + "getVideoGame-pathParams.csv").random
    val createVideoGamePATHFeeder = csv(userDataDirectory + File.separator + "createVideoGame-pathParams.csv").circular

    // Setup all scenarios

    val scncreateVideoGame = scenario("createVideoGameSimulation")
        .feed(createVideoGamePATHFeeder)
        .exec(
            http("createVideoGame")
              .httpRequest("POST","/videogames")
              .body(ElFileBody("data/bodyPostRequest.json")).asJson
        )
        .exec(
            http("editVideoGame")
              .httpRequest("PUT","/videogames/${videoGameId}")
              .body(ElFileBody("data/bodyPutRequest.json")).asJson
        )
        .exec(http("getVideoGame")
            .httpRequest("GET","/videogames/${videoGameId}")
        )
        .exec(
            http("deleteVideoGame")
                .httpRequest("DELETE","/videogames/${videoGameId}")
        )

    val scndeleteVideoGame = scenario("deleteVideoGameSimulation")
      .feed(deleteVideoGamePATHFeeder)


    val scngetVideoGame = scenario("getVideoGameSimulation")
      .feed(getVideoGamePATHFeeder)
      .exec(http("getVideoGame")
        .httpRequest("GET","/videogames/${videoGameId}"))

    val scnlistVideoGames = scenario("listVideoGamesSimulation")
      .exec(http("listVideoGames")
        .httpRequest("GET","/videogames"))

    val scneditVideoGame = scenario("editVideoGameSimulation")
      .feed(editVideoGamePATHFeeder)
      .exec(http("editVideoGame")
        .httpRequest("PUT","/videogames/${videoGameId}")
        .body(ElFileBody("data/bodyPutRequest.json")).asJson
      )

    setUp(
        scncreateVideoGame.inject(
            rampUsersPerSec(1) to 20 during (1 minute),
            constantUsersPerSec(20) during (1 minute),
            rampUsersPerSec(20) to 50 during (1 minute),
            constantUsersPerSec(50) during (1 minute)
        ),
        scnlistVideoGames.inject(
            rampUsersPerSec(1) to 20 during (1 minute),
            constantUsersPerSec(20) during (1 minute),
            rampUsersPerSec(20) to 50 during (1 minute),
            constantUsersPerSec(50) during (1 minute)
        ),
        scngetVideoGame.inject(
            rampUsersPerSec(1) to 10 during (1 minute),
            constantUsersPerSec(10) during (1 minute),
            rampUsersPerSec(10) to 15 during (1 minute),
            constantUsersPerSec(15) during (1 minute)
        )
    ).protocols(httpConf)
}
