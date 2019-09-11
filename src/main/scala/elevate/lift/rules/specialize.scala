package elevate.lift.rules

import elevate.core.{Failure, Lift, RewriteResult, Strategy, Success}
import elevate.lift.extractors._
import lift.OpenCL.primitives.mapGlobal
import lift.core.{Apply, Lambda, primitives}
import lift.core.primitives._

object specialize {

  case object mapSeq extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case primitives.map => Success(primitives.mapSeq)
      case _ => Failure(mapSeq)
    }
    override def toString = "mapSeq"
  }

  case object mapGlobal extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case primitives.map => Success(lift.OpenCL.primitives.mapGlobal(0))
      case _ => Failure(mapGlobal)
    }
    override def toString = "mapGlobal"
  }

  // only transforms maps which contain ForeignFunctions or mapSeqs
  case object mapSeqCompute extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      // (mapSeq λη1. (my_abs η1))
      case _apply(_map(), l@_lambda(_, _apply(_foreignFunction(_, _), _))) => Success(Apply(primitives.mapSeq, l))
      // (map λη1. ((mapSeq λη2. (my_abs η2)) η1))
      case _apply(_map(), l@_lambda(_, _apply(_apply(_mapSeq(), _), _))) => Success(Apply(primitives.mapSeq, l))
      case _ => Failure(mapSeqCompute)
    }
    override def toString = "mapSeqCompute"
  }

  case object reduceSeq extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case primitives.reduce => Success(primitives.reduceSeq)
      case _ => Failure(reduceSeq)
    }
    override def toString = "reduceSeq"
  }

  case class slideSeq(rot: primitives.slideSeq.Rotate) extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case primitives.slide => Success(primitives.slideSeq(rot))
      case _ => Failure(slideSeq(rot))
    }
    override def toString = s"slideSeq($rot)"
  }
}