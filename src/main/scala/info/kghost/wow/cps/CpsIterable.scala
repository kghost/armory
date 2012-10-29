package info.kghost.wow.cps

import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.collection.IterableLike
import scala.util.continuations.cpsParam

object CpsIterable {
  implicit def to[A, Repr](xs: IterableLike[A, Repr]) = new {
    def cps = new {
      def foreach[B](f: A => Any @cpsParam[Object, Object]): Unit @cpsParam[Object, Object] = {
        val it = xs.iterator
        while (it.hasNext) f(it.next)
      }
      def map[B, That](f: A => B @cpsParam[Object, Object])(implicit cbf: CanBuildFrom[Repr, B, That]): That @cpsParam[Object, Object] = {
        val b = cbf(xs.repr)
        foreach(b += f(_))
        b.result
      }
      def flatMap[B, That](f: A => GenTraversableOnce[B] @cpsParam[Object, Object])(implicit cbf: CanBuildFrom[Repr, B, That]): That @cpsParam[Object, Object] = {
        val b = cbf(xs.repr)
        for (x <- this) b ++= f(x)
        b.result
      }
      def foldLeft[B](z: B)(op: (B, A) => B @cpsParam[Object, Object]) = {
        var result = z
        foreach { x => result = op(result, x) }
        result
      }
    }
  }
}

object CpsIterableUnit {
  implicit def to[A, Repr](xs: IterableLike[A, Repr]) = new {
    def cps = new {
      def foreach[B](f: A => Any @cpsParam[Unit, Unit]): Unit @cpsParam[Unit, Unit] = {
        val it = xs.iterator
        while (it.hasNext) f(it.next)
      }
      def map[B, That](f: A => B @cpsParam[Unit, Unit])(implicit cbf: CanBuildFrom[Repr, B, That]): That @cpsParam[Unit, Unit] = {
        val b = cbf(xs.repr)
        foreach(b += f(_))
        b.result
      }
      def flatMap[B, That](f: A => GenTraversableOnce[B] @cpsParam[Unit, Unit])(implicit cbf: CanBuildFrom[Repr, B, That]): That @cpsParam[Unit, Unit] = {
        val b = cbf(xs.repr)
        foreach(b ++= f(_))
        b.result
      }
      def foldLeft[B](z: B)(op: (B, A) => B @cpsParam[Unit, Unit]) = {
        var result = z
        foreach { x => result = op(result, x) }
        result
      }
    }
  }
}