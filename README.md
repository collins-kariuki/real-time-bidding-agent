<p align="center" style="background-color:purple;">
  <a href="" rel="noopener">
 <img width=200px height=200px src="https://global-uploads.webflow.com/60859154db955843c7fc06e7/6086e5b19dcb8d16f5451709_Eskimi.svg" alt="Project logo"></a>
</p>

<h3 align="center">Real-time Bidding Agent</h3>

---

## 📝 Table of Contents

- [About](#about)
- [Getting Started](#getting_started)
- [Built Using](#built_using)
- [Authors](#authors)
- [Acknowledgments](#acknowledgement)

## 🧐 About <a name = "about"></a>

A real-time bidding agent is a simple HTTP server that accepts JSON requests, does some matching between advertising campaigns and the received bid request and responds with either a JSON response with a matched campaign (bid) or an empty response (no bid).

## 🏁 Getting Started <a name = "getting_started"></a>

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

Import Build Using eg. Metals on VScode

Open a Terminal/CMD window on the root directory and run 'sbt run'

Send a Post request using eg. (Postman or Curl) to the defined port using a BidRequest JSON pyload found in the data.scala file

### Prerequisites

- Scala > 2.13.1
- Sbt

## 🔧 Running the tests <a name = "tests"></a>

Open a Terminal/CMD window on the root directory and run 'sbt test'

## ⛏️ Built Using <a name = "built_using"></a>

- [Scala](https://www.mongodb.com/) - Language
- [Akka Actors](https://www.scala-lang.org/) - Concurency Handler
- [Akka HTTP](https://doc.akka.io/docs/akka-http/current/) - Server Environment

## ✍️ Authors <a name = "authors"></a>

- [@collins-kariuki](https://github.com/collins-kariuki)

## 🎉 Acknowledgements <a name = "acknowledgement"></a>

- OpenRTB protocol.
