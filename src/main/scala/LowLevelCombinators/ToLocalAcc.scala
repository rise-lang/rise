package LowLevelCombinators

import Core.OperationalSemantics._
import Core._
import apart.arithmetic.ArithExpr
import opencl.generator.OpenCLAST.VarRef

import scala.xml.Elem

case class ToLocalAcc(dt: DataType,
                      p: Phrase[AccType])
  extends LowLevelAccCombinator with ViewAcc with GeneratableAcc {

  override lazy val `type` = acc"[$dt]"

  override def typeCheck(): Unit = {
    import TypeChecker._
    p checkType acc"[$dt]"
  }

  override def eval(s: Store): AccIdentifier = ???

  override def toOpenCL(env: ToOpenCL.Environment): VarRef = ???

  override def toOpenCL(env: ToOpenCL.Environment,
                        arrayAccess: List[(ArithExpr, ArithExpr)],
                        tupleAccess: List[ArithExpr], dt: DataType): VarRef = ???

  override def visitAndRebuild(fun: VisitAndRebuild.fun): Phrase[AccType] = {
    ToLocalAcc(fun(dt), VisitAndRebuild(p, fun))
  }

  override def prettyPrint: String = s"(toLocalAcc ${PrettyPrinter(p)})"

  override def xmlPrinter: Elem =
    <toLocalAcc>
      {Core.xmlPrinter(p)}
    </toLocalAcc>
}