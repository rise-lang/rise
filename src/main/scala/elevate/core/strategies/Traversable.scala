package elevate.core.strategies

import elevate.core.Strategy

trait Traversable[P] {
  def all: Strategy[P] => Strategy[P]
  def oneHandlingState: Boolean => Strategy[P] => Strategy[P]
  def some: Strategy[P] => Strategy[P]
}