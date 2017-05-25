package idealised.DPIA

import idealised.DPIA.ImperativePrimitives.{Assign, Idx, IdxAcc, Seq}
import idealised.DPIA.Phrases.{BinOp, Identifier, Literal, Pair, Phrase, Proj1, Proj2, UnaryOp}
import idealised.DPIA.Semantics.OperationalSemantics.{FloatData, IndexData, IntData}
import idealised.DPIA.Types._
import idealised.SurfaceLanguage.Operators
import lift.arithmetic.{ContinuousRange, NamedVar}

import scala.language.{implicitConversions, reflectiveCalls}

package object DSL {

  implicit class BinOps(lhs: Phrase[ExpType]) {
    def +(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.ADD, lhs, rhs)
    def -(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.SUB, lhs, rhs)
    def *(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.MUL, lhs, rhs)
    def /(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.DIV, lhs, rhs)
    def %(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.MOD, lhs, rhs)
    def >(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.GT, lhs, rhs)
    def <(rhs: Phrase[ExpType]) = BinOp(Operators.Binary.LT, lhs, rhs)
    def unary_- = UnaryOp(Operators.Unary.NEG, lhs)
  }

  implicit class ExpPhraseExtensions(e: Phrase[ExpType]) {
    def `@`(index: Phrase[ExpType]): Idx = (index.t, e.t) match {
      case (ExpType(IndexType(n1)), ExpType(ArrayType(n2, dt))) if n1 == n2 =>
        Idx(n1, dt, index, e)
      case x => error(x.toString, "(exp[idx(n)], exp[n.dt])")
    }
  }

  implicit class AccPhraseExtensions(a: Phrase[AccType]) {
    def `@`(index: Phrase[ExpType]): IdxAcc = (index.t, a.t) match {
      case (ExpType(IndexType(n1)), AccType(ArrayType(n2, dt))) if n1 == n2 =>
        IdxAcc(n1, dt, index, a)
      case x => error(x.toString, "(exp[idx(n)], acc[n.dt])")
    }
  }

  implicit class Assignment(lhs: Phrase[AccType]) {
    def :=(rhs: Phrase[ExpType]): Assign = {
      Assign(lhs, rhs)
    }

    def :=|(dt: DataType) = new {
      def |(rhs: Phrase[ExpType]): Phrase[CommandType] = {
        Assign(dt, lhs, rhs)
      }
    }
  }

  implicit class CallLambda[T1 <: PhraseType, T2 <: PhraseType](fun: Phrase[T1 -> T2]) {
    def apply(arg: Phrase[T1]): Phrase[T2] = Lifting.liftFunction(fun)(arg)

    def $(arg: Phrase[T1]): Phrase[T2] = apply(arg)
  }

  implicit class CallExpLambda[T <: PhraseType](fun: Phrase[ExpType -> T]) {
    def apply(arg: Phrase[ExpType]): Phrase[T] = CallLambda[ExpType, T](fun)(arg)
    def apply(arg: Nat): Phrase[T] = Lifting.liftFunctionToNatLambda(fun)(arg)

    def $(arg: Phrase[ExpType]): Phrase[T] = apply(arg)
    def $(arg: Nat): Phrase[T] = apply(arg)
  }

  implicit class CallNatDependentLambda[T <: PhraseType](fun: Phrase[`(nat)->`[T]]) {
    def apply(arg: Nat): Phrase[T] =
      Lifting.liftNatDependentFunction(fun)(arg)

    def $(arg: Nat): Phrase[T] = apply(arg)
  }

  implicit class CallTypeDependentLambda[T <: PhraseType](fun: Phrase[`(dt)->`[T]]) {
    def apply(arg: DataType): Phrase[T] =
      Lifting.liftTypeDependentFunction(fun)(arg)

    def $(arg: DataType): Phrase[T] = apply(arg)
  }

  implicit class FunComp[T1 <: PhraseType, T2 <: PhraseType](f: Phrase[T1 -> T2]) {
    def o[T3 <: PhraseType](g: Phrase[T3 -> T1]): Phrase[T3 -> T2] = {
      λ(g.t.inT)(arg => f(g(arg)))
    }
  }

  implicit class SequentialComposition(c1: Phrase[CommandType]) {
    def `;`(c2: Phrase[CommandType]): Phrase[CommandType] = Seq(c1, c2)
  }

  implicit class VarExtensions(v: Phrase[VarType]) {
    def rd: Proj1[ExpType, AccType] = π1(v)

    def wr: Proj2[ExpType, AccType] = π2(v)
  }

  implicit class IdentExpPhraseExtensions(i: Identifier[ExpType]) {
    def asNatIdentifier = NamedVar(i.name)
    def asNatIdentifier(withUpperBound: Nat) =
      NamedVar(i.name, ContinuousRange(0, withUpperBound))
  }

  implicit class NatExtensions(n: Nat) {
    def asPhrase = Literal(IndexData(n), IndexType(n.max))
    def asPhrase(withType: IndexType) = Literal(IndexData(n), withType)
  }

  implicit def toLiteralInt(i: Int): Literal = Literal(IntData(i), int)
  implicit def toLiteralFloat(f: Float): Literal = Literal(FloatData(f), float)

  implicit def toPair[T1 <: PhraseType, T2 <: PhraseType](pair: (Phrase[T1], Phrase[T2])): Pair[T1, T2] =
    Phrases.Pair(pair._1, pair._2)
}
