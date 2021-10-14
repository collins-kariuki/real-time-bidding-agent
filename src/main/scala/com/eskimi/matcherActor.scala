package com.eskimi.matcher

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import com.eskimi.data._
import com.eskimi.types._

import scala.util.Random
import java.util.UUID.randomUUID
import akka.actor.typed.ActorRef

object Matcher {

  def apply(): Behavior[BidMatch] = Behaviors.receive { (context, message) =>
    def matchSiteId(site: Site, targeting: Targeting): Boolean = {
      site match {
        case Site(id, _) if (targeting.targetedSiteIds.contains(site.id)) =>
          true
        case Site(_, domain) if (targeting.domain == site.domain) => true
        case _                                                    => false
      }
    }

    def matchBidfloor(
        bidRequst: BidRequest,
        campaign: Campaign
    ): List[Impression] = {
      val impressionList = bidRequst.imp.get
      return for (impression <- impressionList
                  if campaign.bid >= impression.bidFloor.get) yield impression
    }

    def matchCountry(bidRequst: BidRequest, campaign: Campaign): Boolean = {
      bidRequst match {
        case BidRequest(_, _, _, _, device)
            if (device.isDefined && device.get.geo.get.country.get == campaign.country) => {
          true
        }
        case BidRequest(_, _, _, site, _)
            if (site.isDefined && site.get.geo.get.country.get == campaign.country) => {
          true
        }
        case _ => false
      }
    }

    case class ImpressionMatch(id: String, banner: Banner, price: Double)

    def heightMatch(
        imp: Impression,
        banner: List[Banner],
        bid: BidRequest
    ): Option[ImpressionMatch] = {
      val random = new Random
      imp match {
        case Impression(_, _, _, _, _, _, h, _)
            if (imp.h.isDefined && banner
              .exists(bn => imp.h.getOrElse(0) == bn.height)) => {
          val validBanners =
            banner.filter(bn => imp.h.getOrElse(0) == bn.height)
          val randomImpression =
            validBanners(random.nextInt(validBanners.length))
          Option(
            ImpressionMatch(
              bid.id,
              randomImpression,
              imp.bidFloor.get
            )
          )
        }

        case Impression(_, _, _, _, hmin, _, _, _)
            if (imp.hmin.isDefined && banner
              .exists(bn => imp.hmin.getOrElse(0) == bn.height)) => {
          val validBanners =
            banner.filter(bn => imp.hmin.getOrElse(0) == bn.height)

          val randomImpression =
            validBanners(random.nextInt(validBanners.length))
          Option(
            ImpressionMatch(
              bid.id,
              randomImpression,
              imp.bidFloor.get
            )
          )
        }

        case Impression(_, _, _, _, _, hmax, _, _)
            if (imp.hmax.isDefined && banner
              .exists(bn => imp.hmax.getOrElse(0) == bn.height)) => {
          val validBanners =
            banner.filter(bn => imp.hmax.getOrElse(0) == bn.height)
          val randomImpression =
            validBanners(random.nextInt(validBanners.length))
          Option(
            ImpressionMatch(
              bid.id,
              randomImpression,
              imp.bidFloor.get
            )
          )
        }
        case _ => None
      }
    }

    def matchBannerSize(
        imp: Impression,
        banner: List[Banner],
        bid: BidRequest
    ): Option[ImpressionMatch] = {
      imp match {
        case Impression(_, _, _, w, _, _, _, _)
            if (imp.w.isDefined && banner
              .exists(bn => imp.w.getOrElse(0) == bn.width)) => {
          val validBanners_w =
            banner.filter(bn => imp.w.getOrElse(0) == bn.width)

          heightMatch(imp, validBanners_w, message.bidRequest)

        }
        case Impression(_, wmin, _, _, _, _, _, _)
            if (imp.wmin.isDefined && banner
              .exists(bn => imp.wmin.getOrElse(0) == bn.width)) => {
          val validBanners_wmin =
            banner.filter(bn => imp.wmin.getOrElse(0) == bn.width)

          heightMatch(imp, validBanners_wmin, message.bidRequest)

        }
        case Impression(_, _, wmax, _, _, _, _, _)
            if (imp.wmax.isDefined && banner
              .exists(bn => imp.wmax.getOrElse(0) == bn.width)) => {
          val validBanners_wmax =
            banner.filter(bn => imp.wmax.getOrElse(0) == bn.width)

          heightMatch(imp, validBanners_wmax, message.bidRequest)

        }
        case _ => None
      }
    }

    //Main
    //Validate bidFloor
    val validImpression = matchBidfloor(message.bidRequest, message.campaign)
    //Validate Country and Validate SiteId
    if (validImpression.nonEmpty &&
        matchCountry(message.bidRequest, message.campaign) &&
        matchSiteId(message.bidRequest.site, message.campaign.targeting)) {

      // Validate Width and Height
      val validImpressionFinal =
        validImpression
          .map(impression =>
            matchBannerSize(
              impression,
              message.campaign.banners,
              message.bidRequest
            )
          )
          .filter(imp => imp.isDefined)

      //Choose Random Impression
      if (validImpressionFinal.nonEmpty) {

        val random = new Random
        val chosenImpression = validImpressionFinal(
          random.nextInt(validImpressionFinal.length)
        ).get
        //Build response.
        val response = BidResponse(
          id = randomUUID().toString(),
          bidRequestId = chosenImpression.id,
          price = chosenImpression.price,
          adid = Option(message.campaign.id.toString()),
          banner = Option(chosenImpression.banner)
        )

        message.replyTo ! ReceiveBidResponse(response, message.origin)
      } else {
        //Send No response if No Banner match
        message.replyTo ! NoResponse("No match", message.origin)
      }
    } else {
      //Send No response if No Bid floor, Country and site Id match
      message.replyTo ! NoResponse("No match", message.origin)
    }

    Behaviors.same
  }
}
