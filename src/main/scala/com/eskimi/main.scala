package com.eskimi.main

import com.eskimi.types._
import com.eskimi.data._
import com.eskimi.matcher._

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout

import scala.util.Random
import java.util.UUID.randomUUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import scala.concurrent.Future

object myJsonsupport extends DefaultJsonProtocol {
  implicit val bannerFormat = jsonFormat4(Banner)
  implicit val responseFormat = jsonFormat5(BidResponse)
  implicit val geoFormat = jsonFormat1(Geo)
  implicit val deviceFormat = jsonFormat2(Device)
  implicit val userFormat = jsonFormat2(User)
  implicit val siteFormat = jsonFormat2(Site)
  implicit val impressionFormat = jsonFormat8(Impression)
  implicit val bidRequstFormat = jsonFormat5(BidRequest)
}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import myJsonsupport._
import spray.json._

object MainMatcher {

  def apply(): Behavior[Messages] = Behaviors.setup { context =>
    val matcher = context.spawn(Matcher(), "Matcher_actor")

    var noMatchCount = 0

    Behaviors.receiveMessage(message =>
      message match {

        case ReceiveBidRequest(_, _) =>
          data.activeCampaigns.map(campaign =>
            matcher ! BidMatch(
              message.asInstanceOf[ReceiveBidRequest].bidRequest,
              campaign,
              context.self,
              message.asInstanceOf[ReceiveBidRequest].replyTo
            )
          )

          Behaviors.same
        case NoResponse(_, _) => {
          noMatchCount += 1
          if (noMatchCount < data.activeCampaigns.length) {

            Behaviors.same
          } else {
            message.asInstanceOf[NoResponse].replyTo ! message
              .asInstanceOf[NoResponse]
            Behaviors.same
          }
        }
        case ReceiveBidResponse(_, _) => {
          //Pass the response to Supervisor to send a 200
          message.asInstanceOf[ReceiveBidResponse].replyTo ! message
            .asInstanceOf[ReceiveBidResponse]
            .bidResponse
          Behaviors.same

        }
      }
    )

  }

}

object mine extends App {

  val matchMain: ActorSystem[ReceiveBidRequest] =
    ActorSystem(MainMatcher(), "Supervisor")

// Actor system for the HTTP server
  implicit val system = ActorSystem(Behaviors.empty, "HTTP")

// If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(
    system.settings.config.getDuration("bid-matcher.routes.ask-timeout")
  )

  def matchBid(bidRequest: BidRequest): Future[Messages] =
    matchMain.ask(ReceiveBidRequest(bidRequest, _))

  val route =
    path("bid") {
      post {
        entity(as[BidRequest]) { bidRequest =>
          onSuccess(matchBid(bidRequest)) { response =>
            response match {
              case BidResponse(_, _, _, _, _) =>
                complete(response.asInstanceOf[BidResponse])
              case NoResponse(_, _) => complete(StatusCodes.NoContent)
            }
          }
        }

      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(
    s"\n\n\nServer now online. Send a POST Request to http://localhost:8080/bid  Payload = Bid request JSON \n\n\n"
  )

}
