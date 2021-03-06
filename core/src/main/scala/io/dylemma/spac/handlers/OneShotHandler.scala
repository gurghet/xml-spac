package io.dylemma.spac.handlers

import io.dylemma.spac.Handler

class OneShotHandler[Out](value: =>Out) extends Handler[Any, Out] {
	override def toString = s"OneShot(<lazy>)"

	private var didEmit = false
	def isFinished: Boolean = didEmit

	private def emitWith[T](result: => T) = try result finally didEmit = true
	def handleInput(input: Any): Option[Out] = emitWith{ Some(value) }
	def handleError(err: Throwable): Option[Out] = emitWith{ Some(value) }
	def handleEnd(): Out = emitWith{ value }
}
