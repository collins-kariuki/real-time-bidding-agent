package com.eskimi

import com.eskimi.types._
import com.eskimi.matcher.Matcher._

import org.scalatest.wordspec.AnyWordSpec

class MainTesting extends AnyWordSpec {
  
// TEST DATA //
  val test_bid = BidRequest(
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
  val test_bid1 = BidRequest(
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
    site = Site(id = "", domain = ""),
    user =
      Option(User(id = "USARIO1", geo = Option(Geo(country = Option("LT"))))),
    device = Option(
      Device(
        id = "440579f4b408831516ebd02f6e1c31b4",
        geo = Option(Geo(country = Option("LT")))
      )
    )
  )
  val test_activeCampaigns =
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


  val countryMatch_test_data = BidRequest(
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
    site = Site(id = "0006a522ce0f4bbbbasjsdfvknsf", domain = "unknown.mnk"),
    user =
      Option(User(id = "USARIO1", geo = Option(Geo(country = Option("MN"))))),
    device = Option(
      Device(
        id = "",
        geo = Option(Geo(country = Option("KL")))
      )
    )
  )
  
  val response: List[Impression] = List(
    Impression(
      "1",
      Option(50),
      Option(300),
      Option(300),
      Option(100),
      Option(300),
      Option(250),
      Option(3.12123)
    ),
    Impression(
      "22",
      Option(50),
      Option(300),
      None,
      Option(100),
      Option(300),
      Option(250),
      Option(3.12123)
    )
  )
  

  val test_impression = Impression( 
          id = "1",
          wmin = Option(50),
          wmax = Option(300),
          hmin = Option(100),
          hmax = Option(300),
          h = Option(250),
          w = Option(300),
          bidFloor = Option(3.12123)
  )
  val banner_response = Option(
    ImpressionMatch(
      "SGu1Jpq1IO",
      Banner(
        1,
        "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
        300,
        250
      ),
      3.12123
    )
  )

  // TESTS ///
  "A MatchSite" should {
    "return True if a match is found" in {
      assert(matchSiteId(test_bid.site, test_activeCampaigns.targeting) == true)
    }
    "return false if no match is found" in {
      assert(
        matchSiteId(test_bid1.site, test_activeCampaigns.targeting) == false
      )

    }
  }
"A BidFloorMatch" should {
    "return Impression List" in {
      assert(matchBidfloor(test_bid, test_activeCampaigns) == response)
    }

  }
  "A HeightMatch" should {
    "return matched Impression" in {
     val banners = test_activeCampaigns.banners
     
     val height_return = heightMatch(test_impression, banners, test_bid)

      assert(height_return == banner_response)
  
    }
  }

  "A BannerMatch" should {
    "return a matched banner" in {
      val banner_return = Option(ImpressionMatch("SGu1Jpq1IO",Banner(1,"https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",300,250),3.12123))
      assertResult(banner_return) {
        matchBannerSize(test_impression, test_activeCampaigns.banners, test_bid)
      }
    }
  }
  

  "A CountryMatch" should {
    "return true if country matched campaign "  in {
      assert(matchCountry(test_bid, test_activeCampaigns) == true)
    }
    "return false  if country does not match campign" in {
      assert(matchCountry(countryMatch_test_data, test_activeCampaigns) == false)
    }
  }

  

}
