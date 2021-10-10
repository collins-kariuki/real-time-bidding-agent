import scala.util.Random
import java.util.UUID.randomUUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object Server extends App {
  //Campaign protocol stores information about the advertising campaign
  case class Campaign(
      id: Int,
      country: String,
      targeting: Targeting,
      banners: List[Banner],
      bid: Double
  )
  case class Targeting(targetedSiteIds: Set[String], domain: String)
  case class Banner(id: Int, src: String, width: Int, height: Int)
  //
  //
  val activeCampaigns =
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = Set("0006a522ce0f4bbbbaa6b3c38cafaa0f"), // changed collection from Seq to Immutable set. Added domain field.
        domain = "fake.tld"
      ),
      banners = List(
        Banner(
          id = 1,
          src =
            "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250
        )
      ),
      bid = 5d
    )

  //
  //
  //Bid request protocol
  case class BidRequest(
      id: String,
      imp: Option[List[Impression]],
      site: Site,
      user: Option[User],
      device: Option[Device]
  )

  case class Impression(
      id: String,
      wmin: Option[Int],
      wmax: Option[Int],
      w: Option[Int],
      hmin: Option[Int],
      hmax: Option[Int],
      h: Option[Int],
      bidFloor: Option[Double]
  )
  case class Site(id: String, domain: String)
  case class User(id: String, geo: Option[Geo])
  case class Device(id: String, geo: Option[Geo])
  case class Geo(country: Option[String])

  val bid = BidRequest(
    id = "SGu1Jpq1IO",
    imp = Option(
      List(
        Impression(
          id = "1",
          wmin = Option(50),
          wmax = Option(300),
          hmin = Option(100),
          hmax = Option(300),
          h = Option(250),
          w = Option(300),
          bidFloor = Option(3.12123)
        ),
        Impression(
          id = "22",
          wmin = Option(50),
          wmax = Option(300),
          hmin = Option(100),
          hmax = Option(300),
          h = Option(250),
          w = None,
          bidFloor = Option(3.12123)
        )
      )
    ),
    site = Site(id = "0006a522ce0f4bbbbaa6b3c38cafaa0f", domain = "fake.tld"),
    user =
      Option(User(id = "USARIO1", geo = Option(Geo(country = Option("LT"))))),
    device = Option(
      Device(
        id = "440579f4b408831516ebd02f6e1c31b4",
        geo = Option(Geo(country = Option("LT")))
      )
    )
  )

  case class BidResponse(
      id: String,
      bidRequestId: String,
      price: Double,
      adid: Option[String],
      banner: Option[Banner]
  )

  def matchSiteId(site: Site, targeting: Targeting): Boolean = {
    site match {
      case Site(id, _) if (targeting.targetedSiteIds.contains(site.id)) => true
      case Site(_, domain) if (targeting.domain == site.domain)         => true
      case _                                                            => false
    }
  }

  println(matchSiteId(bid.site, activeCampaigns.targeting))

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
      banner: List[Banner]
  ): Option[ImpressionMatch] = {
    val random = new Random
    imp match {
      case Impression(_, _, _, _, _, _, h, _)
          if (imp.h.isDefined && banner.exists(bn =>
            imp.h.getOrElse(0) == bn.height
          )) => {
        val validBanners = banner.filter(bn => imp.h.getOrElse(0) == bn.height)
        val randomImpression = validBanners(random.nextInt(validBanners.length))
        Option(ImpressionMatch(bid.id, randomImpression, imp.bidFloor.get))
      }

      case Impression(_, _, _, _, hmin, _, _, _)
          if (imp.hmin.isDefined && banner.exists(bn =>
            imp.hmin.getOrElse(0) == bn.height
          )) => {
        val validBanners =
          banner.filter(bn => imp.hmin.getOrElse(0) == bn.height)

        val randomImpression = validBanners(random.nextInt(validBanners.length))
        Option(ImpressionMatch(bid.id, randomImpression, imp.bidFloor.get))
      }

      case Impression(_, _, _, _, _, hmax, _, _)
          if (imp.hmax.isDefined && banner.exists(bn =>
            imp.hmax.getOrElse(0) == bn.height
          )) => {
        val validBanners =
          banner.filter(bn => imp.hmax.getOrElse(0) == bn.height)
        val randomImpression = validBanners(random.nextInt(validBanners.length))
        Option(ImpressionMatch(bid.id, randomImpression, imp.bidFloor.get))
      }
      case _ => None
    }
  }

  def matchBannerSize(
      imp: Impression,
      banner: List[Banner]
  ): Option[ImpressionMatch] = {
    imp match {
      case Impression(_, _, _, w, _, _, _, _)
          if (imp.w.isDefined && banner.exists(bn =>
            imp.w.getOrElse(0) == bn.width
          )) => {
        val validBanners_w =
          banner.filter(bn => imp.w.getOrElse(0) == bn.width)
        println("hapa width")
        heightMatch(imp, validBanners_w)

      }
      case Impression(_, wmin, _, _, _, _, _, _)
          if (imp.wmin.isDefined && banner.exists(bn =>
            imp.wmin.getOrElse(0) == bn.width
          )) => {
        val validBanners_wmin =
          banner.filter(bn => imp.wmin.getOrElse(0) == bn.width)
        println("hapa wmin")
        heightMatch(imp, validBanners_wmin)

      }
      case Impression(_, _, wmax, _, _, _, _, _)
          if (imp.wmax.isDefined && banner.exists(bn =>
            imp.wmax.getOrElse(0) == bn.width
          )) => {
        val validBanners_wmax =
          banner.filter(bn => imp.wmax.getOrElse(0) == bn.width)
        println("hapa wmax")
        heightMatch(imp, validBanners_wmax)

      }
      case _ => None
    }
  }

  //tests Mbwakni

  val impwithhw =
    matchBidfloor(bid, activeCampaigns)
      .map(x => matchBannerSize(x, activeCampaigns.banners))
      .filter(impp => impp.isDefined)

  println(matchBidfloor(bid, activeCampaigns))
  println(impwithhw)

  println(activeCampaigns.bid)

  println(matchCountry(bid, activeCampaigns))

  println(
    activeCampaigns.targeting.targetedSiteIds
      .contains("0006a522ce0f4bbbbaa6b3c38cafaa0f")
  )

  //Main
  //Validate bidFloor
  val validImpression = matchBidfloor(bid, activeCampaigns)
  //Validate Country and Validate SiteId
  if (validImpression.nonEmpty &&
      matchCountry(bid, activeCampaigns) &&
      matchSiteId(bid.site, activeCampaigns.targeting)) {

  // Validate Width and Height
    val validImpressionFinal =
      validImpression
        .map(impression => matchBannerSize(impression, activeCampaigns.banners))
        .filter(imp => imp.isDefined)
    println(validImpressionFinal)
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
        adid = Option(activeCampaigns.id.toString()),
        banner = Option(chosenImpression.banner)
      )
      println(response)
    }
  }

}
