package CommandPatterns

import Core._
import Core.OperationalSemantics._
import opencl.generator.OpenCLAST.Block

case class Skip() extends CommandPattern {

  override def typeCheck(): CommandType = CommandType()

  override def eval(s: Store): Store = s

  override def substitute[T <: PhraseType](phrase: Phrase[T], `for`: Phrase[T]): CommandPattern = this

  override def substituteImpl: Phrase[CommandType] = this

  override def toC = ""

  override def toOpenCL(b: Block): Block = b

  override def prettyPrint: String = ""

}
