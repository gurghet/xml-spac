package io.dylemma.spac.json

import java.io.{File, InputStream, Reader}

import com.fasterxml.jackson.core.{JsonFactory, JsonParser}
import io.dylemma.spac.{ConsumableLike, Handler}

trait JsonResource[R] {
	def createParser(factory: JsonFactory, resource: R): JsonParser
}

object JsonResource {
	implicit def consumableLike[T: JsonResource]: ConsumableLike[T, JsonEvent] = new ConsumableLike[T, JsonEvent] {
		def apply[A](resource: T, handler: Handler[JsonEvent, A]) = {
			runIterator(JsonEvents(resource).iterator, handler)
		}
	}

	def apply[T](f: (JsonFactory, T) => JsonParser): JsonResource[T] = new JsonResource[T] {
		def createParser(factory: JsonFactory, resource: T): JsonParser = f(factory, resource)
	}
	implicit val fileResource = JsonResource[File](_ createParser _)
	implicit val inputStreamResource = JsonResource[InputStream](_.createParser(_).enable(JsonParser.Feature.AUTO_CLOSE_SOURCE))
	implicit val readerResource = JsonResource[Reader](_.createParser(_).enable(JsonParser.Feature.AUTO_CLOSE_SOURCE))
	implicit val stringResource = JsonResource[String](_ createParser _)
}
