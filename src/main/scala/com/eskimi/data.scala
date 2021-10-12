package com.eskimi.data
import com.eskimi.types._

object data {
  val bidRequestSample = """{
  "id": "SGu1Jpq1IO",
  "site": {
    "id": "0006a522ce0f4bbbbaa6b3c38cafaa0f",
    "domain": "fake.tld"
  },
  "device": {
    "id": "440579f4b408831516ebd02f6e1c31b4",
    "geo": {
      "country": "LT"
    }
  },
  "imp": [
    {
      "id": "1",
      "wmin": 50,
      "wmax": 300,
      "hmin": 100,
      "hmax": 300,
      "h": 250,
      "w": 300,
      "bidFloor": 3.12123
    }
  ],
  "user": {
    "geo": {
      "country": "LT"
    },
    "id": "USARIO1"
  }
}"""
  val activeCampaigns = Seq(
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
  )

  val responseSample = BidResponse(
    adid = Option("1"),
    banner = Option(
      Banner(
        1,
        "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
        250,
        300
      )
    ),
    bidRequestId = "SGu1Jpq1IO",
    id = "83f6e120-060b-4463-90e9-cdc695b9c2fd",
    price = 3.12123
  )

}
