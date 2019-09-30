package elevate.lift.rules

import elevate.core.strategies.predicate._
import elevate.lift.strategies.traversal._
import elevate.lift.extractors._
import elevate.core.{Failure, Lift, NotApplicable, RewriteResult, Strategy, Success}
import elevate.lift.strategies.predicate._
import lift.core._
import lift.core.primitives._
import lift.core.DSL._

// Describing possible movements between pairs of primitives

object movement {
  // - Notation -
  // x >> y: piping operator, x then y
  // *f: map(f)
  // T: transpose
  // S: slide/split
  // J: join

  // transpose

  def mapMapFBeforeTranspose: Strategy[Lift] = `**f >> T -> T >> **f`
  case object `**f >> T -> T >> **f` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case Apply(
      `transpose`,
      Apply(Apply(`map`, Apply(`map`, f)), y)) =>
        Success(y |> transpose |> map(map(f)))
      // LCNF
      case _apply(_transpose(),
      _apply(
      mapMapF@_apply(_map(), _lambda(n7, _apply(
              _apply(_map(), _lambda(n6, _apply(
      f, n61))), n71))),
      arg
      )
      ) if contains[Lift](n6).apply(n61) && contains[Lift](n7).apply(n71) =>
        Success(arg |> transpose |> mapMapF)
      case _ => Failure(mapMapFBeforeTranspose)
    }
    override def toString = "mapMapFBeforeTranspose"
  }

  def transposeBeforeMapMapF: Strategy[Lift] = `T >> **f -> **f >> T`
  case object `T >> **f -> **f >> T` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _apply(_map() , _apply(_map(), f)),
      _apply(_transpose() , y)) =>
        Success(y |> map(map(f)) |> transpose)
      case _ => Failure(transposeBeforeMapMapF)
    }
    override def toString = "transposeBeforeMapMapF"
  }

  // split/slide

  private def isSplitOrSlide(s: Expr): Boolean = s match {
    case _depApply(_depApply(_slide(), _: Nat), _: Nat) => true
    case _depApply(_split(), _: Nat) => true
    case _ => false
  }

  def slideBeforeMapMapF: Strategy[Lift] = `S >> **f -> *f >> S`
  case object `S >> **f -> *f >> S` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _apply(_map(), _apply(_map(), f)),
      _apply(s, y)) if isSplitOrSlide(s) =>
        Success(y |> map(f) |> s)
      case _ => Failure(slideBeforeMapMapF)
    }
    override def toString = "slideBeforeMapMapF"
  }

  def mapFBeforeSlide: Strategy[Lift] = `*f >> S -> S >> **f`
  case object `*f >> S -> S >> **f` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      s,
      _apply(_apply(_map(), f), y)) if isSplitOrSlide(s) =>
        Success(y |> s |> map(map(f)))
      case _ => Failure(mapFBeforeSlide)
    }
    override def toString = "mapFBeforeSlide"
  }

  // join

  def joinBeforeMapF: Strategy[Lift] = `J >> *f -> **f >> J`
  case object `J >> *f -> **f >> J` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _apply(_map(), f),
      _apply(_join(), y)
      ) =>
        Success(y |> map(map(f)) >> join)
      case _ => Failure(joinBeforeMapF)
    }
    override def toString = "joinBeforeMapF"
  }

  def mapMapFBeforeJoin: Strategy[Lift] = `**f >> J -> J >> *f`
  case object `**f >> J -> J >> *f` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _join(),
      _apply(_apply(map, _apply(_map(), f)), y)
      ) =>
        Success(y |> join |> map(f))
      case _ => Failure(mapMapFBeforeJoin)
    }
    override def toString = "mapMapFBeforeJoin"
  }

  // special-cases
  // slide + transpose

  def transposeBeforeSlide: Strategy[Lift] = `T >> S -> *S >> T >> *T`
  case object `T >> S -> *S >> T >> *T` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      s,
      _apply(_transpose(), y)
      ) if isSplitOrSlide(s) =>
        Success(y |> map(s) |> transpose >> map(transpose))
      case _ => Failure(transposeBeforeSlide)
    }
    override def toString = "transposeBeforeSlide"
  }

  def transposeBeforeMapSlide: Strategy[Lift] = `T >> *S -> S >> *T >> T`
  case object `T >> *S -> S >> *T >> T` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _apply(_map(), s),
      _apply(_transpose(), y)
      ) if isSplitOrSlide(s) =>
        Success(y |> s |> map(transpose) |> transpose)
      case _ => Failure(transposeBeforeMapSlide)
    }
    override def toString = "transposeBeforeMapSlide"
  }

  def mapSlideBeforeTranspose: Strategy[Lift] = `*S >> T -> T >> S >> *T`
  case object `*S >> T -> T >> S >> *T` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _transpose(),
      _apply(_apply(_map(), s), y)
      ) if isSplitOrSlide(s) =>
        Success(y |> transpose >> s >> map(transpose))
      case _ => Failure(mapSlideBeforeTranspose)
    }
    override def toString = "mapSlideBeforeTranspose"
  }

  // transpose + join

  def joinBeforeTranspose: Strategy[Lift] = `J >> T -> *T >> T >> *J`
  case object `J >> T -> *T >> T >> *J` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _transpose(),
      _apply(_join(), y)
      ) =>
        Success(y |> map(transpose) |> transpose |> map(join))
      case _ => Failure(joinBeforeTranspose)
    }
    override def toString = "joinBeforeTranspose"
  }

  def transposeBeforeMapJoin: Strategy[Lift] = `T >> *J -> *T >> J >> T`
  case object `T >> *J -> *T >> J >> T` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _apply(_map(), _join()),
      _apply(_transpose(), y)
      ) =>
        Success(y |> map(transpose) |> join |> transpose)
      case _ => Failure(transposeBeforeMapJoin)
    }
    override def toString = "transposeBeforeMapJoin"
  }

  def mapTransposeBeforeJoin: Strategy[Lift] = `*T >> J -> T >> *J >> T`
  case object `*T >> J -> T >> *J >> T` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _join(),
      _apply(_apply(_map(), _transpose()), y)
      ) =>
        Success(y |> transpose |> map(join) |> transpose)
      case _ => Failure(mapTransposeBeforeJoin)
    }
    override def toString = "mapTransposeBeforeJoin"
  }

  def mapJoinBeforeTranspose: Strategy[Lift] = `*J >> T -> T >> *T >> J`
  case object `*J >> T -> T >> *T >> J` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _transpose(),
      _apply(_apply(_map(), _join()), y)
      ) =>
        Success(y |> transpose |> map(transpose) |> join)
      case _ => Failure(mapJoinBeforeTranspose)
    }
    override def toString = "mapJoinBeforeTranspose"
  }

  // join + join

  def joinBeforeJoin: Strategy[Lift] = `J >> J -> *J >> J`
  case object `J >> J -> *J >> J` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _join(),
      _apply(_join(), y)
      ) =>
        Success(y |> map(join) >> join)
      case _ => Failure(joinBeforeJoin)
    }
    override def toString = "joinBeforeJoin"
  }

  def mapJoinBeforeJoin: Strategy[Lift] = `*J >> J -> J >> J`
  case object `*J >> J -> J >> J` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _join(),
      _apply(_apply(_map(), _join()), y)
      ) =>
        Success(y |> join |> join)
      case _ => Failure(mapJoinBeforeJoin)
    }
    override def toString = "mapJoinBeforeJoin"
  }

  // split + slide

  def slideBeforeSplit: Strategy[Lift] = `slide(n)(s) >> split(k) -> slide(k+n-s)(k) >> map(slide(n)(s))`
  case object `slide(n)(s) >> split(k) -> slide(k+n-s)(k) >> map(slide(n)(s))` extends Strategy[Lift] {
    def apply(e: Lift): RewriteResult[Lift] = e match {
      case _apply(
      _depApply(_split(), k: Nat),
      _apply(_depApply(_depApply(_slide(), n: Nat), s: Nat), y)
      ) =>
        Success(y |> slide(k + n - s)(k) |> map(slide(n)(s)))
      case _ => Failure(slideBeforeSplit)
    }
    override def toString = "slideBeforeSplit"
  }
}