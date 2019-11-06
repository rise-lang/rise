package idealised.DPIA.ImperativePrimitives

import idealised.DPIA.Compilation.{TranslationContext, TranslationToImperative}
import idealised.DPIA.DSL._
import idealised.DPIA.Phrases._
import idealised.DPIA.Semantics.OperationalSemantics
import idealised.DPIA.Semantics.OperationalSemantics._
import idealised.DPIA.Types._
import idealised.DPIA._

import scala.language.reflectiveCalls
import scala.xml.Elem

final case class MapSndAcc(dt1: DataType,
                           dt2: DataType,
                           dt3: DataType,
                           f: Phrase[AccType ->: AccType],
                           record: Phrase[AccType]) extends AccPrimitive
{
  override val t: AccType =
    (dt1: DataType) ->: (dt2: DataType) ->: (dt3: DataType) ->:
      (f :: acc"[$dt3]" ->: acc"[$dt2]") ->:
      (record :: acc"[$dt1 x $dt3]") ->: acc"[$dt1 x $dt2]"

  override def visitAndRebuild(fun: VisitAndRebuild.Visitor): Phrase[AccType] = {
    MapSndAcc(fun.data(dt1), fun.data(dt2), fun.data(dt3),
      VisitAndRebuild(f, fun),
      VisitAndRebuild(record, fun))
  }

  override def eval(s: Store): AccIdentifier = ???

  override def prettyPrint: String = s"(mapSndAcc ${PrettyPhrasePrinter(f)} ${PrettyPhrasePrinter(record)})"

  override def xmlPrinter: Elem =
    <mapSndAcc dt1={ToString(dt1)} dt2={ToString(dt2)} dt3={ToString(dt3)}>
      <f>{Phrases.xmlPrinter(f)}</f>
      <record>{Phrases.xmlPrinter(record)}</record>
    </mapSndAcc>
}