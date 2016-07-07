package LowLevelCombinators

import Core.OperationalSemantics._
import Core._
import DSL.typed._
import apart.arithmetic.NamedVar
import opencl.generator.OpenCLAST.{Block, Comment, VarDecl}

import scala.xml.Elem

case class New(dt: DataType,
               addressSpace: AddressSpace,
               f: Phrase[(ExpType x AccType) -> CommandType])
  extends LowLevelCommCombinator {

  override def typeCheck(): Unit = {
    import TypeChecker._
    f checkType t"var[$dt] -> comm"
  }

  override def eval(s: Store): Store = {
    val f_ = OperationalSemantics.eval(s, f)
    val arg = identifier(newName(), f.t.inT)
    val newStore = OperationalSemantics.eval(s + (arg.name -> 0), f_(arg))
    newStore - arg.name
  }

  override def visitAndRebuild(fun: VisitAndRebuild.fun): Phrase[CommandType] = {
    New(fun(dt), addressSpace, VisitAndRebuild(f, fun))
  }

  override def toOpenCL(block: Block, env: ToOpenCL.Environment): Block = {
    val v = NamedVar(newName())

    if (addressSpace == PrivateMemory) {
      (block: Block) += VarDecl(v.name, DataType.toType(dt))
    } else {
      // TODO: allocate elsewhere
      (block: Block) += Comment(s"new ${v.name} $dt $addressSpace")
    }

    val f_ = Lift.liftFunction(f)
    val v_ = identifier(v.name, f.t.inT)
    ToOpenCL.cmd(f_(v_), block, env)
  }

  override def prettyPrint: String = s"(new $addressSpace ${PrettyPrinter(f)})"

  override def xmlPrinter: Elem =
    <new dt={ToString(dt)} addressspace={ToString(addressSpace)}>
      {Core.xmlPrinter(f)}
    </new>
}