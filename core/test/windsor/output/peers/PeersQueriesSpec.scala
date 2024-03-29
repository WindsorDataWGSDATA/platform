package windsor.output.peers

import com.mongodb.casbah.MongoClient
import org.joda.time.DateTime

import org.scalatest.{FlatSpec, Tag}
import windsor.persistence._
import libt._
import test._

@DbTest
class PeersQueriesSpec extends FlatSpec {

  trait Fixture {

  val db = {
    val client = MongoClient()("windsor-peers-specs")
    client.dropDatabase()
    val db = PeersCompaniesDb(client)
    db.IndexDb.drop

    db.IndexDb.insert(
        Model('ticker -> Value("A"), 'name -> Value("The A")),
        Model('ticker -> Value("B"), 'name -> Value("The B")),
        Model('ticker -> Value("C"), 'name -> Value("The C")),
        Model('ticker -> Value("D"), 'name -> Value("The D")))
    db

  }

  val models = {
    val time: DateTime = new DateTime()
    Seq(
      // A peers
      Model('ticker -> Value("A"), 'filingDate -> Value(time.toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("B"), 'peerTicker -> Value("B")),
      Model('ticker -> Value("A"), 'filingDate -> Value(time.toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("C"), 'peerTicker -> Value("C")),

      // B peers
      Model('ticker -> Value("B"), 'filingDate -> Value(time.toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("A"), 'peerTicker -> Value("A")),
      Model('ticker -> Value("B"), 'filingDate -> Value(time.toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("C"), 'peerTicker -> Value("C")),
      Model('ticker -> Value("B"), 'filingDate -> Value(time.minusDays(1).toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("C"), 'peerTicker -> Value("WillNotAppear")),

      // C peers
      Model('ticker -> Value("C"), 'filingDate -> Value(time.toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("A"), 'peerTicker -> Value("A")),
      Model('ticker -> Value("C"), 'filingDate -> Value(time.toDate), 'fiscalYear -> Value(2013), 'peerCoName -> Value("B"), 'peerTicker -> Value("B")))
  }

    db.insert(models:_*)
  }

  behavior of "Peers Queries for Reports"

    it should "Get collection that have a target Company as a Peer" in new Fixture {
      assert(db.indirectPeersOf("A").exists(m => m /!/ 'ticker == "B"))
      assert(db.indirectPeersOf("A").exists(m => m /!/ 'ticker == "C"))
    }

    it should "Get Direct Peers for a single target Company" in new Fixture {
      assert(db.peersOf("B").toList.map(_ - 'filingDate - 'fiscalYear).toSet ===
        Set(
          Model('peerTicker -> Value("A"), 'ticker -> Value("B"), 'peerCoName -> Value("The A")),
          Model('peerTicker -> Value("C"),'ticker -> Value("B"), 'peerCoName -> Value("The C"))))
    }

    it should "Get empty seq for a target company with no peers" in new Fixture {
      assert(db.peersOf("D") === Seq())
    }

    it should "Get empty seq for no target company" in new Fixture {
      assert(db.peersOf() === Seq())
    }


    it should "Get Direct Peers for target collection" in new Fixture {
      assert(db.peersOf("A","B").toList.map(_ -  'filingDate - 'fiscalYear).toSet ===
        Set(Model('ticker -> Value("A"),'peerTicker -> Value("B"), 'peerCoName -> Value("The B")),
            Model('ticker -> Value("A"),'peerTicker -> Value("C"), 'peerCoName -> Value("The C")),
            Model('ticker -> Value("B"),'peerTicker -> Value("A"), 'peerCoName -> Value("The A")),
            Model('ticker -> Value("B"),'peerTicker -> Value("C"), 'peerCoName -> Value("The C"))))
    }

    it should "Get Peers of Peers collection for a target Company" in new Fixture {
      assert(db.peersOfPeersOf("A")._2.map(_ - 'filingDate - 'fiscalYear).toSet ===
        Set(
          // B peers
          Model('ticker -> Value("B"),'peerTicker -> Value("A"), 'peerCoName -> Value("The A")),
          Model('ticker -> Value("B"),'peerTicker -> Value("C"), 'peerCoName -> Value("The C")),

          // C peers
          Model('ticker -> Value("C"),'peerTicker -> Value("A"), 'peerCoName -> Value("The A")),
          Model('ticker -> Value("C"),'peerTicker -> Value("B"), 'peerCoName -> Value("The B"))))
    }

    it should "nameValueFromIndex to return default value when not in index" in new Fixture {
      assert(db.nameValueFromIndex("foo", Value("bar")) === Value("bar"))
    }

  it should "foo" in new Fixture {
    assert(db.peersOfPeersFromPrimary("A")._2.map(_ - 'filingDate) ===
      Seq(
        Model('ticker -> Value("A"),
          'peerTicker -> Value("B"),
          'peerCoName -> Value("The B"),
          'fiscalYear -> Value(2013)),

        Model('ticker -> Value("A"),
          'peerTicker -> Value("C"),
          'peerCoName -> Value("The C"),
          'fiscalYear -> Value(2013))))
  }
}
