package model.mapping

import libt.spreadsheet.reader.workflow._
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt._

import model.PeerCompanies._
import libt.error.generic.Validated

package object peers {

  val peersMapping = Seq[Strip](
    Path('companyName),
    Path('ticker),
    Path('src_doc),
    Path('filingDate),
    Path('group),
    Path('fiscalYear),
    Path('comments),
    Path('link),
    Path('groupDesc),
    Path('changes),
    Path('subGroup),
    Path('peerCoName),
    Path('peerTicker),
    Path('value))

  def Workflow: FrontPhase[Seq[Model]] =
    MappingPhase(Mapping) >> CombinerPhase

  def Mapping = WorkbookMapping(Seq(Area(TPeers, Offset(1, 1), None, ColumnOrientedLayout(RawValueReader), peersMapping)))
  def CombinerPhase : Phase[Seq[Seq[Model]], Seq[Model]] =
    (_, xs) => Validated(xs.head.filter { model =>
      peerId.forall(_.forall(model.nonEmpty(_)))
    })
}
