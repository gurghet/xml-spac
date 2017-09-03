package io.dylemma.spac

import io.dylemma.spac.handlers._
import io.dylemma.spac.types.Id

import scala.language.higherKinds
import scala.util.Try

/** An immutable object that can be used to create `Handler`s.
	*/
trait Consumer[-In, +Out] extends AbstractHandlerFactory[In, Out, Id, Consumer] { self =>

	def consume[S](source: S)(implicit consume: ConsumableLike[S, In]): Out = {
		consume(source, makeHandler())
	}

	def mapResult[U](f: Id[Out] => Id[U]): Consumer[In, U] = new Consumer[In, U] {
		def makeHandler(): Handler[In, U] = new MappedConsumerHandler(f, self.makeHandler())
	}

	def wrapSafe: Consumer[In, Try[Out]] = Consumer.WrapSafe(this)
	def unwrapSafe[T](implicit ev: Out <:< Try[T]): Consumer[In, T] = Consumer.UnwrapSafe(asInstanceOf[Consumer[In, Try[T]]])
}

object Consumer {

	case class WrapSafe[In, Out](self: Consumer[In, Out]) extends Consumer[In, Try[Out]] {
		def makeHandler(): Handler[In, Try[Out]] = new SafeConsumerHandler(self.makeHandler())
	}

	case class UnwrapSafe[In, Out](self: Consumer[In, Try[Out]]) extends Consumer[In, Out] {
		def makeHandler(): Handler[In, Out] = new UnwrapSafeConsumerHandler(self.makeHandler())
	}

	case class ToList[A]() extends Consumer[A, List[A]] {
		def makeHandler(): Handler[A, List[A]] = {
			new ToListHandler[A]
		}
		override def toString = "ToList"
	}

	case class First[A]() extends Consumer[A, A] {
		def makeHandler(): Handler[A, A] = {
			new GetFirstHandler[A]
		}
		override def toString = "First"
	}

	case class FirstOption[A]() extends Consumer[A, Option[A]] {
		def makeHandler(): Handler[A, Option[A]] = {
			new GetFirstOptionHandler[A]
		}
		override def toString = "FirstOption"
	}

	case class Fold[A, R](init: R, f: (R, A) => R) extends Consumer[A, R] {
		def makeHandler(): Handler[A, R] = {
			new FoldHandler(init, f)
		}
		override def toString = s"Fold($init, $f)"
	}

	case class ForEach[A](f: A => Any) extends Consumer[A, Unit] {
		def makeHandler() = new ForEachHandler(f)
		override def toString = s"ForEach($f)"
	}

	case class Constant[A](result: A) extends Consumer[Any, A] {
		def makeHandler(): Handler[Any, A] = new ConstantHandler(result)
		override def toString = s"Constant($result)"
	}
}