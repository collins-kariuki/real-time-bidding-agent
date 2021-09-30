object Server extends App {
  //Campaign protocol stores information about the advertising campaign
  case class Campaign(
      id: Int,
      country: String,
      targeting: Targeting,
      banners: List[Banner],
      bid: Double
  )
  case class Targeting(targetedSiteIds: Seq[String])
  case class Banner(id: Int, src: String, width: Int, height: Int)
  //
  //
  val activeCampaigns =
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f") // Use collection of your choice
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

  case class ImpressionMatch(id: Option[String], banner: Option[List[Banner]])

  val bannerz = activeCampaigns.banners
  val biddd = bid.imp.get
  val impress = biddd.filter(yy => activeCampaigns.bid >= (yy.bidFloor.get))

  def heightMatch(
      imp: Impression,
      banner: List[Banner]
  ): Option[ImpressionMatch] = {
    imp match {
      case Impression(_, _, _, _, _, _, h, _)
          if (imp.h.isDefined && banner
            .filter(bn => imp.h.getOrElse(0) == bn.height)
            .nonEmpty) => {
        val validBanners = banner.filter(bn => imp.h.getOrElse(0) == bn.height)
        Option(ImpressionMatch(Option(bid.id), Option(validBanners)))
      }

      case Impression(_, _, _, _, hmin, _, _, _)
          if (imp.hmin.isDefined && banner
            .filter(bn => imp.hmin.getOrElse(0) == bn.height)
            .nonEmpty) => {
        val validBanners =
          banner.filter(bn => imp.hmin.getOrElse(0) == bn.height)
        Option(ImpressionMatch(Option(bid.id), Option(validBanners)))
      }

      case Impression(_, _, _, _, _, hmax, _, _)
          if (imp.hmax.isDefined && banner
            .filter(bn => imp.hmax.getOrElse(0) == bn.height)
            .nonEmpty) => {
        val validBanners =
          banner.filter(bn => imp.hmax.getOrElse(0) == bn.height)
        Option(ImpressionMatch(Option(bid.id), Option(validBanners)))
      }
      case _ => None
    }
  }

  def matcher(
      imp: Impression,
      banner: List[Banner]
  ): Option[ImpressionMatch] = {
    imp match {
      case Impression(_, _, _, w, _, _, _, _)
          if (imp.w.isDefined && banner
            .filter(bn => imp.w.getOrElse(0) == bn.width)
            .nonEmpty) => {
        val validBanners_w =
          banner.filter(bn => imp.w.getOrElse(0) == bn.width)
        println("hapa width")
        heightMatch(imp, validBanners_w)

      }
      case Impression(_, wmin, _, _, _, _, _, _)
          if (imp.wmin.isDefined && banner
            .filter(bn => imp.wmin.getOrElse(0) == bn.width)
            .nonEmpty) => {
        val validBanners_wmin =
          banner.filter(bn => imp.wmin.getOrElse(0) == bn.width)
        println("hapa wmin")
        heightMatch(imp, validBanners_wmin)

      }
      case Impression(_, _, wmax, _, _, _, _, _)
          if (imp.wmax.isDefined && banner
            .filter(bn => imp.wmax.getOrElse(0) == bn.width)
            .nonEmpty) => {
        val validBanners_wmax =
          banner.filter(bn => imp.wmax.getOrElse(0) == bn.width)
        println("hapa wmax")
        heightMatch(imp, validBanners_wmax)

      }
      case _ => None
    }
  }
val impwithhw =
    impress.map(x => matcher(x, bannerz)).filter(impp => impp.isDefined)

  println(impwithhw)

  println(activeCampaigns.bid)

}
